package com.business.lawco.fragment.consumerfragment.filterAttrony

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.ImageUploadAdapter
import com.business.lawco.adapter.consumer.AttorneySearchAdapter
import com.business.lawco.adapter.consumer.SelectedAttorneyAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentSelectedAttorneyBinding
import com.business.lawco.fragment.filterDialog.BottomSheetAttorneyFilterDialog
import com.business.lawco.fragment.filterDialog.FilterApply
import com.business.lawco.model.consumer.AttorneyListDataModel
import com.business.lawco.model.consumer.AttorneyProfile
import com.business.lawco.model.consumer.Data
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeScreenViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.OnItemSelectListener
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class FilterAttorneyFragment : BaseFragment(), View.OnClickListener, FilterApply,OnItemSelectListener {
    lateinit var binding: FragmentSelectedAttorneyBinding
    private lateinit var adapterSelectedAttorney: SelectedAttorneyAdapter
    private var attorneyList: MutableList<AttorneyProfile> = arrayListOf()
    private lateinit var attorneySearchAdapter: AttorneySearchAdapter
    lateinit var sessionManager: SessionManager
    private lateinit var consumerHomeScreenViewModel: ConsumerHomeScreenViewModel

    private var addressList = ArrayList<String>()
    private var areaOfPracticeList = ArrayList<String>()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var latitude: String = "0"
    private var longitude: String = "0"
    private var tAG: String = "Location"
    private lateinit var rcyData: RecyclerView
    val uriList = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageUploadAdapter

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@registerForActivityResult

        val data = result.data
        val maxFiles = 5
        val maxSize = 10 * 1024 * 1024 // 10 MB

        // Handle multiple files
        data?.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) {
                if (uriList.size >= maxFiles) {
                    Toast.makeText(requireContext(), "Maximum 5 files allowed", Toast.LENGTH_SHORT).show()
                    break
                }
                val uri = clip.getItemAt(i).uri
                if (isFileSizeValid(uri, maxSize)) {
                    if (!uriList.contains(uri)) {
                        uriList.add(uri)
                    }
                } else {
                    Toast.makeText(requireContext(), "File exceeds 10 MB", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle single file
        data?.data?.let { uri ->
            if (uriList.size >= maxFiles) {
                Toast.makeText(requireContext(), "Maximum 5 files allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            if (isFileSizeValid(uri, maxSize)) {
                if (!uriList.contains(uri)) {
                    uriList.add(uri)
                }
            } else {
                Toast.makeText(requireContext(), "File exceeds 10 MB", Toast.LENGTH_SHORT).show()
            }
        }

        // Logs
        uriList.forEach { Log.e("uri", "****** $it") }

        // Recycler visibility
        rcyData.visibility = if (uriList.isNotEmpty()) View.VISIBLE else View.GONE
        imageAdapter.updateData(uriList)
    }

    /**
     * Check if the selected file size is less than maxSize
     */
    private fun isFileSizeValid(uri: Uri, maxSize: Int): Boolean {
        return try {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                it.moveToFirst()
                val size = it.getLong(sizeIndex)
                size <= maxSize
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectedAttorneyBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        consumerHomeScreenViewModel = ViewModelProvider(this)[ConsumerHomeScreenViewModel::class.java]
        binding.consumerHomeScreenViewModel = consumerHomeScreenViewModel


        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //findNavController().navigate(R.id.action_selectedAttorneyFragment_to_consumerHomeFragment)
                    findNavController().navigateUp()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        addressList = requireArguments().getStringArrayList(AppConstant.ADDRESS_LIST)!!
        areaOfPracticeList = requireArguments().getStringArrayList(AppConstant.AREA_OF_PRACTICE_LIST)!!

        binding.tvSelectedAttorney.text = requireArguments().getString(AppConstant.FILTER_PAGE_NAME)

        binding.EtSearch.hint = "Search an " + binding.tvSelectedAttorney.text

        Log.e("Select Address", addressList.toString())
        Log.e("Select Area of Practice", areaOfPracticeList.toString())

        binding.FilterRefreshList.setOnRefreshListener {
            getAllAttorneyList(latitude, longitude, addressList, areaOfPracticeList)
        }

        attorneySearchAdapter = AttorneySearchAdapter(attorneyList, requireContext(), AppConstant.FILTER)
        binding.recyclerAttorneySearch.adapter = attorneySearchAdapter
        binding.recyclerAttorneySearch.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.EtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(editTextAttorneySearch: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (editTextAttorneySearch != null) {
                    if (editTextAttorneySearch.isNotEmpty()) {
                        val filterList: ArrayList<AttorneyProfile> = arrayListOf()
                        attorneyList.forEach {
                            if (it.full_name.contains(editTextAttorneySearch.toString(), ignoreCase = true)) {
                                filterList.add(it)
                            }
                        }
                        if (filterList.isNotEmpty()){
                            adapterSelectedAttorney.updateData(filterList)
                            binding.rcvSelectedAttorney.visibility = View.VISIBLE
                            binding.textNoDataFound.visibility = View.GONE
                        }else{
                            binding.rcvSelectedAttorney.visibility = View.GONE
                            binding.textNoDataFound.visibility = View.VISIBLE
                        }
                    } else {
                        showList()
                    }
                } else {
                    showList()
                }
            }

            override fun afterTextChanged(editTextAttorneySearch: Editable?) {
            }

        })

        // set the attorney list from the api
        adapterSelectedAttorney = SelectedAttorneyAdapter(attorneyList, requireContext())
        binding.rcvSelectedAttorney.adapter = adapterSelectedAttorney
        binding.rcvSelectedAttorney.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapterSelectedAttorney.setOnSendRequest(object : SelectedAttorneyAdapter.OnSendRequest {
            override fun onSendRequest(position: Int, attorneyId: String, action: String) {
                sendRequest(position, attorneyId, action)
            }
        })

        binding.btBack.setOnClickListener(this)
        binding.btFilter.setOnClickListener(this)

        locationData()

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
                    handleNewLocation()
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
                            handleNewLocation()
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

    private fun handleNewLocation(){
        if (!sessionManager.getUserLat().isEmpty() && !sessionManager.getUserLng().isEmpty()){
            if (!sessionManager.getUserCurrent()){
                latitude = sessionManager.getUserLat()
                longitude = sessionManager.getUserLng()
            }
        }else{
            sessionManager.setUserCurrent(true)
            sessionManager.setUserLat(latitude.toString())
            sessionManager.setUserLng(longitude.toString())
        }
        getAllAttorneyList(latitude, longitude, listOf(), areaOfPracticeList)
    }

    private fun showList(){
        if (attorneyList.isNotEmpty()){
            adapterSelectedAttorney.updateData(attorneyList)
            binding.rcvSelectedAttorney.visibility = View.VISIBLE
            binding.textNoDataFound.visibility = View.GONE
        }else{
            binding.rcvSelectedAttorney.visibility = View.GONE
            binding.textNoDataFound.visibility = View.VISIBLE
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
                        Log.i(tAG, "PendingIntent unable to execute request.")
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


    override fun onResume() {
        super.onResume()
        binding.EtSearch.text.clear()
    }


    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.btBack -> {
                // findNavController().navigate(R.id.action_selectedAttorneyFragment_to_consumerHomeFragment)
                findNavController().navigateUp()
            }
            R.id.btFilter -> {
                val bottomSheetFragment = BottomSheetAttorneyFilterDialog(this)
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getAllAttorneyList(
        latitude: String, longitude: String,
        address: List<String>,
        areaOfPractice: List<String>
    ) {
        Log.e("LatLong", "$latitude ....... $longitude")
        showMe()
        lifecycleScope.launch {
            consumerHomeScreenViewModel.getAllAttorneyList(
                "0", latitude,
                longitude, areaOfPractice
            )
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    binding.FilterRefreshList.isRefreshing = false
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            attorneyList.clear()
                            val attorneyListResp = Gson().fromJson(jsonObjectData, AttorneyListDataModel::class.java)
                            attorneyList = attorneyListResp.data as MutableList
                            if (attorneyList.isNotEmpty()){
                                adapterSelectedAttorney.updateData(attorneyList)
                                attorneySearchAdapter.updateData(attorneyList, binding.searchView, binding.EtSearch)
                                binding.rcvSelectedAttorney.visibility = View.VISIBLE
                                binding.textNoDataFound.visibility = View.GONE
                            }else{
                                binding.rcvSelectedAttorney.visibility = View.GONE
                                binding.textNoDataFound.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            binding.rcvSelectedAttorney.visibility = View.GONE
                            binding.textNoDataFound.visibility = View.VISIBLE
                            Log.d("@Error","***"+e.message)
                        }
                    }else{
                        binding.rcvSelectedAttorney.visibility = View.GONE
                        binding.textNoDataFound.visibility = View.VISIBLE
                    }
                }
        }
    }

    @SuppressLint("SetTextI18n")
    fun sendRequest(position: Int, attorneyId: String, action: String) {
        val requestDialog = Dialog(requireContext())
        requestDialog.setContentView(R.layout.request_dialog)
        requestDialog.setCancelable(true)
        requestDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val etCardNumber: EditText = requestDialog.findViewById(R.id.etCardNumber)
        val etSubject: EditText = requestDialog.findViewById(R.id.etSubject)
        val btnYes: TextView = requestDialog.findViewById(R.id.yes)
        val btnCancel: TextView = requestDialog.findViewById(R.id.Cancel)
        rcyData = requestDialog.findViewById(R.id.rcyData)
        val imgUpload: ImageView = requestDialog.findViewById(R.id.imgUpload)
        val imgClose: ImageView = requestDialog.findViewById(R.id.imgClose)
        uriList.clear()
        imageAdapter=ImageUploadAdapter(uriList,requireContext(),this)
        rcyData.adapter= imageAdapter

        fun isValidation(): Boolean{
            if (etSubject.text.toString().isEmpty()){
                Toast.makeText(requireContext(),"Subject can't be empty.", Toast.LENGTH_SHORT).show()
                return false
            }else if (etCardNumber.text.toString().isEmpty()){
                Toast.makeText(requireContext(),"Description can't be empty.", Toast.LENGTH_SHORT).show()
                return false
            }else if (uriList.isEmpty()){
                Toast.makeText(requireContext(),"Documents can't be empty.", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }

        btnYes.setOnClickListener {
            if (isValidation()){
                sendRequestAttrony(requestDialog,position,attorneyId,action,etSubject.text.toString(),etCardNumber.text.toString())
            }

        }

        imgUpload.setOnClickListener {
            openGallery("5")
        }
        imgClose.setOnClickListener {
            requestDialog.dismiss()
        }
        btnCancel.setOnClickListener {
            requestDialog.dismiss()
        }
        requestDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        requestDialog.show()
    }

    private fun openGallery(selectType: String,format :String ="image") {
        AlertDialog.Builder(requireContext())
            .setTitle("Upload File")
            .setMessage("Choose a file type to upload")
            .setPositiveButton("Image") { _, _ ->
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.putExtra("SELECT_TYPE", selectType) // <<----- parameter
                imagePickerLauncher.launch(intent)
            }
            // PDF + DOC + DOCX
            .setNegativeButton("Document") { _, _ ->
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf(
                            "application/pdf",
                            "application/msword", // .doc
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
                        )
                    )
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                imagePickerLauncher.launch(intent)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun sendRequestAttrony(
        requestDialog: Dialog,
        position: Int,
        attorneyId: String,
        action: String,
        etSubject: String,
        etCardNumber: String
    ) {
        Log.e("Request Power", action)
        showMe()
        lifecycleScope.launch {
            val list = createMultipartFiles(uriList)
            consumerHomeScreenViewModel.sendRequestToAttorneyWithDoc(attorneyId, action, latitude, longitude,etSubject,etCardNumber,list)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        requestDialog.dismiss()
                        try {
                            val dataObject = jsonObjectData.getAsJsonObject("data")
                            if (action.toInt() == 0) {
                                attorneyList[position].request = 0
                            } else {
                                attorneyList[position].request = 1
                            }
                            attorneyList[position].subject =
                                if (dataObject != null && dataObject.has("subject") && !dataObject.get("subject").isJsonNull)
                                    dataObject.get("subject").asString
                                else
                                    null
                            attorneyList[position].description =
                                if (dataObject != null && dataObject.has("description") && !dataObject.get("description").isJsonNull)
                                    dataObject.get("description").asString
                                else
                                    null
                            val documentsList = mutableListOf<String>()
                            if (dataObject != null && dataObject.has("documents") && dataObject.get("documents").isJsonArray) {
                                dataObject.getAsJsonArray("documents").forEach {
                                    if (!it.isJsonNull) {
                                        documentsList.add(it.asString)
                                    }
                                }
                            }
                            attorneyList[position].documents = documentsList
                            adapterSelectedAttorney.notifyItemChanged(position)
                            alertSend()
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun alertSend(){
        val postDialog = Dialog(requireContext())
        postDialog.setContentView(R.layout.alert_dialog_successful_sign_up)
        postDialog.setCancelable(false)
        val submit: TextView = postDialog.findViewById(R.id.btn_okay)
        val tv2: TextView = postDialog.findViewById(R.id.tv2)
        tv2.text="Your request has been submitted \nsuccessfully."
        submit.setOnClickListener {
            postDialog.dismiss()
        }
        postDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        postDialog.show()
    }

    fun createMultipartFiles(uriList: MutableList<Uri>): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()

        uriList.forEachIndexed { index, uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileBytes = inputStream?.readBytes()
            val fileName = getFileName(uri)
            if (fileBytes != null && fileName != null) {
                val requestFile = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("files[]", fileName, requestFile)
                parts.add(part)
            }
        }

        return parts
    }

    // Utility function to get file name from Uri
    fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOpenableColumnName())
            }
        }
        return name
    }

    // Extension for cursor column name
    fun Cursor.getColumnIndexOpenableColumnName(): Int = getColumnIndex(OpenableColumns.DISPLAY_NAME)

    override fun apply(address: List<Data>, practice: List<String>, practiceId: MutableList<String>) {
        val (lat, lng) = if (address.isNotEmpty()) {
            address[0].latitude to address[0].longitude
        } else {
            latitude to longitude
        }
        if (practice.isNotEmpty()){
            getAllAttorneyList(lat, lng, emptyList(), practice)
        }else{
            getAllAttorneyList(lat, lng, emptyList(), areaOfPracticeList)
        }
    }


    override fun itemSelect(position: Int?, status: String?, type: String?) {
        uriList.removeAt(position?:0)
        if (uriList.isNotEmpty()){
            imageAdapter.updateData(uriList)
            rcyData.visibility = View.VISIBLE
        }else{
            rcyData.visibility = View.GONE
        }
    }


}