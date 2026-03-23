package com.business.lawco.fragment.attronyfragment.log

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.adapter.attroney.AcceptedAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentLogBinding
import com.business.lawco.model.RequestListModel
import com.business.lawco.networkModel.homeScreen.attorney.AttorneyHomeScreenViewModel
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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.adapter.ImageShowAdapter
import com.business.lawco.model.RequestData
import com.business.lawco.utility.OnItemSelectListener

@AndroidEntryPoint
class LogFragment : BaseFragment(), View.OnClickListener, OnItemSelectListener {
    lateinit var binding: FragmentLogBinding
    lateinit var sessionManager: SessionManager
    private lateinit var homeScreenViewModel: AttorneyHomeScreenViewModel


    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var latitude: String = "0"
    private var longitude: String = "0"
    private var tAG: String = "Location"
    private var lead:String="0"
    private var requestList: ArrayList<RequestData> = arrayListOf()

    private lateinit var adapterRequestList :AcceptedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        homeScreenViewModel = ViewModelProvider(this)[AttorneyHomeScreenViewModel::class.java]
        binding.homeScreenViewModel = homeScreenViewModel
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_logFragment_to_homeFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        binding.layRequested.setOnClickListener(this)
        binding.layAccepted.setOnClickListener(this)
        binding.layDeclined.setOnClickListener(this)
        binding.arrowWhite.setOnClickListener(this)

        binding.logRefresh.setOnRefreshListener {
            locationData()
        }

