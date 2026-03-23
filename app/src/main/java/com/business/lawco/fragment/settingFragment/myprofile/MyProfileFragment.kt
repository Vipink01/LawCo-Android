package com.business.lawco.fragment.settingFragment.myprofile


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentMyProfileBinding
import com.business.lawco.model.consumer.AreaOfPractice
import com.business.lawco.model.consumer.CategoryModel
import com.business.lawco.networkModel.common.CommonViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.MediaUtility
import com.business.lawco.utility.ValidationData
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.common.api.Status
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
import androidx.core.view.isVisible

@AndroidEntryPoint
class MyProfileFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentMyProfileBinding
    private lateinit var sessionManager: SessionManager
    private var editable = false
    private var categoriesPageList = ArrayList<AreaOfPractice>()
    lateinit var commonViewModel: CommonViewModel
    private var registrationBy: String? = null
    private var imageFile: File? = null
    private var imageurl: String=""

    var lat = "0.0"
    var lng = "0.0"
    private var tAGProfile = "MyProfileFragment"
    private var existingEmail = ""
    private var existingPhone = ""


    private var checked: Boolean? = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyProfileBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        sessionManager = SessionManager(requireContext())
        commonViewModel = ViewModelProvider(requireActivity())[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel
        val apiKey = getString(R.string.map_api_key)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        initView()
        return binding.root
    }
    private fun initView() {

        binding.etAreaOfPractice.setOnClickListener(this)
        binding.btEdit.setOnClickListener(this)
        binding.arrowWhite.setOnClickListener(this)
        binding.upload.setOnClickListener(this)

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

        binding.profilePic.setOnClickListener {
            alertProfilePic()
        }

        binding.etLocation.setOnClickListener {
            onSearchCalled()
        }

        if (sessionManager.getEditProfileStatus()){
            openProfileScreen()
        }else{
            openEditScreen()
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


        when (sessionManager.getUserType()) {
            AppConstant.ATTORNEY -> {
                binding.areaBox.visibility = View.VISIBLE
                binding.registerNumberBox.visibility = View.VISIBLE
                binding.About.visibility = View.VISIBLE
            }
            AppConstant.CONSUMER -> {
                binding.areaBox.visibility = View.GONE
                binding.registerNumberBox.visibility = View.GONE
                binding.About.visibility = View.GONE
            }
        }
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
                Log.d("API_RESPONSE", jsonObject.toString())
                val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                Log.e("Get Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        val otp = jsonObjectData.get("otp")?.asInt.toString()
                        Toast.makeText(requireContext(),""+otp, Toast.LENGTH_SHORT).show()
                        val bundle = Bundle()
                        bundle.putString("emailPhone",userInput)
                        when (sessionManager.getUserType()) {
                            AppConstant.ATTORNEY -> {
                                findNavController().navigate(R.id.action_myProfileFragment_to_otpVerificationFragment,bundle)
                            }
                            AppConstant.CONSUMER -> {
                                findNavController().navigate(R.id.action_myProfileFragment_to_otpVerificationFragment2,bundle)
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }
    }
    private fun alertProfilePic() {
        val alertDialog = Dialog(requireContext())
        alertDialog.setCancelable(true)
        alertDialog.setContentView(R.layout.profile_pic_alert)

        val imgClose: CardView = alertDialog.findViewById(R.id.imgclose)
        val imgLoad: ImageView = alertDialog.findViewById(R.id.imgload)

        val url = imageFile?.toString() ?: imageurl

        // Load image with Glide
        Glide.with(requireActivity())
            .load(url)
            .placeholder(R.drawable.demo_user)
            .error(R.drawable.demo_user)
            .into(imgLoad)
        imgClose.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }
    private fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
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
                 Log.i(tAGProfile, "Place: " + place.name + ", " + place.id + ", " + place.address)
                val address: String = place.address?:""
                val latLng: LatLng = place.latLng!!
                val setAddress:String=address
                Log.d("searchLocation", setAddress)
                binding.etLocation.text = setAddress
                lat = latLng.latitude.toString()
                lng = latLng.longitude.toString()
            }
            // do query with address
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            var status: Status? = null
            if (result.data != null) {
                status = Autocomplete.getStatusFromIntent(result.data!!)
            }
            assert(status != null)
            Toast.makeText(context, "Error: " + status!!.statusMessage, Toast.LENGTH_LONG)
                .show()
            // Log.i(TAG, status.getStatusMessage());
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.btEdit -> {
                if (editable) {
                    if (checkValidation()) {
                        editUserProfile()
                    }
                } else {
                    sessionManager.setEditProfileStatus(false)
                    openEditScreen()
                }
            }

            R.id.etAreaOfPractice -> {
                if (checked == true) {
                    checked = false
                    binding.relAreaOfPractice.visibility = View.GONE
                } else {
                    checked = true

                    if (categoriesPageList.isNotEmpty()){
                        showAreaOfPractice()
                    }else{
                        getAreaOfPractice()
                    }


                }
//                sessionManager.searchableAlertDialog(categoriesPageList.map { it.category_name } , binding.etAreaOfPractice)
            }
            R.id.arrowWhite -> {
               // findNavController().navigate(R.id.action_myProfileFragment_to_settingsFragment)
                findNavController().navigateUp()
            }
            R.id.upload -> {
                browseCameraAndGallery()
            }
        }
    }
    // this function is used for edit attorney profile
    private fun editUserProfile() {
        showMe()
        Log.e(" User Image", imageFile?.name.toString())
        val imageRequestBody = imageFile?.asRequestBody("application/octet-stream".toMediaType())
        val imagePart = imageRequestBody?.let {
            MultipartBody.Part.createFormData("profile_picture", imageFile?.name, it)
        }
        if (imagePart == null) {
            Log.e("No Profile Picture", "True")
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
                sessionManager.setEditProfileStatus(true)
                val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                Log.e("Edit Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        if (imageFile != null) {
                            Glide.with(requireActivity())
                                .load(/*AppConstant.BASE_URL+*/imageFile)
                                .into(binding.profilePic)
                        }
                        openProfileScreen()
                        Toast.makeText(requireContext(),jsonObjectData["message"].asString, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        sessionManager.alertErrorDialog(e.toString())
                    }
                }
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
            .crop(150f, 150f)
            .compress(1024)
            .maxResultSize(
                1080,
                1080
            )
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
    }
    // this function is used for to get image from gallery
    private fun onSelectFromGalleryResultant(data: Intent) {
        try {
            val uri = data.data
            if (uri != null) {
                imageFile = File(MediaUtility.getPath(requireContext(), uri))
                Glide.with(requireContext()).load(uri).into(binding.profilePic)
                //  binding.profilePic.setImageURI(uri)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    // this function is used for to get Attorney profile
    private fun getUserProfile() {
        showMe()
        lifecycleScope.launch {
            commonViewModel.getUserProfile().observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
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
        binding.etName.setText(if (!data.get("name").isJsonNull) data.get("name").asString else null)
        binding.etEmail.setText(if (!data.get("email").isJsonNull) data.get("email").asString else null)
        binding.etPhone.setText(if (!data.get("phone").isJsonNull) data.get("phone").asString else null)
        existingEmail=binding.etEmail.text.toString().trim()
        existingPhone=binding.etPhone.text.toString().trim()
        binding.etLocation.text = if (!data.get("location").isJsonNull) data.get("location").asString else null
        if (!data.get("location").isJsonNull) {
            val setAddress:String=data.get("location").asString
            val maxLength = 34
            if (setAddress.length > maxLength) {
                val truncatedText = setAddress.take(maxLength) + "..."
                binding.etLocation.text = truncatedText
            }
            lat = data.get("latitude").asString
            lng = data.get("longitude").asString
        }
        registrationBy = if (!data.get("registered_by").isJsonNull) data.get("registered_by").asString else null
        if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
            binding.etAreaOfPractice.text = if (!data.get("area_of_practice").isJsonNull) data.get("area_of_practice").asString else null
            binding.etRegistrationNumber.setText(if (!data.get("registration_number").isJsonNull) data.get("registration_number").asString else null)
            binding.etAbout.setText(if (!data.get("about").isJsonNull) data.get("about").asString else null)
        }
        if (!data.get("profile_picture_url").isJsonNull) {
            Log.d("*****", data.get("profile_picture_url").asString)
            imageurl=data.get("profile_picture_url").asString
            activity?.runOnUiThread {
                Glide.with(requireActivity())
                    .load(data.get("profile_picture_url").asString)
                    .apply(RequestOptions().error(R.drawable.demo_user))
                    .placeholder(R.drawable.demo_user)
                    .into(binding.profilePic)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun openProfileScreen() {
        editable = false
        binding.btEdit.text = "Edit"
        binding.textProfileHeader.text = "My Profile"
        binding.etName.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.etRegistrationNumber.isEnabled = false
        binding.etAreaOfPractice.isEnabled = false
        binding.etLocation.isEnabled = false
        binding.etAbout.isEnabled = false
        binding.upload.visibility = View.GONE
        binding.layEmail.visibility = View.GONE
        binding.layPhone.visibility = View.GONE
        binding.etAreaOfPractice.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }
    @SuppressLint("SetTextI18n")
    private fun openEditScreen() {
        editable = true
        binding.btEdit.text = "Submit"
        binding.textProfileHeader.text = "Edit Profile"
        binding.etName.isEnabled = true
        binding.etPhone.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etRegistrationNumber.isEnabled = true
        binding.etAreaOfPractice.isEnabled = true
        binding.etLocation.isEnabled = true
        binding.etAbout.isEnabled = true
        binding.upload.visibility = View.VISIBLE
        binding.layEmail.visibility = View.VISIBLE
        binding.layPhone.visibility = View.VISIBLE
        val newArrowResId = R.drawable.ic_arrow
        binding.etAreaOfPractice.setCompoundDrawablesWithIntrinsicBounds(0, 0, newArrowResId, 0)

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

    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.getUserType() == AppConstant.CONSUMER) {
            val activity = requireActivity() as ConsumerHomeActivity
            activity.profileResume()
        } else {
            val activity = requireActivity() as AttronyHomeActivity
            activity.profileColor()
        }
    }

    // This function is used for check validation
    @SuppressLint("UseKtx")
    private fun checkValidation(): Boolean {
        if (binding.etName.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_name))
        } else if (binding.etEmail.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_email))
        } else if (!ValidationData.emailValidate(binding.etEmail.text.toString())) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_email))
        } else if (binding.emailVerifyClick.isVisible) {
            sessionManager.alertErrorDialog(getString(R.string.email_verify))
        } else if (binding.etPhone.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_phone))
        } else if (binding.etPhone.text.length != 10) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_phone))
        } else if (binding.phoneVerifyClick.visibility == View.VISIBLE) {
            sessionManager.alertErrorDialog(getString(R.string.Phone_verify))
        }else if (binding.etLocation.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_location))
        } else if (sessionManager.getUserType() == AppConstant.CONSUMER) {
            return true
        } else if (binding.etAreaOfPractice.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_area_practice))
        } else if (binding.etRegistrationNumber.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_registration_number))
        } else if (binding.etAbout.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_about))
        } else if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        } else {
            return true
        }
        return false
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
                            showAreaOfPractice()
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }

                }
        }
    }


    private fun showAreaOfPractice(){
        val searchList: List<String> = categoriesPageList.map { it.category_name }
        binding.relAreaOfPractice.visibility = View.VISIBLE
        val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, searchList) }
        binding.listView.adapter = adapter
        binding.listView.emptyView = binding.tvNoData
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter!!.filter.filter(s)
                binding.listView.post {
                    if (adapter.count == 0) {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.listView.visibility = View.GONE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                        binding.listView.visibility = View.VISIBLE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            checked = false
            binding.relAreaOfPractice.visibility = View.GONE
            binding.etAreaOfPractice.text = adapter!!.getItem(position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setEditProfileStatus(true)
        commonViewModel.clearProfile()
    }

}