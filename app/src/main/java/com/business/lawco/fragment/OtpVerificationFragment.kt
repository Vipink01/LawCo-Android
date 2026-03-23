package com.business.lawco.fragment

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.networkModel.common.CommonViewModel

@AndroidEntryPoint
class OtpVerificationFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentSignUpVerificationBinding
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var userOTP: String = ""
    private var receiveOTP: String = ""
    var emailPhone: String = ""
    lateinit var commonViewModel: CommonViewModel

    lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpVerificationBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        commonViewModel = ViewModelProvider(requireActivity())[CommonViewModel::class.java]

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.textSubmit.setOnClickListener(this)
        binding.textResend.setOnClickListener(this)
        binding.imageBack.setOnClickListener(this)
        binding.textResend.setOnClickListener(this)
        startCountDownTimer()
        countDownTimer!!.cancel()

        emailPhone = requireArguments().getString("emailPhone").toString()

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
                if (!sessionManager.isNetworkAvailable()) {
                    sessionManager.alertErrorDialog(getString(R.string.no_internet))
                } else {
                    if (userOTP.isEmpty()) {
                        sessionManager.alertErrorDialog(getString(R.string.otp_empty))
                    } else if (userOTP.length < 4) {
                        sessionManager.alertErrorDialog(getString(R.string.otp_not_full))
                    } else {
                        otpVerify()
                    }
                }
            }
            R.id.textResend -> {
                if (resendEnabled) {
                    if (!sessionManager.isNetworkAvailable()) {
                        sessionManager.alertErrorDialog(getString(R.string.no_internet))
                    } else {
                        resendEnabled = false
                        sendVerificationOtp()
                    }
                }
            }
            R.id.imageBack -> {
                findNavController().navigateUp()
            }
        }
    }

    private fun otpVerify(){
        showMe()
        lifecycleScope.launch {


            val userType = if(sessionManager.getUserType().equals("attorney",true)) {
             "0" // attorney
            } else {
              "1" // consumer
            }



            commonViewModel.otpEmailPhoneVerify(emailPhone,userType,userOTP).observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                val jsonObjectData = sessionManager.checkResponse(jsonObject)
                Log.e("Get Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        if (emailPhone.contains("@")){
                            commonViewModel.updateEmail(emailPhone)
                        }else{
                            commonViewModel.updatePhone(emailPhone)
                        }
                        findNavController().navigateUp()
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
        }
    }

    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f = DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                binding.textTimeResend.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.textResend.isEnabled = true
                binding.textTimeResend.text = "00:00"
                binding.rlResendLine.visibility = View.GONE
                if (binding.textTimeResend.text == "00:00") {
                    resendEnabled = true
                    binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                } else {
                    binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }
        }
        countDownTimer?.start()
    }

    // This function is used for send Sign Up verification Otp
    private fun sendVerificationOtp() {
        showMe()
        lifecycleScope.launch {
            val userType = if(sessionManager.getUserType().equals("attorney",true)) {
                "0" // attorney
            } else {
                "1" // consumer
            }
            commonViewModel.sendOtpEmailPhone(emailPhone,userType).observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                Log.e("Get Profile", "True")
                if (jsonObjectData != null) {
                    try {
                        val otp = jsonObjectData.get("otp")?.asInt.toString()
                        Toast.makeText(requireContext(),""+otp, Toast.LENGTH_SHORT).show()
                        binding.textResend.isEnabled = false
                        binding.rlResendLine.visibility = View.VISIBLE
                        binding.incorrectOtp.visibility = View.GONE
                        countDownTimer?.cancel()
                        startCountDownTimer()
                        binding.textResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                        receiveOTP = jsonObjectData["otp"].asString
                        Toast.makeText(requireContext(), getString(R.string.otp_Send), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }
            }
           }
    }

    override fun onStart() {
        super.onStart()
        toggleFooter(false)
    }

    override fun onStop() {
        super.onStop()
        toggleFooter(true)
    }

    private fun toggleFooter(show: Boolean) {
        when (sessionManager.getUserType()) {
            AppConstant.ATTORNEY ->
                (activity as? AttronyHomeActivity)?.attronyFooter(show)

            AppConstant.CONSUMER ->
                (activity as? ConsumerHomeActivity)?.consumerFooter(show)
        }
    }


}