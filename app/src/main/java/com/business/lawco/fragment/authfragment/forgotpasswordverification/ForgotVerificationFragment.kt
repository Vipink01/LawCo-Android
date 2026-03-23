package com.business.lawco.fragment.authfragment.forgotpasswordverification

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ozcanalasalvar.otp_view.view.OtpView
import com.business.lawco.utility.AppConstant
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentForgotVerificationBinding
import com.business.lawco.networkModel.forgotPassword.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat


@AndroidEntryPoint
class ForgotVerificationFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentForgotVerificationBinding
    private var countDownTimer: CountDownTimer? = null
    private var reSendEnable = false
    lateinit var sessionManager: SessionManager
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel

    var otpValue: String = ""
    private var receiveOtp: String = ""
    var email: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotVerificationBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        forgotPasswordViewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]

        binding.forgotPasswordViewModel = forgotPasswordViewModel

        binding.textSubmit1.setOnClickListener(this)
        binding.imageBackButton.setOnClickListener(this)
        binding.textResend1.setOnClickListener(this)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    val bundle = Bundle()
                    bundle.putString(
                        AppConstant.EMAIL,
                        requireArguments().getString(AppConstant.EMAIL).toString()
                    )
                    findNavController().navigate(R.id.action_forgotVerificationFragment_to_forgotPasswordFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        try {
            receiveOtp = requireArguments().getString(AppConstant.OTP).toString()
        } catch (e: Exception) {
            Log.e("receiveOtp No argument", "Forget Verification")
        }

        try {
            email = requireArguments().getString(AppConstant.EMAIL).toString()
        } catch (e: Exception) {
            Log.e(" email No argument", "Forget Verification")
        }


        startCountDownTimer()
        countDownTimer!!.cancel()

        binding.textTimeResend1.text = "${"00"}:${"00"} sec"

        if (binding.textTimeResend1.text == "${"00"}:${"00"} sec") {
            reSendEnable = true
            binding.textResend1.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
        } else {
            binding.textResend1.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }


        binding.OtpView.setTextChangeListener(object : OtpView.ChangeListener {
            override fun onTextChange(value: String, completed: Boolean) {
                if (completed) {
                    otpValue = value
                }
            }
        })
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.textSubmit1 -> {
                if (otpValue.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.otp_empty))
                } else if (otpValue.length <= 3) {
                    sessionManager.alertErrorDialog(getString(R.string.otp_not_full))
                } else if (otpValue != receiveOtp) {
                    Log.e("Type Otp", otpValue)
                    Log.e("Recive Otp", receiveOtp)
                    binding.rlResendVerification.visibility = View.GONE
                    sessionManager.alertErrorDialog(getString(R.string.otp_valid))
//                    binding.errorText.visibility = View.VISIBLE
                    binding.textResend1.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orange
                        )
                    )
                } else {
                    val bundle = Bundle()
                    bundle.putString(AppConstant.EMAIL, email)
                    findNavController().navigate(
                        R.id.action_forgotVerificationFragment_to_resetPasswordFragment,
                        bundle
                    )
                }
            }

            R.id.imageBackButton -> {
                /* val bundle = Bundle()
                 bundle.putString(AppConstant.EMAIL, email)
                 findNavController().navigate(
                     R.id.action_forgotVerificationFragment_to_forgotPasswordFragment,
                     bundle
                 )*/
                findNavController().navigateUp()
            }

            R.id.textResend1 -> {
                if (reSendEnable) {

                    if (!sessionManager.isNetworkAvailable()) {
                        sessionManager.alertErrorDialog(getString(R.string.no_internet))
                    } else {
                        if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                            sendForgotOtp("0")
                        } else {
                            sendForgotOtp("1")
                        }
                    }
                }
            }
        }
    }

    // This function is use for check validation in all screen field
    private fun checkValidation(): Boolean {
        if (otpValue.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.otp_empty))
            return false
        } else if (otpValue.length <= 3) {
            sessionManager.alertErrorDialog(getString(R.string.otp_not_full))
            return false
        } else if (otpValue != receiveOtp) {
            Log.e("Type Otp", otpValue)
            Log.e("Recive Otp", receiveOtp)
            binding.rlResendVerification.visibility = View.GONE
            sessionManager.alertErrorDialog(getString(R.string.otp_valid))
//            binding.errorText.visibility = View.VISIBLE
            binding.textResend1.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.orange
                )
            )
            return false
        } else {
            return true
        }
    }

    // This function is use for start timer
    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f = DecimalFormat("00")
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                binding.textTimeResend1.text = "${f.format(min)}:${f.format(sec)} sec"
            }

            // When the task is over it will print 00:00 there
            @SuppressLint("SuspiciousIndentation", "SetTextI18n")
            override fun onFinish() {
                binding.textResend1.isEnabled = true
                binding.textTimeResend1.text = "00:00"
                binding.rlResendVerification.visibility = View.GONE
                if (binding.textTimeResend1.text == "00:00") {
                    reSendEnable = true
                    binding.textResend1.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orange
                        )
                    )
                } else {
                    binding.textResend1.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }
        }
        countDownTimer?.start()
    }

    // This function is use for send otp for password reset
    private fun sendForgotOtp(userType: String) {
        showMe()
        lifecycleScope.launch {
            forgotPasswordViewModel.sendForgotVerificationOtp(email, userType)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            binding.textResend1.isEnabled = false
                            binding.rlResendVerification.visibility = View.VISIBLE
                            binding.errorText.visibility = View.GONE
                            countDownTimer?.cancel()
                            startCountDownTimer()
                            binding.textResend1.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                            receiveOtp = jsonObjectData["otp"].asString
                            sessionManager.alertErrorDialog(getString(R.string.otp_Send))
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }

                    }

                }
        }

    }

}


