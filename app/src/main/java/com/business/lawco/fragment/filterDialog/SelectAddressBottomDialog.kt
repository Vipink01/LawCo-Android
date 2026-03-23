package com.business.lawco.fragment.filterDialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.LocationMapActivity
import com.business.lawco.adapter.attroney.SavedAddressAdapter
import com.business.lawco.adapter.attroney.SelectAddress
import com.business.lawco.databinding.BottomSheetLocationBinding
import com.business.lawco.model.Address
import com.business.lawco.model.SavedAddressModel
import com.business.lawco.networkModel.common.CommonViewModel
import com.business.lawco.utility.AppConstant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectAddressBottomDialog(val selectAddress: HomeSelectAddress)
    : BottomSheetDialogFragment(), View.OnClickListener ,SelectAddress{

    lateinit var binding: BottomSheetLocationBinding

    lateinit var sessionManager: SessionManager
    private lateinit var commonViewModel: CommonViewModel

    private val addressList = ArrayList<Address>()
    private lateinit var savedAddressAdapter: SavedAddressAdapter

    private val mapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val searchIntent = result.data
            if (searchIntent != null) {
                val place = Autocomplete.getPlaceFromIntent(searchIntent)
                binding.etSearch.text = place.name
                Log.e("Lat_lang", place.latLng?.toString().toString())
                if (place.latLng != null) {
                    val mapIntent =
                        Intent(requireContext(), LocationMapActivity::class.java)
                    mapIntent.putExtra(
                        AppConstant.ADDRESS_ID, "")
                    mapIntent.putExtra(
                        AppConstant.LATITUDE,
                        place.latLng!!.latitude.toString()
                    )
                    mapIntent.putExtra(
                        AppConstant.LONGITUDE,
                        place.latLng!!.longitude.toString()
                    )
                    commonViewModel.isLoading.set(false)
                    dismiss()
                    startActivity(mapIntent)
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.i(ContentValues.TAG, "User canceled autocomplete")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = resources.getDrawable(R.drawable.bottom_white_layout, null)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isHideable = false
            behavior.isDraggable = false
        }
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetLocationBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        sessionManager = SessionManager(requireContext())
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel


        savedAddressAdapter = SavedAddressAdapter(addressList, requireActivity(),this)
        binding.rcySavedAddress.adapter = savedAddressAdapter
        binding.rcySavedAddress.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        savedAddressAdapter.setOnMenuItemClick(object : SavedAddressAdapter.OnMenuItemClick {
            override fun editAddress(addressId: String, latitude: String, longitude: String) {
                dismiss()
                val mapIntent = Intent(requireContext(), LocationMapActivity::class.java)
                mapIntent.putExtra(AppConstant.ADDRESS_ID, addressId)
                mapIntent.putExtra(AppConstant.LATITUDE, latitude)
                mapIntent.putExtra(AppConstant.LONGITUDE, longitude)
                startActivity(mapIntent)
            }

            override fun deleteAddress(addressId: String) {
                deleteAddressFromServer(addressId)
            }

            override fun defaultAddress(addressId: String) {
                makeDefaultAddress(addressId)
            }
        })


        binding.imageCross.setOnClickListener(this)
        binding.btGetCurrentLocation.setOnClickListener(this)
        binding.etSearch.setOnClickListener(this)

        getAddress()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.imageCross -> {
                dismiss()
            }

            R.id.btGetCurrentLocation -> {
                val intent = Intent(requireContext(), LocationMapActivity::class.java)
                startActivity(intent)
                dismiss()
            }

            R.id.etSearch -> {
                commonViewModel.isLoading.set(true)
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(requireContext())
                mapActivityResult.launch(intent)
            }

        }
    }

    private fun getAddress() {
        commonViewModel.isLoading.set(true)
        lifecycleScope.launch {
            commonViewModel.getAddress().observe(viewLifecycleOwner) { jsonObject ->
                commonViewModel.isLoading.set(false)
                val jsonObjectData = sessionManager.checkResponse(jsonObject)
                if (jsonObjectData != null) {
                    try {
                        val savedAddressList = Gson().fromJson(jsonObjectData, SavedAddressModel::class.java)
                        savedAddressAdapter.updateAddressList(savedAddressList.data)
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }

    }

    private fun deleteAddressFromServer(addressId: String) {
        commonViewModel.isLoading.set(true)

        lifecycleScope.launch {
            commonViewModel.deleteAddress(addressId).observe(viewLifecycleOwner) { jsonObject ->
                commonViewModel.isLoading.set(false)
                val jsonObjectData = sessionManager.checkResponse(jsonObject)

                if (jsonObjectData != null) {
                    try {
                        getAddress()
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }
    }

    private fun makeDefaultAddress(addressId: String) {
        commonViewModel.isLoading.set(true)

        lifecycleScope.launch {
            commonViewModel.setDefaultAddress(addressId).observe(viewLifecycleOwner) { jsonObject ->
                commonViewModel.isLoading.set(false)
                val jsonObjectData = sessionManager.checkResponse(jsonObject)

                if (jsonObjectData != null) {
                    try {
                        getAddress()
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }
    }

    override fun select(lat: String, lng: String) {
         sessionManager.setUserLat(lat)
         sessionManager.setUserLng(lng)
        sessionManager.setUserCurrent(false)
        selectAddress.selectAddress(lat,lng)
        dismiss()
    }

}