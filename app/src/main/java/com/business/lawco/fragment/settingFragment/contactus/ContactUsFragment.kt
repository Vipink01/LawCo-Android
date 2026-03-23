package com.business.lawco.fragment.settingFragment.contactus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseFragment
import com.business.lawco.utility.ValidationData
import com.business.lawco.databinding.FragmentContactUsBinding
import com.business.lawco.networkModel.common.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactUsFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentContactUsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var commonViewModel: CommonViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactUsBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel

//        val callback: OnBackPressedCallback =
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().navigate(R.id.action_contactUsFragment_to_settingsFragment)
//                }
//            }
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        binding.arrowWhiteContact.setOnClickListener(this)
        binding.btSubmit.setOnClickListener(this)

    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.arrowWhiteContact -> {
             //   findNavController().navigate(R.id.action_contactUsFragment_to_settingsFragment)
                findNavController().navigateUp()
            }

            R.id.btSubmit -> {
                if (isValidation()) {
                    sendContactUs(
                        binding.etName.text.toString(),
                        binding.etEmail.text.toString(),
                        binding.etPhone.text.toString(),
                        binding.etMessage.text.toString()
                    )
                }
            }

        }
    }

    // This function is used for send contact us
    private fun sendContactUs(name: String, email: String, phone: String, message: String) {
        showMe()
        lifecycleScope.launch {
            commonViewModel.contactUs(name, email, phone, message)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            binding.etName.text.clear()
                            binding.etEmail.text.clear()
                            binding.etPhone.text.clear()
                            binding.etMessage.text.clear()
                            Toast.makeText(
                                requireContext(),
                                "Message Send Successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }


    }

    // This function is used for check validation on all field
    private fun isValidation(): Boolean {
        var valid = false
        if (binding.etName.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_name))
        } else if (binding.etEmail.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_emailOnly))
        } else if (!ValidationData.emailValidate(binding.etEmail.text.toString())) {
            sessionManager.alertErrorDialog(getString(R.string.fillValidEmailOnly))
        } else if (binding.etPhone.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_phone))
        } else if (binding.etPhone.text.length != 10) {
            sessionManager.alertErrorDialog(getString(R.string.fill_valid_phone))
        } else if (binding.etMessage.text.isEmpty()) {
            sessionManager.alertErrorDialog(getString(R.string.fill_message))
        } else if (!sessionManager.isNetworkAvailable()){
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        }else{
            valid = true
        }
        return valid
    }


}