package com.business.lawco.fragment.settingFragment.notification

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.attroney.NotificationAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentNotificationsBinding
import com.business.lawco.model.NotificationModel
import com.business.lawco.networkModel.common.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentNotificationsBinding
    private var notificationOn = true
    private lateinit var sessionManager: SessionManager
    lateinit var commonViewModel: CommonViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel
        binding.notificationIV.setOnClickListener(this)
        binding.arrowWhiteNotifications.setOnClickListener(this)
        if (!sessionManager.isNetworkAvailable()){
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        }else{
            startGetNotification()
        }
        binding.notificationRefresh.setOnRefreshListener {
            if (!sessionManager.isNetworkAvailable()){
                binding.notificationRefresh.isRefreshing = false
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            }else{
                startGetNotification()
            }
        }
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.arrowWhiteNotifications -> {
                findNavController().navigateUp()
//                if (sessionManager.getUserType() == AppConstant.CONSUMER) {
//                    findNavController().navigate(R.id.action_notificationsFragment_to_consumerHomeFragment)
//                } else {
//                    findNavController().navigate(R.id.action_notificationsFragment_to_attronyHomeFragment)
//                }
            }
            R.id.notificationIV -> {
                if (notificationOn) {
                    changeNotificationStatus("Inactive")
                } else {
                    changeNotificationStatus("Activate")
                }
            }
        }
    }

    private fun startGetNotification() {
        if (!sessionManager.isNetworkAvailable()) {
            binding.notificationRefresh.isRefreshing = false
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
            hideData(false)
        } else {
            getNotification()
        }
    }

    private fun getNotification() {
        showMe()
        lifecycleScope.launch {
            commonViewModel.getNotification().observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                binding.notificationRefresh.isRefreshing = false
                val jsonObjectData = sessionManager.checkResponse(jsonObject)
                if (jsonObjectData != null) {
                    try {
                        if (jsonObjectData.get("notificationStatus").asString == "Activate") {
                            notificationOn = true
                            binding.notificationIV.setImageResource(R.drawable.notification_icon)
                        } else {
                            notificationOn = false
                            binding.notificationIV.setImageResource(R.drawable.notification_off)
                        }
                        val notificationList = Gson().fromJson(jsonObjectData, NotificationModel::class.java)
                        binding.RcvNotification.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        if (notificationList.data.isNullOrEmpty()) {
                            hideData(false)
                        }else{
                            binding.RcvNotification.adapter = NotificationAdapter(notificationList.data, requireActivity())
                            hideData(true)
                        }
                    } catch (e: Exception) {
                        hideData(false)
                        binding.notificationRefresh.isRefreshing = false
                        Log.d("@Error","***"+e.message)
                    }
                }else{
                    hideData(false)
                    binding.notificationRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun hideData(status:Boolean){
        if (status){
            binding.textNoDataFound.visibility = View.GONE
            binding.RcvNotification.visibility = View.VISIBLE
        }else{
            binding.textNoDataFound.visibility = View.VISIBLE
            binding.RcvNotification.visibility = View.GONE
        }
    }

    private fun changeNotificationStatus(notificationStatus: String) {
        showMe()
        lifecycleScope.launch {
            commonViewModel.changeNotificationStatus(notificationStatus)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            if (notificationStatus == "Activate") {
                                notificationOn = true
                                binding.notificationIV.setImageResource(R.drawable.notification_icon)
                            } else {
                                notificationOn = false
                                binding.notificationIV.setImageResource(R.drawable.notification_off)
                            }
                        } catch (e: Exception) {
                            binding.notificationRefresh.isRefreshing = false
                            Log.d("@Error","***"+e.message)
                        }
                    }else{
                        binding.notificationRefresh.isRefreshing = false
                    }
                }
        }
    }

}