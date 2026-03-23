package com.business.lawco.fragment.consumerfragment.attronydetails

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.ImageShowAdapter
import com.business.lawco.adapter.ImageUploadAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentAttorneyDetailsBinding
import com.business.lawco.model.consumer.AttorneyProfile
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeScreenViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.OnItemSelectListener
import com.business.lawco.utility.PermissionUtils
import com.business.lawco.utility.ValidationData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


@AndroidEntryPoint
class AttorneyDetailsFragment : BaseFragment() , OnMapReadyCallback , View.OnClickListener,OnItemSelectListener{

    lateinit var binding: FragmentAttorneyDetailsBinding

    private var gMap: GoogleMap? = null

    private var connected : Int = 0
    private var declined : Int = 0
    private var requestSent : Int = 0

    lateinit var sessionManager: SessionManager
    private lateinit var consumerHomeScreenViewModel: ConsumerHomeScreenViewModel
    private var attorneyId : String = ""
    var phone :String = ""
    var attorneyDetail: AttorneyProfile?=null
    val uriList = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageUploadAdapter
    private lateinit var rcyData: RecyclerView


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttorneyDetailsBinding.inflate(LayoutInflater.from(requireActivity()) ,container , false)
        binding.mapFragment.onCreate(savedInstanceState)
        binding.mapFragment.getMapAsync(this)


        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        initView()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        sessionManager = SessionManager(requireContext())
        consumerHomeScreenViewModel = ViewModelProvider(this)[ConsumerHomeScreenViewModel::class.java]
        binding.consumerHomeScreenViewModel = consumerHomeScreenViewModel
        binding.aboutContentBox.visibility = View.VISIBLE
        binding.contactBox.visibility = View.GONE

        val arrayListJson = requireArguments().getString(AppConstant.ATTORNEY_PROFILE)
        val type = object : TypeToken<AttorneyProfile>() {}.type
        attorneyDetail =  Gson().fromJson(arrayListJson, type)

        Log.e("Attorney Detail",attorneyDetail.toString())
        Log.e("full_name Detail", attorneyDetail?.full_name
            ?.trim()
            ?.replaceFirstChar { it.uppercase() }
            ?: "")

        attorneyId = attorneyDetail?.id.toString()


        binding.tvAttorneyName.text =  attorneyDetail?.full_name
            ?.trim()
            ?.replaceFirstChar { it.uppercase() }
            ?: ""

        binding.tvAreaOfWork.text = attorneyDetail?.area_of_practice +" Attorney"

        binding.tvLocation.text =attorneyDetail?.address
        if (attorneyDetail?.distance != null){ binding.tvDistance.text = ValidationData.formatDistance(attorneyDetail?.distance!!.toDouble()) }
        binding.tvAbout.text = attorneyDetail?.about
        binding.tvPhone.text = attorneyDetail?.phone.toString()
        binding.tvEmail.text = attorneyDetail?.email
        binding.tvAddress.text = attorneyDetail?.address
        connected = attorneyDetail?.connected?:0
        declined = attorneyDetail?.declined?:0
        requestSent = attorneyDetail?.request!!
        phone = attorneyDetail?.phone.toString()

        if (attorneyDetail?.profile_picture_url!=null){
            Glide.with(requireActivity())
                .load( attorneyDetail?.profile_picture_url)
                .placeholder(R.drawable.demo_user) // jab tak image load ho rahi hai
                .error(R.drawable.demo_user)
                .into(binding.tvProfile)
        }else{
            binding.tvProfile.setImageResource(R.drawable.demo_user)
        }


        binding.btConnect.setOnClickListener(this)
        binding.btAbout.setOnClickListener(this)
        binding.btContact.setOnClickListener(this)
        binding.imageBack.setOnClickListener(this)
        binding.btnCall.setOnClickListener(this)
        binding.btnMessage.setOnClickListener(this)

        binding.zoomIn.setOnClickListener {
            gMap?.animateCamera(CameraUpdateFactory.zoomIn())
            Log.e("******","ZoomIn")
        }

        binding.zoomOut.setOnClickListener {
            gMap?.animateCamera(CameraUpdateFactory.zoomOut())
            Log.e("******","ZoomOut")
        }

        Log.e("Request Sent",requestSent.toString())
        Log.e("Connected ",connected.toString())


        if (connected == 1){
            binding.btConnect.visibility = View.GONE
            binding.callEnableBox.visibility = View.GONE
        }else{
            binding.btConnect.visibility = View.VISIBLE
            binding.callEnableBox.visibility = View.VISIBLE
        }

        if (attorneyDetail?.online_status == 1) {
            binding.showActive.visibility = View.VISIBLE
        }else{
            binding.showActive.visibility = View.GONE
        }

        viewDetail()

