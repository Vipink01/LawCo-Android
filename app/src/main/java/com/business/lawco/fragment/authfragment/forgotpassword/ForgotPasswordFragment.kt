package com.business.lawco.fragment.authfragment.forgotpassword

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentForgotPasswordBinding
import com.business.lawco.networkModel.forgotPassword.ForgotPasswordViewModel
import com.business.lawco.utility.AppConstant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentForgotPasswordBinding
    lateinit var sessionManager: SessionManager
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel
    var type:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotPasswordBinding.inflate(
            LayoutInflater.from(requireActivity()), container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        forgotPasswordViewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        binding.forgotPasswordViewModel = forgotPasswordViewModel

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                @SuppressLint("SuspiciousIndentation")
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        try {
            binding.etEmail.setText(requireArguments().getString(AppConstant.EMAIL).toString())
        }catch (e :Exception){
            Log.e("Email",e.toString())
        }

        binding.imageBack.setOnClickListener(this)
        binding.textBtSubmit.setOnClickListener(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.textBtSubmit -> {
                if (checkValidation()) {
                    if (sessionManager.getUserType() == AppConstant.ATTORNEY){
                        sendForgotOtp(binding.etEmail.text.toString(),"0")
                    }else{
                        sendForgotOtp(binding.etEmail.text.toString(),"1")
                    }
                }
            }

            R.id.imageBack -> {
                findNavController().navigate(R.id.action_forgotPasswordFragment_to_signInFragment)

            }
        }
    }

    // This function is use for check the validation of all field
    private fun checkValidation() : Boolean {
        if (binding.etEmail.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_email))
//            binding.etEmail.requestFocus()
            return false
        } else if (!validateEmailPhone()!!) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_email_phone))
            return false
        }
        /* else if (!ValidationData.emailValidate(binding.etEmail.text.toString())) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_email_phone))
            binding.etEmail.requestFocus()
            return false
        }*/ else if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
            return false
        } else {
            return true
        }
    }

    private fun validateEmailPhone(): Boolean? {

        val input: String = binding.etEmail.text.toString().trim()

        if (emailValidator(input)) {
            Patterns.EMAIL_ADDRESS.matcher(input).matches()
            type = "email"
            return true
        } else if (validateNumber(input)) {
            type = "mobile"
            return true
        }
        return false
    }


    private fun emailValidator(email: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun validateNumber(mobNumber: String): Boolean {
        //validates phone numbers having 10 digits (9998887776)
        return if (mobNumber.matches("\\d{10}".toRegex())) true else if (mobNumber.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}".toRegex())) true else if (mobNumber.matches(
                "\\d{4}[-\\.\\s]\\d{3}[-\\.\\s]\\d{3}".toRegex()
            )
        ) true else if (mobNumber.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}".toRegex())) true else if (mobNumber.matches(
                "\\(\\d{3}\\)-\\d{3}-\\d{4}".toRegex()
            )
        ) true else if (mobNumber.matches("\\(\\d{5}\\)-\\d{3}-\\d{3}".toRegex())) true else if (mobNumber.matches(
                "\\(\\d{4}\\)-\\d{3}-\\d{3}".toRegex()
            )
        ) true else false
    }


    // This function is use for send otp for password reset and open verification screen
    private fun sendForgotOtp(email: String , userType:String) {
        showMe()
        lifecycleScope.launch {
            forgotPasswordViewModel.sendForgotVerificationOtp(email,userType)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val bundle = Bundle()
                            bundle.putString(AppConstant.EMAIL, binding.etEmail.text.toString())
                            bundle.putString(AppConstant.OTP, jsonObjectData["otp"].asString)
                            findNavController().navigate(
                                R.id.action_forgotPasswordFragment_to_forgotVerificationFragment,
                                bundle
                            )
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }

                    }

                }

        }

    }

}


