package com.business.lawco.fragment.authfragment.signupverification

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ozcanalasalvar.otp_view.view.OtpView
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseFragment
import com.business.lawco.utility.AppConstant
import com.business.lawco.databinding.FragmentSignUpVerificationBinding
import com.business.lawco.networkModel.signUp.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.graphics.drawable.toDrawable
import com.google.firebase.messaging.FirebaseMessaging

@AndroidEntryPoint
class SignUpVerificationFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentSignUpVerificationBinding
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var userOTP: String = ""
    private var receiveOTP: String = ""
    var name: String = ""
    var email: String = ""
    var password: String = ""
    private var token: String = ""

    lateinit var sessionManager: SessionManager

    private lateinit var signViewModel: SignUpViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpVerificationBinding.inflate(
            LayoutInflater.from(requireActivity()), container, false
        )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        signViewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bundle = Bundle()
                bundle.putString(AppConstant.NAME, name)
                bundle.putString(AppConstant.EMAIL, email)
                bundle.putString(AppConstant.PASSWORD, password)
                findNavController().navigate(R.id.action_signUpVerificationFragment_to_signUpFragment, bundle)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.textSubmit.setOnClickListener(this)
        binding.textResend.setOnClickListener(this)
        binding.imageBack.setOnClickListener(this)
        binding.textResend.setOnClickListener(this)
        binding.imageBack.setOnClickListener(this)
//        startCountDownTimer()
//        countDownTimer!!.cancel()
        name = requireArguments().getString(AppConstant.NAME).toString()
        email = requireArguments().getString(AppConstant.EMAIL).toString()
        password = requireArguments().getString(AppConstant.PASSWORD).toString()
        receiveOTP = requireArguments().getString(AppConstant.OTP).toString()

        binding.textTimeResend.text = "${"00"}:${"00"} sec"

        if (binding.textTimeResend.text .equals("${"00"}:${"00"} sec")) {
            resendEnabled = true
            binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
        }
        else {
            binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }

        binding.OtpView.setTextChangeListener(object : OtpView.ChangeListener {
            override fun onTextChange(value: String, completed: Boolean) {
                userOTP = if (completed) {
                    value
                } else{
                    value
                }
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.textSubmit -> {
                if (userOTP.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.otp_empty))
                }
                else if (userOTP.length < 4) {
                    sessionManager.alertErrorDialog(getString(R.string.otp_not_full))
                }
                else {
                    if (userOTP == receiveOTP){
                        if (!sessionManager.isNetworkAvailable()) {
                            sessionManager.alertErrorDialog(getString(R.string.no_internet))
                        } else {
                            if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                                signUpAccount("0")
                            } else {
                                signUpAccount("1")
                            }
                        }
                    }
                    else {
                        sessionManager.alertErrorDialog(getString(R.string.otp_valid))
                    }
                }
            }
            R.id.textResend -> {
                if (resendEnabled) {
                    if (!sessionManager.isNetworkAvailable()) {
                        sessionManager.alertErrorDialog(getString(R.string.no_internet))
                    } else {
                        resendEnabled = false
                        sendVerificationOtp(email)
                    }
                }
            }
            R.id.imageBack -> {
                val bundle = Bundle()
                bundle.putString(AppConstant.NAME, name)
                bundle.putString(AppConstant.EMAIL, email)
                bundle.putString(AppConstant.PASSWORD, password)
                findNavController().navigate(R.id.action_signUpVerificationFragment_to_signUpFragment, bundle)
            }
        }

    }


    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f = android.icu.text.DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                binding.textTimeResend.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.textResend.isEnabled = true
                binding.textTimeResend.text = "00:00"
                binding.rlResendLine.visibility = View.GONE
                resendEnabled = true
                binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            }
        }
        countDownTimer?.start()
    }

    // This function is used for create the user account
    private fun signUpAccount(type: String) {
        showMe()
        lifecycleScope.launch {
            signViewModel.signUp(name, email, password, type)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            sessionManager.setBearerToken(jsonObjectData["token"].asString)
                            Log.e("Bearer Token", sessionManager.getBearerToken())
                            alertBox()
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }

    // This function is used for send Sign Up verification Otp
    private fun sendVerificationOtp(email: String) {
        showMe()
        lifecycleScope.launch {
            val userType =
                if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                    "0"
                } else {
                    "1"
                }
            signViewModel.signUpOtp(email,userType).observe(viewLifecycleOwner) { jsonObject ->
                    resendEnabled = true
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            binding.textResend.isEnabled = false
                            binding.rlResendLine.visibility = View.VISIBLE
                            binding.incorrectOtp.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer()
                            binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                            receiveOTP = jsonObjectData.get("otp")?.asString ?: ""
                            Toast.makeText(requireContext(), getString(R.string.otp_Send), Toast.LENGTH_SHORT).show()
                        }
                        catch (e :Exception){
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
           }
    }

    private fun alertBox() {
        val postDialog = Dialog(requireContext())
        postDialog.setContentView(R.layout.alert_dialog_successful_sign_up)
        postDialog.setCancelable(true)

        val submit: TextView = postDialog.findViewById(R.id.btn_okay)

        submit.setOnClickListener {
            postDialog.dismiss()
            findNavController().navigate(R.id.action_signUpVerificationFragment_to_allowLocationFragment)
        }

        postDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        postDialog.show()
    }

    override fun onStart() {
        super.onStart()
        getFcmToken()
    }
    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    token=task.result
                    Log.d("FCM", "FCM Token: ${task.result}")
                } else {
                    token="Fetching FCM token failed"
                    Log.e("FCM", "Fetching FCM token failed", task.exception)
                }
            }
    }

}