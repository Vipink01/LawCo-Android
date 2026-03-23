package com.business.lawco.fragment.attronyfragment.claimprofiledetails

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentClaimProfileDetailsBinding
import com.business.lawco.networkModel.claimProfile.ClaimProfileViewModel
import com.withpersona.sdk2.inquiry.Environment
import com.withpersona.sdk2.inquiry.Fields
import com.withpersona.sdk2.inquiry.Inquiry
import com.withpersona.sdk2.inquiry.InquiryResponse
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.toString
import androidx.core.view.isVisible
import com.business.lawco.utility.AppConstant


class ClaimProfileDetailsFragment : BaseFragment() {

    private lateinit var binding: FragmentClaimProfileDetailsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var claimProfileViewModel: ClaimProfileViewModel
    private lateinit var getInquiryResult: ActivityResultLauncher<Inquiry>
    private var userID: String =""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClaimProfileDetailsBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        claimProfileViewModel = ViewModelProvider(requireActivity())[ClaimProfileViewModel::class.java]


        getInquiryResult = registerForActivityResult(Inquiry.Contract()) { result ->
            when (result) {
                is InquiryResponse.Complete -> {
                    result.collectedData
                    Log.d("result","****"+result.fields)
                    Log.d("result","****"+result.inquiryId)
                    Log.d("result","****"+result.collectedData)
                    // User identity verification completed successfully
                     verifyUser()
                }
                is InquiryResponse.Cancel -> {
                    Toast.makeText(requireContext(),"Request Cancelled",Toast.LENGTH_LONG).show()
                }
                is InquiryResponse.Error -> {
                    Log.e("PersonaError", ""+result.errorCode+""+result.cause+""+result.debugMessage)
                    // Error occurred during identity verification
                    showVerificationNotCompletedDialog()
                }
            }
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the selected attorney LiveData
        claimProfileViewModel.selectedAttorney.observe(viewLifecycleOwner) { selected ->
            // 'selected' is the Data object you clicked
            if (selected != null) {
                if (selected.profile_picture != null) {
                    Glide.with(this)
                        .load(selected.profile_picture)
                        .placeholder(R.drawable.empty_profile_icon)
                        .error(R.drawable.empty_profile_icon)
                        .into(binding.profilePic)
                } else {
                    binding.profilePic.setImageResource(R.drawable.empty_profile_icon)
                }

                selected.full_name?.let { name->
                    binding.etName.setText(
                        name.trim().replaceFirstChar { it.uppercase() })
                }

                selected.email?.let { email->
                    binding.etEmail.setText(email)
                }

                selected.phone?.let { phone->
                    binding.etPhone.setText(""+phone)
                }

                selected.address?.let { address->
                    binding.etLocation.text = address
                }


                 selected.emailStatus?.let { emailStatus->
                     if (emailStatus){
                         binding.emailVerify.visibility = View.VISIBLE
                         binding.emailVerifyClick.visibility = View.GONE
                     }else{
                         binding.emailVerify.visibility = View.GONE
                         binding.emailVerifyClick.visibility = View.VISIBLE
                     }
                 }

                selected.phoneStatus?.let { phoneStatus ->
                    if (phoneStatus) {
                        binding.phoneVerify.visibility = View.VISIBLE
                        binding.phoneVerifyClick.visibility = View.GONE
                    } else {
                        binding.phoneVerify.visibility = View.GONE
                        binding.phoneVerifyClick.visibility = View.VISIBLE
                    }
                }

                selected.id?.let { id->
                    userID=id.toString()
                }
                selected.area_of_practice?.let { areaOfPractice->
                    binding.etAreaOfPractice.text = areaOfPractice
                }
            }
        }

        binding.emailVerifyClick.setOnClickListener {
            sendOtpEmailPhone("email")
        }

        binding.phoneVerifyClick.setOnClickListener {
            sendOtpEmailPhone("phone")
        }

        binding.btSubmit.setOnClickListener {
            if (!sessionManager.isNetworkAvailable()) {
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            } else {

                if (isValidation()){
//                      launchVerifyIdentity()
                    documentVerifyAlert()
                 }
            }
        }

    }


