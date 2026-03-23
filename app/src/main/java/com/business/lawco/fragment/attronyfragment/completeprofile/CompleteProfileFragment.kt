package com.business.lawco.fragment.attronyfragment.completeprofile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.IdentityActivity
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentProfileBinding
import com.business.lawco.model.consumer.AreaOfPractice
import com.business.lawco.model.consumer.CategoryModel
import com.business.lawco.networkModel.common.CommonViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.MediaUtility
import com.business.lawco.utility.ValidationData
import com.github.dhaval2404.imagepicker.ImagePicker
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class CompleteProfileFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentProfileBinding
    lateinit var sessionManager: SessionManager
    lateinit var commonViewModel: CommonViewModel
    private var imageFile: File? = null

    var lat = "0.0"
    var lng = "0.0"
    private var checked: Boolean? = false
    private var existingEmail = ""
    private var existingPhone = ""
    private var categoriesPageList = ArrayList<AreaOfPractice>()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var tAGProfile = "MyProfileFragment"

    private var tAG: String = "Location"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiKey = getString(R.string.map_api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
    }

    private fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY, fields
        ) /*.setCountry("IND")*/ //USA
            .build(requireActivity())
        someActivityResultLauncher.launch(intent)
    }

    var someActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            if (result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                 Log.i(tAGProfile, "Place: " + place.name + ", " + place.id + ", " + place.address);
                val address: String = place.address ?:""
                val latLng: LatLng = place.latLng!!
                binding.etLocation.text = address
                lat = latLng.latitude.toString()
                lng = latLng.longitude.toString()
            }
            // do query with address
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
            var status: Status? = null
            if (result.data != null) {
                status = Autocomplete.getStatusFromIntent(result.data!!)
            }
            assert(status != null)
            Toast.makeText(context, "Error: " + status!!.statusMessage, Toast.LENGTH_LONG).show()
            // Log.i(TAG, status.getStatusMessage());
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        sessionManager = context?.let { SessionManager(it) }!!
        commonViewModel = ViewModelProvider(requireActivity())[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager


        binding.etAreaOfPractice.setOnClickListener(this)
        binding.btSubmit.setOnClickListener(this)
        binding.uploadIcon.setOnClickListener(this)

        binding.etLocation.setOnClickListener {
            onSearchCalled()
        }

        if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        } else {
            commonViewModel.getProfile()?.let {
                showData(it)
            }?:run {
                getUserProfile()
            }
        }


        checkNumber()

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val enteredEmail = s.toString().trim()
                if (enteredEmail.isNotEmpty()){
                    binding.layEmail.visibility = View.VISIBLE
                    if (enteredEmail.equals(existingEmail, ignoreCase = true)) {
                        binding.emailVerify.visibility = View.VISIBLE
                        binding.emailVerifyClick.visibility = View.GONE
                    } else {
                        binding.emailVerify.visibility = View.GONE
                        binding.emailVerifyClick.visibility = View.VISIBLE
                    }
                }else{
                    binding.layEmail.visibility = View.GONE
                }

            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val enteredPhone = s.toString().trim()
                if (enteredPhone.isNotEmpty()){
                    binding.layPhone.visibility = View.VISIBLE
                    if (enteredPhone.equals(existingPhone, ignoreCase = true)) {
                        binding.phoneVerify.visibility = View.VISIBLE
                        binding.phoneVerifyClick.visibility = View.GONE
                    } else {
                        binding.phoneVerify.visibility = View.GONE
                        binding.phoneVerifyClick.visibility = View.VISIBLE
                    }
                }else{
                    binding.layPhone.visibility = View.GONE
                }

            }
            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.emailVerifyClick.setOnClickListener {
            if (!sessionManager.isNetworkAvailable()) {
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            } else {
                if (binding.etEmail.text.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_email))
                } else if (!ValidationData.emailValidate(binding.etEmail.text.toString())) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_valid_email_only))
                }else{
                    sendOtpEmailPhone("email")
                }
            }

        }

        binding.phoneVerifyClick.setOnClickListener {
            if (!sessionManager.isNetworkAvailable()) {
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            } else {
                if (binding.etPhone.text.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_phone))
                } else if (binding.etPhone.text.length != 10) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_valid_phone))
                } else{
                    sendOtpEmailPhone("phone")
                }
            }
        }

        locationData()

        return binding.root
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
                    lat = location.latitude.toString()
                    lng = location.longitude.toString()
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
                            lat = location1!!.latitude.toString()
                            lng = location1.longitude.toString()
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
        getAddressFromLocation(lat.toDouble(), lng.toDouble())
    }

    private fun getAddressFromLocation(
        latitude: Double,
        longitude: Double,
    ) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressParts = mutableListOf<String>()
                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }
                binding.etLocation.text = addressParts.joinToString(separator = "\n")
                Log.e("Address", address.toString())
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
        }
    }
    private fun checkNumber(){
        if (existingEmail.isNotEmpty()){
            if (binding.etEmail.text.toString().equals(existingEmail, ignoreCase = true)) {
                binding.emailVerify.visibility = View.VISIBLE
                binding.emailVerifyClick.visibility = View.GONE
            } else {
                binding.layEmail.visibility = View.GONE
                binding.emailVerify.visibility = View.GONE
                binding.emailVerifyClick.visibility = View.VISIBLE
            }
        }else{
            binding.layEmail.visibility = View.GONE
            binding.emailVerify.visibility = View.GONE
            binding.emailVerifyClick.visibility = View.VISIBLE
        }

        if (existingPhone.isNotEmpty()){
            if (binding.etPhone.text.toString().equals(existingPhone, ignoreCase = true)) {
                binding.phoneVerify.visibility = View.VISIBLE
                binding.phoneVerifyClick.visibility = View.GONE
            } else {
                binding.layPhone.visibility = View.GONE
                binding.phoneVerify.visibility = View.GONE
                binding.phoneVerifyClick.visibility = View.VISIBLE
            }
        }else{
            binding.layPhone.visibility = View.GONE
            binding.phoneVerify.visibility = View.GONE
            binding.phoneVerifyClick.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(requireContext(), IdentityActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun sendOtpEmailPhone(userSelect: String){
        showMe()
        lifecycleScope.launch {
            val  userInput = if (userSelect.equals("email",true)){
                binding.etEmail.text.toString().trim()
            }else{
                binding.etPhone.text.toString().trim()
            }

            val userType = if(sessionManager.getUserType().equals("attorney",true)) {
                "0" // attorney
            } else {
                "1" // consumer
            }

            commonViewModel.sendOtpEmailPhone(userInput,userType).observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                Log.e("Get Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        val otp = jsonObjectData.get("otp")?.asInt.toString()
                        Toast.makeText(requireContext(),""+otp, Toast.LENGTH_SHORT).show()
                        val bundle = Bundle()
                        bundle.putString("emailPhone",userInput)
                        findNavController().navigate(R.id.action_profileFragment_to_otpVerificationFragment3,bundle)
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btSubmit -> {
                if (checkValidation()) {
                    editUserProfile(imageFile, false)
                }
            }

            R.id.etAreaOfPractice -> {
                if (checked == true) {
                    checked = false
                    binding.relAreaOfPractice.visibility = View.GONE
                } else {
                    checked = true
                    if (categoriesPageList.isNotEmpty()){
                        showAreaCategory()
                    }else{
                        getAreaOfPractice()
                    }
                }
            }
            R.id.uploadIcon -> {
                browseCameraAndGallery()
            }
        }
    }

    // this function is used for open the menu for opening gallery of camera
    private fun browseCameraAndGallery() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose Image", "Cancel")
        val builder = AlertDialog.Builder(
            requireContext()
        )
        builder.setTitle("Choose File")
        builder.setItems(
            items
        ) { dialog: DialogInterface, item: Int ->
            if (items[item] == "Take Photo") {
                try {
                    cameraIntent()
                } catch (e: java.lang.Exception) {
                    Log.v("Exception", e.message!!)
                }
            } else if (items[item] == "Choose Image") {
                try {
                    galleryIntent()
                } catch (e: java.lang.Exception) {
                    Log.v("Exception", e.message!!)
                }
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    // this function is used for open the camera
    private fun cameraIntent() {
        ImagePicker.with(this)
            .cameraOnly()
            .maxResultSize(
                1080,
                1080)
            .start()
    }

    // this function is used for open the gallery
    private fun galleryIntent() {
        ImagePicker.with(this)
            .crop(150f, 150f)
            .galleryOnly()
            .compress(1024)
            .maxResultSize(
                1080,
                1080
            )
            .start()
    }

    @Deprecated("Deprecated in Java")
    // this function is used for to get image from camera or gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE) {
            data?.let {
                onSelectFromGalleryResultant(it)
            }
        }

        if (requestCode == 100) {
            if (Activity.RESULT_OK == resultCode) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_SHORT).show()
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

    // this function is used for to get image from gallery
    private fun onSelectFromGalleryResultant(data: Intent) {
        try {
            imageFile = File(MediaUtility.getPath(requireContext(), data.data!!))
            activity?.runOnUiThread {
                Glide.with(requireActivity())
                    .load(imageFile)
                    .apply(RequestOptions().error(R.drawable.demo_user))
                    .placeholder(R.drawable.demo_user)
                    .into(binding.profilePic)
               // binding.profilePic.setImageURI(data.data)
            }

            //editUserProfile(imageFile, true)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    // This function is used for check validation
    private fun checkValidation(): Boolean {
        if (binding.etName.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_name))
        } else if (binding.etEmail.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_email))
        } else if (binding.emailVerifyClick.visibility == View.VISIBLE) {
            sessionManager.alertErrorDialog(getString(R.string.email_verify))
        } else if (!ValidationData.emailValidate(binding.etEmail.text.toString())) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_email))
        } else if (binding.etPhone.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_phone))
        } else if (binding.etPhone.text.length != 10) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_phone))
        } else if (binding.phoneVerifyClick.visibility == View.VISIBLE) {
            sessionManager.alertErrorDialog(getString(R.string.Phone_verify))
        } else if (binding.etLocation.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_location))
        } else if (sessionManager.getUserType() == AppConstant.CONSUMER) {
            return true
        } else if (binding.etAreaOfPractice.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_area_practice))
        } else if (binding.etRegistrationNumber.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_registration_number))
        }/*else if (!ValidationData.isPhoneNumber(binding.etRegistrationNumber.text.toString())) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_email))
            binding.etRegistrationNumber.requestFocus()
        }*/
        else if (binding.etAbout.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_about))
        }/*else if (imageFile==null) {
            sessionManager.alertErrorDialog(getString(R.string.fill_profile))
        }*/
        else {
            return true
        }
        return false
    }

    // this function is used for edit attorney profile
    private fun editUserProfile(image: File?, onlyProfilePictureUpdate: Boolean) {
      showMe()
        val imageRequestBody = image?.asRequestBody("application/octet-stream".toMediaType())
        val imagePart = imageRequestBody?.let {
            MultipartBody.Part.createFormData("profile_picture", image.name, it)
        }
        val userType = if(sessionManager.getUserType().equals("attorney",true)) {
            "0" // attorney
        } else {
            "1" // consumer
        }
        lifecycleScope.launch {
            commonViewModel.editUserProfile(
                binding.etName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etPhone.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etEmail.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etLocation.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etAreaOfPractice.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etRegistrationNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                binding.etAbout.text.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                lat.toRequestBody("text/plain".toMediaTypeOrNull()),
                lng.toRequestBody("text/plain".toMediaTypeOrNull()),
                userType.toRequestBody("text/plain".toMediaTypeOrNull()),
                imagePart
            ).observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()

                val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)

                Log.e("Edit Profile", "True")

                if (jsonObjectData != null) {
                    try {
                        if (!onlyProfilePictureUpdate) {
                            sessionManager.setIsLogin(true)
                           // findNavController().navigate(R.id.action_profileFragment_to_allowLocationFragment)
                             val intent = Intent(requireActivity(), AttronyHomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }

            }
        }
    }

    // this function is used for to get Attorney profile
    private fun getUserProfile() {
        showMe()
        lifecycleScope.launch {
            commonViewModel.getUserProfile().observe(viewLifecycleOwner) { jsonObject ->
               dismissMe()
                Log.e("Get Profile", "True")
                val jsonObjectData = sessionManager.checkResponse(jsonObject)
                Log.e("Get Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        val data = jsonObjectData.getAsJsonObject("data")
                        commonViewModel.setUserProfile(data)
                        showData(data)
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }

    }

    private fun showData(data: JsonObject) {
        if (data != null) {
            try {
//                val data = jsonObjectData.getAsJsonObject("data")
                if(!data.get("name").isJsonNull){
                    binding.etName.setText( data.get("name").asString)
                }
                if(!data.get("email").isJsonNull){
                    binding.etEmail.setText( data.get("email").asString)
                    existingEmail=binding.etEmail.text.toString().trim()
                }
                if (!data.get("phone").isJsonNull) {
                    binding.etPhone.setText(data.get("phone").asString)
                    existingPhone=binding.etPhone.text.toString().trim()
                }
                if (!data.get("location").isJsonNull){

                    val setAddress: String? =data.get("location").asString
                    val maxLength = 34
                    if (setAddress!!.length > maxLength) {
                        val truncatedText = setAddress.take(maxLength) + "..."
                        binding.etLocation.text = truncatedText
                    }

//                            binding.etLocation.setText(data.get("location").asString)
                }
                if (!data.get("registered_by").isJsonNull) {
                    val registrationBy =
                        data.get("registered_by").asString
                    if (registrationBy == "email") {
                        binding.etPhone.isEnabled = true
                    } else {
                        binding.etEmail.isEnabled = true
                    }
                }

                if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                    binding.etAreaOfPractice.text  = if (!data.get("area_of_practice").isJsonNull) data.get(
                        "area_of_practice"
                    ).asString else null

                    binding.etRegistrationNumber.setText(
                        if (!data.get("registration_number").isJsonNull) data.get(
                            "registration_number"
                        ).asString else null
                    )
                    binding.etAbout.setText(if (!data.get("about").isJsonNull) data.get("about").asString else null)

                }
                if (!data.get("profile_picture_url").isJsonNull) {
                    val profileUrl =
                        data.get("profile_picture_url").asString
                    Glide.with(requireActivity())
                        .load(profileUrl)
                        .placeholder(R.drawable.untitled)
                        .into(binding.profilePic)
                }

                checkNumber()

            } catch (e: Exception) {
                Log.d("@Error","***"+e.message)
            }
        }
    }

    private fun getAreaOfPractice() {
        showMe()
        lifecycleScope.launch {
            commonViewModel.getAreaOfPractice()
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            categoriesPageList.clear()
                            val categoryList = Gson().fromJson(jsonObjectData, CategoryModel::class.java)
                            categoriesPageList = categoryList.data
                            showAreaCategory()
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }

                }
        }
    }

    private fun showAreaCategory(){
        val searchList: List<String> = categoriesPageList.map { it.category_name }
        binding.relAreaOfPractice.visibility = View.VISIBLE
        val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, searchList) }
        binding.listView.adapter = adapter
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter!!.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            binding.relAreaOfPractice.visibility = View.GONE
            checked = false
            binding.etAreaOfPractice.text = adapter!!.getItem(position)
        }
    }

}


