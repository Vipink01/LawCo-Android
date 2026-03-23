package com.business.lawco.fragment.filterDialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.consumer.SelectAddressAdapter
import com.business.lawco.adapter.consumer.SelectAreaOfPracticeAdapter
import com.business.lawco.adapter.consumer.SelectLocation
import com.business.lawco.databinding.BottomSheetFilterBinding
import com.business.lawco.model.consumer.CategoryModel
import com.business.lawco.model.consumer.Data
import com.business.lawco.model.consumer.SelectAddressModel
import com.business.lawco.model.consumer.SelectAreaOfPracticeModel
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class BottomSheetAttorneyFilterDialog(var filterApply: FilterApply?) : BottomSheetDialogFragment(), View.OnClickListener, SelectLocation, SelectAreaOfPracticeAdapter.SelectPractice {

    lateinit var binding: BottomSheetFilterBinding
    private lateinit var selectAreaOfPracticeAdapter: SelectAreaOfPracticeAdapter
    private lateinit var selectAddressAdapter: SelectAddressAdapter

    private var selectAddressModel: MutableList<Data> = arrayListOf()
    var selectAreaOfPracticeList: MutableList<SelectAreaOfPracticeModel> = arrayListOf()
    lateinit var sessionManager: SessionManager

    private lateinit var consumerHomeScreenViewModel: ConsumerHomeScreenViewModel
    private var dataItem: Data? = null
    private val selectpra: MutableList<String> = mutableListOf()
    private var selectId: MutableList<String> = mutableListOf()
    private var isInitialLoad = true // Add this flag

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = resources.getDrawable(R.drawable.bottom_white_layout, null)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isHideable = false
            behavior.isDraggable = false
        }
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetFilterBinding.inflate(LayoutInflater.from(requireActivity()), container, false)

        binding.btnTextClear.setOnClickListener {
            sessionManager.setFilterUserAddress("")
            val filteredList: ArrayList<Data> = ArrayList()
            selectpra.clear()
            selectId.clear()
            filterApply?.apply(filteredList, selectpra, selectId)
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        sessionManager = SessionManager(requireContext())
        consumerHomeScreenViewModel = ViewModelProvider(this)[ConsumerHomeScreenViewModel::class.java]

        // Initialize selectId with null check
        val savedFilterJson = sessionManager.getFilterPracticeChecked()
        selectId = if (!savedFilterJson.isNullOrEmpty()) {
            try {
                Gson().fromJson(savedFilterJson, object : TypeToken<MutableList<String>>() {}.type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // Initialize selectpra from session
        selectpra.clear()
        // This line might cause issue if selectAreaOfPracticeList is empty initially
        // selectAreaOfPracticeList.forEach {
        //     if (it.status) {
        //         selectpra.add(it.areaOfPractice)
        //     }
        // }

        // Instead, get practices from saved IDs
        if (savedFilterJson != null && savedFilterJson.isNotEmpty()) {
            try {
                val savedIds: MutableList<String> = Gson().fromJson(savedFilterJson, object : TypeToken<MutableList<String>>() {}.type)
                // We'll populate selectpra later when we load the data
            } catch (e: Exception) {
                // Handle error
            }
        }

        selectAddressAdapter = SelectAddressAdapter(selectAddressModel, requireContext(), this)
        binding.rcvSelectCountry.adapter = selectAddressAdapter
        binding.rcvSelectCountry.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        selectAreaOfPracticeAdapter =
            SelectAreaOfPracticeAdapter(selectAreaOfPracticeList, requireContext(), this)
        binding.rcvSelectAttorney.adapter = selectAreaOfPracticeAdapter
        binding.rcvSelectAttorney.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        areaOfPracticeSearch()
        locationSearch()

        binding.btnApplyFilter.setOnClickListener(this)
        binding.constraintLocation.setOnClickListener(this)
        binding.constraintAreaOfPractice.setOnClickListener(this)
        binding.btCloseFilter.setOnClickListener(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun areaOfPracticeSearch() {
        binding.EditTextSearchAttorney.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("Whole List", selectAreaOfPracticeList.toString())
                val searchWord = s.toString()
                filterPractice(searchWord)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun locationSearch() {
        binding.EditTextLocationSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchWord = s.toString()
                filter(searchWord)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun filter(text: String) {
        val filteredList: MutableList<Data> = ArrayList()
        for (item in selectAddressModel) {
            if (item.address.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault())) ||
                item.address.uppercase(Locale.ROOT).contains(text.uppercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }
        if (!filteredList.isEmpty()) {
            selectAddressAdapter.updateData(filteredList)
            binding.rcvSelectCountry.visibility = View.VISIBLE
        } else {
            binding.rcvSelectCountry.visibility = View.GONE
        }
    }

    private fun filterPractice(text: String) {
        val filteredList: MutableList<SelectAreaOfPracticeModel> = ArrayList()
        for (item in selectAreaOfPracticeList) {
            if (item.areaOfPractice.lowercase(Locale.ROOT)
                    .contains(text.lowercase(Locale.getDefault())) ||
                item.areaOfPractice.uppercase(Locale.ROOT)
                    .contains(text.uppercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }
        if (!filteredList.isEmpty()) {
            selectAreaOfPracticeAdapter.updateData(filteredList)
            binding.rcvSelectAttorney.visibility = View.VISIBLE
        } else {
            binding.rcvSelectAttorney.visibility = View.GONE
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.constraintAreaOfPractice -> {
                if (binding.CardViewAreaOfPractice.visibility == View.GONE) {
                    if (selectAreaOfPracticeList.isEmpty()) {
                        getAreaOfPractice()
                    }
                    binding.btOpenAreaOfPractice.setImageResource(R.drawable.filter_open_arrow)
                    binding.CardViewAreaOfPractice.visibility = View.VISIBLE
                } else {
                    binding.btOpenAreaOfPractice.setImageResource(R.drawable.filter_close_arrow)
                    binding.CardViewAreaOfPractice.visibility = View.GONE
                }
            }

            R.id.constraintLocation -> {
                if (binding.CardViewLocation.visibility == View.GONE) {
                    if (selectAddressModel.isEmpty()) {
                        getAddressList()
                    }
                    binding.btOpenLocation.setImageResource(R.drawable.filter_open_arrow)
                    binding.CardViewLocation.visibility = View.VISIBLE
                } else {
                    binding.btOpenLocation.setImageResource(R.drawable.filter_close_arrow)
                    binding.CardViewLocation.visibility = View.GONE
                }
            }

            R.id.btnApplyFilter -> {
                dismiss()
                // Extract the addresses from the filtered list
                val filteredList: ArrayList<Data> = ArrayList()
                // Save selected practice IDs to session
                val selectedIds = mutableListOf<String>()
                val selectedPractices = mutableListOf<String>()
                selectAreaOfPracticeList.forEach {
                    if (it.status) {
                        selectedPractices.add(it.areaOfPractice)
                        selectedIds.add(it.id.toString())
                    }
                }
                // Save to session
                sessionManager.setFilterPracticeChecked(Gson().toJson(selectedIds))
                if (dataItem != null) {
                    filteredList.add(dataItem!!)
                    sessionManager.setFilterUserAddress(dataItem!!.address)
                }
                filterApply?.apply(filteredList, selectedPractices, selectedIds)
            }

            R.id.btCloseFilter -> {
                sessionManager.setFilterUserAddress("")
                sessionManager.setFilterPracticeChecked("") // Clear saved filters
                selectAreaOfPracticeList.forEach { it.status = false } // Reset all selections
                selectAreaOfPracticeAdapter.updateData(selectAreaOfPracticeList)
                dismiss()
            }
        }
    }

    private fun getAddressList() {
        binding.locationProgressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            consumerHomeScreenViewModel.getAllRegisterLocation()
                .observe(viewLifecycleOwner) { jsonObject ->
                    binding.locationProgressBar.visibility = View.GONE
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val selectdata: SelectAddressModel =
                                Gson().fromJson(jsonObjectData, SelectAddressModel::class.java)
                            selectAddressModel = selectdata.data as MutableList<Data>
                            selectAddressModel.removeAll { it.address == null }

                            if (sessionManager.getFilterUserAddress() != "") {
                                for (i in selectAddressModel.indices) {
                                    val address = sessionManager.getFilterUserAddress()
                                    if (address == selectAddressModel[i].address) {
                                        selectAddressAdapter.selectedItemPos = i
                                        selectAddressAdapter.lastItemSelectedPos = i
                                        break
                                    }
                                }
                            }
                            selectAddressAdapter.updateData(selectAddressModel)
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }
    }

//    private fun getAreaOfPractice() {
//        binding.areaOfPracticeProgressBar.visibility = View.VISIBLE
//
//        lifecycleScope.launch {
//            consumerHomeScreenViewModel.getAreaOfPractice()
//                .observe(viewLifecycleOwner) { jsonObject ->
//                    binding.areaOfPracticeProgressBar.visibility = View.GONE
//                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
//                    if (jsonObjectData != null) {
//                        try {
//                            val categoryList =
//                                Gson().fromJson(jsonObjectData, CategoryModel::class.java)
//                            // Clear existing list first
//                          //  selectAreaOfPracticeList.clear()
//                            selectAreaOfPracticeList.addAll(categoryList.data.map {
//                                SelectAreaOfPracticeModel(
//                                    it.id, it.category_name
//                                )
//                            })
////                            val savedIdsJson = sessionManager.getFilterPracticeChecked()
////                            val savedIds: MutableList<String> = if (!savedIdsJson.isNullOrEmpty()) {
////                                Gson().fromJson(savedIdsJson, object : TypeToken<MutableList<String>>() {}.type)
////                            } else {
////                                mutableListOf()
////                            }
//
//                            for (i in 0 until selectAreaOfPracticeList.size) {
//                                if (selectId.contains(selectAreaOfPracticeList[i].areaOfPractice)) {
//                                    val range: SelectAreaOfPracticeModel = selectAreaOfPracticeList.get(i)
//                                    range.id = (selectAreaOfPracticeList.get(i).id)
//                                    range.status = true
//                                    range.areaOfPractice = (selectAreaOfPracticeList[i].areaOfPractice)
//                                    selectAreaOfPracticeList.set(i, range)
//                                }
//                            }
//
////                            for (i in 0 until selectAreaOfPracticeList.size) {
////                                val item = selectAreaOfPracticeList[i]
////                                // Check if this item's ID is in the saved list
////                                if (savedIds.contains(item.id.toString())) {
////                                    selectAreaOfPracticeList[i] = item.copy(status = true)
////                                    selectId.add(item.id.toString())
////                                    selectpra.add(item.areaOfPractice)
////                                }
////                            }
//
//                            selectAreaOfPracticeAdapter.updateData(selectAreaOfPracticeList)
//                            Log.e("All Category List", selectAreaOfPracticeList.toString())
//                        } catch (e: Exception) {
//                            sessionManager.alertErrorDialog(e.toString())
//                        }
//                    }
//                }
//        }
//    }

    private fun getAreaOfPractice() {
        binding.areaOfPracticeProgressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            consumerHomeScreenViewModel.getAreaOfPractice()
                .observe(viewLifecycleOwner) { jsonObject ->
                    binding.areaOfPracticeProgressBar.visibility = View.GONE
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val categoryList =
                                Gson().fromJson(jsonObjectData, CategoryModel::class.java)

                            // REMOVE THIS LINE - DON'T CLEAR THE LIST
                            // selectAreaOfPracticeList.clear()

                            // Only add items on initial load
                            if (isInitialLoad) {
                                selectAreaOfPracticeList.clear() // Clear only on first load
                                selectAreaOfPracticeList.addAll(categoryList.data.map {
                                    SelectAreaOfPracticeModel(
                                        it.id,
                                        it.category_name,
                                        false // Default status is false
                                    )
                                })
                                isInitialLoad = false
                            }

                            // Get saved IDs from session
                            val savedIdsJson = sessionManager.getFilterPracticeChecked()
                            val savedIds: MutableList<String> = if (!savedIdsJson.isNullOrEmpty()) {
                                Gson().fromJson(savedIdsJson, object : TypeToken<MutableList<String>>() {}.type)
                            } else {
                                mutableListOf()
                            }

                            // Mark items as selected based on saved IDs
                            for (i in selectAreaOfPracticeList.indices) {
                                val item = selectAreaOfPracticeList[i]
                                // Check if this item's ID is in the saved list
                                if (savedIds.contains(item.id.toString())) {
                                    selectAreaOfPracticeList[i] = item.copy(status = true)
                                }
                            }

                            selectAreaOfPracticeAdapter.updateData(selectAreaOfPracticeList)
                            Log.e("All Category List", selectAreaOfPracticeList.toString())
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }
    }

    override fun location(dataItem: Data) {
        this.dataItem = dataItem
    }

    override fun practice(status: Boolean, name: String, check: Boolean) {
//        val index = selectAreaOfPracticeList.indexOfFirst { it.areaOfPractice == name }
//        if (index != -1) {
//            selectAreaOfPracticeList[index] = selectAreaOfPracticeList[index].copy(status = status)
//        }
//       if (status){
//           selectpra.add(name)
//           selectId.add(selectAreaOfPracticeList.get())
//       }else{
//           if (selectpra.contains(name)){
//               selectpra.remove(name)
//               selectId.remove(id)
//           }
//       }
    }

}