        binding.btnRequest.setOnClickListener {
            showView()
        }


    }

    @SuppressLint("SetTextI18n")
    private fun viewDetail(){
        if (declined == 1 ){
            connected=0
            binding.btConnect.visibility = View.GONE
            binding.callEnableBox.visibility = View.VISIBLE
        }else{
            if (connected == 1){
                binding.btConnect.visibility = View.GONE
                binding.callEnableBox.visibility = View.GONE
                binding.layRequest.visibility = View.VISIBLE
            }else{
                if (requestSent == 1){
                    binding.layRequest.visibility = View.VISIBLE
                    binding.btConnect.text = "Requested"
                } else{
                    binding.layRequest.visibility = View.GONE
                    binding.btConnect.text = "Connect"
                }
            }
        }

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

    private fun showView(){
        val requestDialog = Dialog(requireContext())
        requestDialog.setContentView(R.layout.request_dialog)
        requestDialog.setCancelable(true)
        requestDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val etCardNumber: EditText = requestDialog.findViewById(R.id.etCardNumber)
        val etSubject: EditText = requestDialog.findViewById(R.id.etSubject)
        val btnYes: TextView = requestDialog.findViewById(R.id.yes)
        val tvUpload: TextView = requestDialog.findViewById(R.id.tvUpload)
        val btnCancel: TextView = requestDialog.findViewById(R.id.Cancel)
        val tvInfo: TextView = requestDialog.findViewById(R.id.tvInfo)
        val tvDownload: TextView = requestDialog.findViewById(R.id.tvDownload)
        val rcyData: RecyclerView = requestDialog.findViewById(R.id.rcyData)
        val imgUpload: ImageView = requestDialog.findViewById(R.id.imgUpload)
        val imgClose: ImageView = requestDialog.findViewById(R.id.imgClose)
        val btnShow: LinearLayout = requestDialog.findViewById(R.id.btnShow)

        imgUpload.visibility = View.GONE
        tvInfo.visibility = View.GONE
        btnShow.visibility = View.GONE
        tvUpload.visibility = View.VISIBLE
        tvDownload.visibility = View.VISIBLE

        etCardNumber.isEnabled = false
        etSubject.isEnabled = false

        attorneyDetail?.subject?.let {
            etSubject.setText(it)
        }

        attorneyDetail?.description?.let {
            etCardNumber.setText(it)
        }

        attorneyDetail?.documents?.let { list->
            if (list.isNotEmpty()){
                rcyData.adapter= ImageShowAdapter(requireContext(),list)
            }
        }

        btnYes.setOnClickListener {
            requestDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            requestDialog.dismiss()
        }

        imgClose.setOnClickListener {
            requestDialog.dismiss()
        }

        requestDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requestDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapFragment.onDestroy()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        if (attorneyDetail?.latitude!=null && attorneyDetail?.longitude!=null) {
            val currentLatLng = LatLng(
                attorneyDetail?.latitude!!.toDouble(), attorneyDetail?.longitude!!.toDouble()
            )
            gMap?.addMarker(MarkerOptions().position(currentLatLng).title("My Location"))
            gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
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

    @SuppressLint("SetTextI18n")
    override fun onClick(item: View?) {

        when(item!!.id){
            R.id.imageBack->{
               findNavController().navigateUp()
            }

            R.id.btConnect->{
                if (requestSent == 1){
                    Toast.makeText(requireContext(),R.string.already_req, Toast.LENGTH_SHORT).show()
                }else{
                    sendRequest(attorneyId ,"1")
                }
            }

            R.id.btnCall -> {
                if (connected == 1){
                    callToAttorney()
                }
            }

            R.id.btnMessage -> {
                if (connected == 1){
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("sms:$phone")
                    startActivity(intent)
                }
            }

            R.id.btContact ->{
                if (connected == 1){
                    showContact()
                }
            }

            R.id.btAbout ->{

                showAbout()
            }
       }

    }

    private fun callToAttorney() {
        val dialIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phone")
        }

        if (PermissionUtils.isCallPermissionGranted(requireContext())) {
            startActivity(dialIntent)
        }else{
           sessionManager.allowCallPermissionAlertDialog()
        }

    }

    private fun showAbout(){
        binding.btAbout.setBackgroundResource(R.drawable.orange_button_identity)
        binding.btAbout.setTextColor(requireContext().getColor(R.color.white))
        binding.btContact.background = null
        binding.btContact.setTextColor(requireContext().getColor(R.color.inactive_text_color))
        binding.aboutContentBox.visibility = View.VISIBLE
        binding.contactBox.visibility = View.GONE
    }

    private fun showContact(){
        binding.btContact.setBackgroundResource(R.drawable.orange_button_identity)
        binding.btContact.setTextColor(requireContext().getColor(R.color.white))
        binding.btAbout.background = null
        binding.btAbout.setTextColor(requireContext().getColor(R.color.inactive_text_color))
        binding.aboutContentBox.visibility = View.GONE
        binding.contactBox.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun sendRequest(attorneyId: String, action: String, ) {
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
                sendRequestAttrony(requestDialog,attorneyId,action,etSubject.text.toString(),etCardNumber.text.toString())
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



    private fun sendRequestAttrony(
        requestDialog: Dialog,
        attorneyId: String,
        action: String,
        etSubject: String,
        etCardNumber: String
    ) {
        Log.e("Request Power", action)
        showMe()
        lifecycleScope.launch {
            val list = createMultipartFiles(uriList)
            consumerHomeScreenViewModel.sendRequestToAttorneyWithDoc(attorneyId, action, sessionManager.getUserLat(), sessionManager.getUserLng(),etSubject,etCardNumber,list)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        requestDialog.dismiss()
                        val jsonObjectData = sessionManager.checkResponse(jsonObject)
                        if (jsonObjectData != null) {
                            try {
                                if (action.toInt() == 1){
                                    binding.btConnect.text = "Requested"
                                    requestSent = 1
                                } else{
                                    binding.btConnect.text = "Connect"
                                    requestSent = 0
                                }
                                // Get data object
                                val dataObject = jsonObjectData.getAsJsonObject("data")
                                attorneyDetail?.subject =
                                    if (dataObject != null && dataObject.has("subject") && !dataObject.get("subject").isJsonNull)
                                        dataObject.get("subject").asString
                                    else
                                        null
                                attorneyDetail?.description =
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
                                attorneyDetail?.documents = documentsList
                                viewDetail()
                                alertSend()
                            } catch (e: Exception) {
                                Log.d("@Error","***"+e.message)
                            }
                        }
                    }
                }
        }
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