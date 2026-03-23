package com.business.lawco.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.utility.AppConstant
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentSettingsBinding
import com.business.lawco.networkModel.common.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : BaseFragment(), View.OnClickListener {
    lateinit var binding: FragmentSettingsBinding
    private lateinit var sessionManager: SessionManager
    lateinit var commonViewModel: CommonViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(
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

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val bundle = Bundle()
                    bundle.putString(AppConstant.SOURCE_FRAGMENT , AppConstant.SETTING)
                    findNavController().navigate(R.id.action_settingsFragment_to_homeFragment,bundle)
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.ProfileCl.setOnClickListener(this)
        binding.aboutCl.setOnClickListener(this)
        binding.SubscriptionCl.setOnClickListener(this)
        binding.PaymentCl.setOnClickListener(this)
        binding.TransactionsCl.setOnClickListener(this)
        binding.NotificationsCl.setOnClickListener(this)
        binding.ContactCl.setOnClickListener(this)
        binding.PrivacyCl.setOnClickListener(this)
        binding.TermsCl.setOnClickListener(this)
        binding.DeleteCl.setOnClickListener(this)
        binding.LogoutCl.setOnClickListener(this)
        binding.arrowWhite.setOnClickListener(this)

        when (sessionManager.getUserType()) {
            AppConstant.ATTORNEY -> {
                setVisibilityForAttorney()
            }

            AppConstant.CONSUMER -> {
                setVisibilityForConsumer()
            }
        }
    }

    override fun onClick(item: View?) {

        when (item!!.id) {

            R.id.ProfileCl -> {
                findNavController().navigate(R.id.action_settingsFragment_to_myProfileFragment)
            }

            R.id.aboutCl -> {
                findNavController().navigate(R.id.action_settingsFragment_to_aboutUsFragment)
            }

            R.id.PrivacyCl -> {
                findNavController().navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
            }

            R.id.TermsCl -> {
                val bundle = Bundle()
                bundle.putString(AppConstant.SOURCE_FRAGMENT , AppConstant.SETTING)
                findNavController().navigate(R.id.action_settingsFragment_to_termAndConditionFragment,bundle)
            }

            R.id.DeleteCl -> {
                deleteAccountAlertBox()
            }

            R.id.LogoutCl -> {
                logOutAlertBox()
            }

            R.id.SubscriptionCl -> {
                if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                    findNavController().navigate(R.id.action_settingsFragment_to_subscriptionsFragment)
                }
            }

            R.id.PaymentCl -> {
                val bundle = Bundle()
                bundle.putString(AppConstant.SOURCE_FRAGMENT, AppConstant.SETTING)
                findNavController().navigate(R.id.action_settingsFragment_to_paymentFragment, bundle)
            }

            R.id.TransactionsCl -> {
                findNavController().navigate(R.id.action_settingsFragment_to_transactionFragment)
            }

            R.id.NotificationsCl -> {
                findNavController().navigate(R.id.action_settingsFragment_to_notificationsFragment)
            }

            R.id.ContactCl -> {
                findNavController().navigate(R.id.action_settingsFragment_to_contactUsFragment)
            }

            R.id.arrowWhite -> {
                findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
            }
        }
    }

    private fun logOutAlertBox() {
        val logOutAlertDialog = Dialog(requireContext())
        logOutAlertDialog.setContentView(R.layout.log_out_alert_dialog)
        logOutAlertDialog.setCancelable(false)
        logOutAlertDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val yes: TextView = logOutAlertDialog.findViewById(R.id.yes)
        val no: TextView = logOutAlertDialog.findViewById(R.id.no)

        no.setOnClickListener {
            logOutAlertDialog.dismiss()
        }

        yes.setOnClickListener {
            logOutAlertDialog.dismiss()
            if (!sessionManager.isNetworkAvailable()){
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            }else{
                logOutUser()
            }
        }

        logOutAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logOutAlertDialog.show()
    }

    private fun deleteAccountAlertBox() {
        val deleteAccountDialog = Dialog(requireContext())
        deleteAccountDialog.setContentView(R.layout.delete_alert_dialog)
        deleteAccountDialog.setCancelable(true)
        deleteAccountDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val btYes: TextView = deleteAccountDialog.findViewById(R.id.yes)
        val btNo: TextView = deleteAccountDialog.findViewById(R.id.no)

        btNo.setOnClickListener {
            deleteAccountDialog.dismiss()
        }

        btYes.setOnClickListener {
            deleteAccountDialog.dismiss()
            if (!sessionManager.isNetworkAvailable()){
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            }else{
                deleteUser()
            }

        }

        deleteAccountDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteAccountDialog.show()
    }

    private fun setVisibilityForConsumer() {
        with(binding) {
            ProfileCl.isVisible = false
            aboutCl.isVisible = true
            SubscriptionCl.isVisible = false
            PaymentCl.isVisible = false
            TransactionsCl.isVisible = false
            NotificationsCl.isVisible = true
            ContactCl.isVisible = true
            PrivacyCl.isVisible = true
            TermsCl.isVisible = true
            DeleteCl.isVisible = true
            LogoutCl.isVisible = true
            viewPayment.isVisible = false
            viewSubscription.isVisible = false
            viewTransactions.isVisible = false
        }
    }

    private fun setVisibilityForAttorney() {
        with(binding) {
            ProfileCl.isVisible = false
            aboutCl.isVisible = true
            SubscriptionCl.isVisible = true
            PaymentCl.isVisible = false
            TransactionsCl.isVisible = true
            NotificationsCl.isVisible = true
            ContactCl.isVisible = true
            PrivacyCl.isVisible = true
            TermsCl.isVisible = true
            DeleteCl.isVisible = true
            LogoutCl.isVisible = true
        }
    }

    private fun logOutUser() {
        showMe()
        commonViewModel.isLoading.set(true)
        lifecycleScope.launch {
            commonViewModel.logOutAccount().observe(viewLifecycleOwner) { jsonObject ->
                commonViewModel.isLoading.set(false)
                dismissMe()
                val jsonObjectData = sessionManager.checkResponse(jsonObject)

                if (jsonObjectData != null) {
                    try {
                        sessionManager.logOutAccount(requireActivity())
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }

            }
        }
    }

    private fun deleteUser() {
        showMe()
        commonViewModel.isLoading.set(true)
        lifecycleScope.launch {
            commonViewModel.deleteAccount().observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                commonViewModel.isLoading.set(false)
                val jsonObjectData = sessionManager.checkResponse(jsonObject)

                if (jsonObjectData != null) {
                    try {
                        sessionManager.logOutAccount(requireActivity())
                    } catch (e: Exception) {
                        Log.d("@Error","***"+e.message)
                    }
                }

            }
        }


    }


    override fun onResume() {
        super.onResume()
        if (sessionManager.getUserType() == AppConstant.CONSUMER) {
            val activity = requireActivity() as ConsumerHomeActivity
            activity.settingsResume()
        } else if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
            val activity = requireActivity() as AttronyHomeActivity
            activity.settingsColor()
        }

    }


}