package com.business.lawco.fragment.consumerfragment.connections

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.adapter.consumer.ConnectionAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentConnectionsBinding
import com.business.lawco.fragment.filterDialog.BottomSheetAttorneyFilterDialog
import com.business.lawco.fragment.filterDialog.FilterApply
import com.business.lawco.model.consumer.ConnectionsDataModel
import com.business.lawco.model.consumer.ConnectionsModel
import com.business.lawco.model.consumer.Data
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeScreenViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConnectionsFragment : BaseFragment(), View.OnClickListener ,FilterApply{
    lateinit var binding: FragmentConnectionsBinding
    lateinit var sessionManager: SessionManager
    private lateinit var consumerHomeScreenViewModel: ConsumerHomeScreenViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private lateinit var connectionAdapter: ConnectionAdapter
    private var latitude: String = "0"
    private var longitude: String = "0"
    private var tAG: String = "Location"
    private var attronyList: MutableList<ConnectionsDataModel> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentConnectionsBinding.inflate(LayoutInflater.from(requireActivity()), container, false)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        consumerHomeScreenViewModel = ViewModelProvider(this)[ConsumerHomeScreenViewModel::class.java]
        binding.consumerHomeScreenViewModel = consumerHomeScreenViewModel

        if (sessionManager.getSelectType().equals("Requested")){
            selectDataType("Requested")
        }
        if (sessionManager.getSelectType().equals("Accepted")){
            selectDataType("Accepted")
        }
        if (sessionManager.getSelectType().equals("Declined")){
            selectDataType("Declined")
        }


        binding.connectionRefresh.setOnRefreshListener {
            locationData()
        }

        binding.imageBackArrow.setOnClickListener(this)
        binding.btFilter.setOnClickListener(this)

        binding.EtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                val searchWord = s.toString().trim()
                val searchList = ArrayList<ConnectionsDataModel>()
                if (searchWord.isNotEmpty()) {
                    for (item in attronyList) {
                        val nameMatch = item.full_name?.lowercase()?.contains(searchWord.lowercase()) == true
                        val areaMatch = item.area_of_practice?.lowercase()?.contains(searchWord.lowercase()) == true
                        if (nameMatch || areaMatch) {
                            searchList.add(item)
                        }
                    }
                    if (searchList.isNotEmpty()) {
                        connectionAdapter.updateList(searchList)
                        hideData(true)
                    } else {
                        hideData(false)
                    }

                } else {
                    if (attronyList.isNotEmpty()){
                        connectionAdapter.updateList(attronyList)
                        hideData(true)
                    }else{
                        hideData(false)
                    }
                }
            }
        })

        binding.layRequested.setOnClickListener(this)
        binding.layAccepted.setOnClickListener(this)
        binding.layDeclined.setOnClickListener(this)


    }

    private fun locationData(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }
    }

    private fun getCurrentLocation() {
        // Initialize Location manager
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // When location service is enabled
            // Get last location
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                // Initialize location
                val location = task.result
                // Check condition
                if (location != null) {
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    getAllConnectedAttorneyList(latitude, longitude, listOf())
                } else {
                    val locationRequest =
                        LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1)

                    val locationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            // Initialize
                            // location
                            val location1 = locationResult.lastLocation
                            latitude = location1!!.latitude.toString()
                            longitude = location1.longitude.toString()
                            getAllConnectedAttorneyList(latitude, longitude, listOf())
                        }
                    }
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        }
    }

    private fun hideData(status:Boolean){
        if (status){
            binding.rcvConnectionList.visibility = View.VISIBLE
            binding.textNoDataFound.visibility = View.GONE
        }else{
            binding.rcvConnectionList.visibility = View.GONE
            binding.textNoDataFound.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllConnectedAttorneyList(latitude: String, longitude: String,list: List<String>) {
        showMe()
        lifecycleScope.launch {
            val type = when {
                binding.RequestedLine.isVisible -> "3"
                binding.acceptLine.isVisible -> "1"
                binding.deniedLine.isVisible -> "2"
                else -> "3"
            }
            consumerHomeScreenViewModel.getAllAttorneyList(type, latitude, longitude, list)
                .observe(viewLifecycleOwner) { jsonObject ->
                   dismissMe()
                    binding.connectionRefresh.isRefreshing = false
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val attorneyListResp = Gson().fromJson(jsonObjectData, ConnectionsModel::class.java)
                            attronyList.clear()
                            attronyList.addAll(attorneyListResp.data)
                            if (attronyList.isNotEmpty()){
                                connectionAdapter = ConnectionAdapter(attorneyListResp.data, requireContext())
                                binding.rcvConnectionList.adapter = connectionAdapter
                                binding.rcvConnectionList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                                hideData(true)
                            }else{
                                hideData(false)
                            }
                        } catch (e: Exception) {
                            hideData(false)
                            Log.e("Error","******"+e.message)
                        }
                    }else{
                        hideData(false)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as ConsumerHomeActivity
        activity.Onselectcolor()
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.imageBackArrow -> {
               findNavController().navigateUp()
            }
            R.id.layRequested -> {
                selectDataType("Requested")
            }
            R.id.layAccepted -> {
                selectDataType("Accepted")
            }
            R.id.layDeclined -> {
                selectDataType("Declined")
            }
            R.id.btFilter -> {
                val bottomSheetFragment = BottomSheetAttorneyFilterDialog(this)
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }
        }
    }


    private fun selectDataType(selectType: String){
        hideData(false)
        sessionManager.setSelectType(selectType)
        if (selectType.equals("Requested",true)){
            binding.RequestedLine.visibility = View.VISIBLE
            binding.deniedLine.visibility = View.GONE
            binding.acceptLine.visibility = View.GONE
            binding.Requested.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            binding.Accepted.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light))
            binding.Declined.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light))
        }
        if (selectType.equals("Accepted",true)){
            binding.RequestedLine.visibility = View.GONE
            binding.deniedLine.visibility = View.GONE
            binding.acceptLine.visibility = View.VISIBLE
            binding.Accepted.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            binding.Declined.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light))
            binding.Requested.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light))
        }
        if (selectType.equals("Declined",true)){
            binding.RequestedLine.visibility = View.GONE
            binding.deniedLine.visibility = View.VISIBLE
            binding.acceptLine.visibility = View.GONE
            binding.Accepted.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light))
            binding.Declined.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            binding.Requested.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_light))
        }
        locationData()
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && (grantResults[0] + grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                displayLocationSettingsRequest(requireContext())
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
                }
            }
        }
    }

    private fun displayLocationSettingsRequest(context: Context) {
        val googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 1000
        locationRequest.numUpdates = 1
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.i(tAG, "All location settings are satisfied.")
                    getCurrentLocation()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    Log.i(tAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.resolution?.let {
                            startIntentSenderForResult(it.intentSender, 100, null, 0, 0, 0, null)
                        }

                    } catch (e: IntentSender.SendIntentException) {
                        Log.i(tAG, "PendingIntent unable to execute request."+e.message)
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(tAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")

            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun apply(address: List<Data>, practice: List<String>, practiceId: MutableList<String>) {
        address.firstOrNull()?.let {
            getAllConnectedAttorneyList(it.latitude, it.longitude, practice)
        } ?: run {
            getAllConnectedAttorneyList(latitude, longitude, practice)
        }
    }


}