    private fun documentVerifyAlert(){
        val requestDialog = Dialog(requireContext())
        requestDialog.setContentView(R.layout.document_upload_dialog)
        requestDialog.setCancelable(false)
        requestDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val btnYes: TextView = requestDialog.findViewById(R.id.yes)
        val btnCancel: TextView = requestDialog.findViewById(R.id.Cancel)
        val imgClose: ImageView = requestDialog.findViewById(R.id.imgClose)
        val layDocument: TextView = requestDialog.findViewById(R.id.layDocument)
        val layWithOutDocument: TextView = requestDialog.findViewById(R.id.layWithOutDocument)
        var selectType="withOutDocument"
        layDocument.setOnClickListener {
            selectType="document"
            layDocument.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.select_box,
                0
            )
            layWithOutDocument.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.unselect_box,
                0
            )
        }
        layWithOutDocument.setOnClickListener {
            selectType="withOutDocument"
            layDocument.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.unselect_box,
                0
            )
            layWithOutDocument.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.select_box,
                0
            )
        }
        imgClose.setOnClickListener {
            requestDialog.dismiss()
        }
        btnYes.setOnClickListener {
            requestDialog.dismiss()
            launchVerifyIdentity(selectType)
        }
        btnCancel.setOnClickListener {
            requestDialog.dismiss()
        }
        requestDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        requestDialog.show()
    }


    private fun showVerificationSuccessDialog(token: String) {
        val successDialog = Dialog(requireContext())
        successDialog.setContentView(R.layout.alert_verification_success_dialog)
        successDialog.setCancelable(false)
        successDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnGoToProfile = successDialog.findViewById<TextView>(R.id.btnGoToProfile)

        btnGoToProfile.setOnClickListener {
            successDialog.dismiss()
            sessionManager.setBearerToken(token)
            sessionManager.setIsLogin(true)
            val intent = Intent(requireActivity(), AttronyHomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

        successDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        successDialog.show()

    }

    private fun showVerificationNotCompletedDialog() {
        val successDialog = Dialog(requireContext())
        successDialog.setContentView(R.layout.alert_verification_not_completed_dialog)
        successDialog.setCancelable(true)
        successDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnUpdateInformation = successDialog.findViewById<TextView>(R.id.btnUpdateInformation)

        btnUpdateInformation.setOnClickListener {
            successDialog.dismiss()

        }

        successDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        successDialog.show()
    }

    private fun verifyUser(){
        showMe()
        lifecycleScope.launch {
            claimProfileViewModel.personaVerifyUser(userID,"1").observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                Log.d("API_RESPONSE", jsonObject.toString())
                val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                Log.e("Get Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        val jsonObjectData = sessionManager.checkResponseHidemessage(jsonObject)
                        if (jsonObjectData != null) {
                            try {
                                Log.d("token","*****"+jsonObjectData["token"].asString)
                                Log.e("success", jsonObjectData.toString())
                                showVerificationSuccessDialog(jsonObjectData["token"].asString)
                            } catch (e: Exception) {
                                Log.d("@Error","***"+e.message)
                                showVerificationNotCompletedDialog()
                            }
                        }else{
                            showVerificationNotCompletedDialog()
                        }
                    } catch (e: Exception) {
                        showVerificationNotCompletedDialog()
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }
    }

    private fun sendOtpEmailPhone(userSelect: String){
        if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        } else {
            showMe()
            lifecycleScope.launch {
                val  userInput = if (userSelect.equals("email",true)){
                    binding.etEmail.text.toString().trim()
                }else{
                    binding.etPhone.text.toString().trim()
                }
                Log.d("userInput", "*****$userInput")
                claimProfileViewModel.sendOtpClaimEmailPhone(userInput,userID).observe(viewLifecycleOwner) { jsonObject ->
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
                            bundle.putString("userId",userID)
                            findNavController().navigate(R.id.action_claimProfileDetailsFragment_to_otpClaimVerificationFragment,bundle)
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
            }
        }
    }

    private fun launchVerifyIdentity(selectType: String) {

        val key = if (selectType.equals("document",true)){
            AppConstant.PERSONA_ID_KEY_DOCUMENT
        }else{
            AppConstant.PERSONA_ID_KEY
        }
        val inquiry = Inquiry.fromTemplate(key)
            .environment(Environment.SANDBOX)
            .referenceId(userID)
            .fields(Fields.Builder().build())
            .locale(Locale.getDefault().language)
            .build()
        getInquiryResult.launch(inquiry)
    }

    @SuppressLint("UseKtx")
    private fun isValidation(): Boolean{
        if (binding.emailVerifyClick.isVisible){
            sessionManager.alertErrorDialog(getString(R.string.email_verify))
            return false
        } else if (binding.emailVerifyClick.isVisible){
            sessionManager.alertErrorDialog(getString(R.string.Phone_verify))
            return false
        }

        return true
    }

}