        if (sessionManager.getSelectType().equals("Requested")){
            selectDataType("Requested")
        }
        if (sessionManager.getSelectType().equals("Accepted")){
            selectDataType("Accepted")
        }
        if (sessionManager.getSelectType().equals("Declined")){
            selectDataType("Declined")
        }


    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.layRequested -> {
                selectDataType("Requested")
            }
            R.id.layAccepted -> {
                selectDataType("Accepted")
            }
            R.id.layDeclined -> {
                selectDataType("Declined")
            }
            R.id.arrowWhite -> {
                findNavController().navigate(R.id.action_logFragment_to_homeFragment)
            }
        }
    }


    private fun getCredits(position: Int?, status: String?, type: String?) {
        lifecycleScope.launch {
            homeScreenViewModel.getCredit().observe(viewLifecycleOwner) { jsonObject ->
                val jsonObjectData = sessionManager.checkResponseHidemessage(jsonObject)
                if (jsonObjectData != null) {
                    try {
                        lead=jsonObjectData["credit"].asInt.toString()
                    } catch (e: Exception) {
                        lead="0"
                        Log.d("@Error","***"+e.message)
                    } finally {
                        if (lead.toInt() > 0) {
                            showView(position,status,type)
                        } else {
                            sessionManager.alertSubscriptionDialog(getString(R.string.leadError)){
                                findNavController().navigate(
                                    R.id.action_logFragment_to_subscriptionsFragment
                                )
                            }
                        }
                    }
                }
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

        if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        } else {
            locationData()
        }


    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as AttronyHomeActivity
        activity.logResume()
    }
    private fun locationData(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }
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
                    apiCall()
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
                            apiCall()
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

    private fun apiCall(){
        showMe()
        val type = when {
            binding.RequestedLine.isVisible -> "0"
            binding.acceptLine.isVisible -> "1"
            binding.deniedLine.isVisible -> "2"
            else -> "0"
        }
        lifecycleScope.launch {
            homeScreenViewModel.getAllRequest(type, latitude, longitude)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    binding.logRefresh.isRefreshing= false
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val requestData = Gson().fromJson(jsonObjectData, RequestListModel::class.java)
                            if (requestData.data!=null && requestData.data!!.isNotEmpty()){
                                hideData(true)
                                showData(requestData.data!!,type)
                            }else{
                                hideData(false)
                            }
                        } catch (e: Exception) {
                            hideData(false)
                            Log.d("@Error","***"+e.message)
                        }
                    }else{
                        hideData(false)
                    }
                }
        }

    }

    private fun showData(data: ArrayList<RequestData>, type: String) {
        requestList.clear()
        requestList.addAll(data)
        adapterRequestList=AcceptedAdapter(requestList, requireActivity(),type,this)
        binding.requestListRecycleView.adapter = adapterRequestList
        binding.requestListRecycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun hideData(status:Boolean){
        if (status){
            binding.requestListRecycleView.visibility = View.VISIBLE
            binding.textNoDataFound.visibility = View.GONE
        }else{
            binding.requestListRecycleView.visibility = View.GONE
            binding.textNoDataFound.visibility = View.VISIBLE
        }
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        if (!sessionManager.getSelectType().equals("Requested")){
            showView(position,status,type)
        }else{
            getCredits(position,status,type)
        }


    }

    @SuppressLint("SetTextI18n")
    private fun showView(position: Int?, status: String?, type: String?) {
        val requestDialog = Dialog(requireContext())
        requestDialog.setContentView(R.layout.request_dialog)
        requestDialog.setCancelable(false)
        requestDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val etCardNumber: EditText = requestDialog.findViewById(R.id.etCardNumber)
        val etSubject: EditText = requestDialog.findViewById(R.id.etSubject)
        val btnYes: TextView = requestDialog.findViewById(R.id.yes)
        val tvDescription: TextView = requestDialog.findViewById(R.id.tvDescription)
        val tvTitle: TextView = requestDialog.findViewById(R.id.tvTitle)
        val tvUpload: TextView = requestDialog.findViewById(R.id.tvUpload)
        val btnCancel: TextView = requestDialog.findViewById(R.id.Cancel)
        val tvInfo: TextView = requestDialog.findViewById(R.id.tvInfo)
        val tvDownload: TextView = requestDialog.findViewById(R.id.tvDownload)
        val rcyData: RecyclerView = requestDialog.findViewById(R.id.rcyData)
        val imgUpload: ImageView = requestDialog.findViewById(R.id.imgUpload)
        val imgClose: ImageView = requestDialog.findViewById(R.id.imgClose)
        val btnShow: LinearLayout = requestDialog.findViewById(R.id.btnShow)
        tvDescription.text="Legal Problem Description"
        tvTitle.text="Request Details"
        tvUpload.text="Uploaded Documents"


        val dataItem = requestList.find { it.request_id.equals(status,true) }

        dataItem?.subject?.let {
            etSubject.setText(it)
        }
        dataItem?.description?.let {
            etCardNumber.setText(it)
        }


        dataItem?.documents?.let { list->
            if (list.isNotEmpty()){
                rcyData.adapter= ImageShowAdapter(requireContext(),list)
            }
        }

        if (type.equals("0",true)){
            tvInfo.text="Accepting will enable one on one chat and call access with the client."
            tvInfo.visibility = View.VISIBLE
            btnYes.text="Accept"
            btnCancel.text="Decline"
            btnShow.visibility = View.VISIBLE
        }else{
            tvInfo.visibility = View.GONE
            btnShow.visibility = View.GONE
        }


        imgUpload.visibility = View.GONE
        tvUpload.visibility = View.VISIBLE
        tvDownload.visibility = View.VISIBLE
        etCardNumber.isEnabled = false
        etSubject.isEnabled = false

        btnYes.setOnClickListener {
            if (!sessionManager.isNetworkAvailable()) {
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            } else {
                if (lead.toInt() > 0) {
                    requestAction(position!!, status!!, "accepted",requestDialog)
                } else {
                    sessionManager.alertSubscriptionDialog(getString(R.string.leadError)){
                        requestDialog.dismiss()
                        findNavController().navigate(
                            R.id.action_logFragment_to_subscriptionsFragment
                        )
                    }
                }
            }

        }

        btnCancel.setOnClickListener {
            if (!sessionManager.isNetworkAvailable()) {
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            } else {
                if (lead.toInt() > 0) {
                    requestAction(position!!, status!!, "rejected",requestDialog)
                } else {
                    sessionManager.alertSubscriptionDialog(getString(R.string.leadError)){
                        requestDialog.dismiss()
                        findNavController().navigate(
                            R.id.action_logFragment_to_subscriptionsFragment
                        )
                    }
                }
            }
        }
        imgClose.setOnClickListener {
            requestDialog.dismiss()
        }
        requestDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        requestDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun requestAction(
        position: Int,
        requestId: String,
        action: String,
        requestDialog: Dialog
    ) {
        showMe()
        lifecycleScope.launch {
            homeScreenViewModel.requestAction(requestId, action)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            requestList.removeAt(position)
                            adapterRequestList.notifyDataSetChanged()
                            if (requestList.isNotEmpty()){
                                binding.textNoDataFound.visibility = View.GONE
                                binding.requestListRecycleView.visibility = View.VISIBLE
                            }else{
                                binding.textNoDataFound.visibility = View.VISIBLE
                                binding.requestListRecycleView.visibility = View.GONE
                            }
                            if (action.equals("accepted",true)) {
                                lead = (lead.toInt() - 1).toString()
                            }
                            requestDialog.dismiss()
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }
    }


}