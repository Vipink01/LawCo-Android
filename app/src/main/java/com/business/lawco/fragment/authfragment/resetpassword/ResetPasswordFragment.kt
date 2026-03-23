package com.business.lawco.fragment.authfragment.resetpassword

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.utility.AppConstant
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseFragment
import com.business.lawco.utility.ValidationData
import com.business.lawco.databinding.FragmentResetPasswordBinding
import com.business.lawco.networkModel.forgotPassword.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : BaseFragment() {
    lateinit var binding: FragmentResetPasswordBinding
    lateinit var sessionManager: SessionManager
    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentResetPasswordBinding.inflate(
            LayoutInflater.from(requireActivity()), container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        forgotPasswordViewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        binding.forgotPasswordViewModel = forgotPasswordViewModel
        showPassword()

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /*  val bundle = Bundle()
                      bundle.putString(
                          AppConstant.EMAIL,
                          requireArguments().getString(AppConstant.EMAIL).toString())
                      findNavController().navigate(
                          R.id.action_resetPasswordFragment_to_forgotVerificationFragment,bundle)  */
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.imageBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btSubmit.setOnClickListener {
            if (checkValidation()) {
                resetPassword(
                    requireArguments().getString(AppConstant.EMAIL).toString(),
                    binding.etCreatePassword.text.toString().trim(),
                    binding.etConfirmPassword.text.toString().trim()
                )
            }
        }
    }

    // This function is used for check validation in all filed
    private fun checkValidation(): Boolean {
        if (binding.etCreatePassword.text.toString().trim().isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_new_password))
            return false
        } else if (!ValidationData.passCheck(binding.etCreatePassword.text.toString().trim())) {
            sessionManager.alertErrorDialog(getString(R.string.password_validation_text))
            return false
        } else if (binding.etConfirmPassword.text.toString().trim().isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_confirm_new_password))
            return false
        } else if (binding.etCreatePassword.text.toString()
                .trim() != binding.etConfirmPassword.text.toString().trim()
        ) {
            sessionManager.alertErrorDialog(getString(R.string.password_not_match))
            return false
        } else if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
            return false
        } else {
            return true
        }
    }

    // This function is used for clickable eye button
    private fun showPassword() {

        binding.imgShowPass.setOnClickListener {
            binding.imgShowPass.visibility = View.GONE
            binding.imgHidePass.visibility = View.VISIBLE
            binding.etCreatePassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            binding.etCreatePassword.setSelection(
                binding.etCreatePassword.text.toString().trim().length
            )
        }

        binding.imgHidePass.setOnClickListener {
            binding.imgShowPass.visibility = View.VISIBLE
            binding.imgHidePass.visibility = View.GONE
            binding.etCreatePassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.etCreatePassword.setSelection(
                binding.etCreatePassword.text.toString().trim().length
            )
        }

        binding.imgHidePass1.setOnClickListener {
            binding.imgShowPass1.visibility = View.VISIBLE
            binding.imgHidePass1.visibility = View.GONE
            binding.etConfirmPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.etConfirmPassword.setSelection(
                binding.etConfirmPassword.text.toString().trim().length
            )

        }
        binding.imgShowPass1.setOnClickListener {
            binding.imgShowPass1.visibility = View.GONE
            binding.imgHidePass1.visibility = View.VISIBLE
            binding.etConfirmPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            binding.etConfirmPassword.setSelection(
                binding.etConfirmPassword.text.toString().trim().length
            )
        }
    }

    // This function is used for reset the password
    private fun resetPassword(
        emailOrPhone: String, password: String, passwordConfirmation: String
    ) {
        showMe()
        lifecycleScope.launch {
            forgotPasswordViewModel.resetPassword(emailOrPhone, password, passwordConfirmation)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            passwordChangeAlertBox()
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }

    // This function is used for open change password alert box
    private fun passwordChangeAlertBox() {
        val postDialog = Dialog(requireContext())
        postDialog.setContentView(R.layout.alert_dialog_password_change)
        postDialog.setCancelable(true)

        val submitBtn: TextView = postDialog.findViewById(R.id.pass_btn_okay)

        submitBtn.setOnClickListener {
            postDialog.dismiss()
            findNavController().navigate(R.id.action_resetPasswordFragment_to_signInFragment)
        }
        postDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        postDialog.show()

    }


}