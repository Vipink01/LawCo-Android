package com.business.lawco.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.business.lawco.utility.AppConstant
import com.business.lawco.R
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.SessionManager
import com.business.lawco.databinding.FragmentAllowNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllowNotificationsFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentAllowNotificationsBinding
    private lateinit var sessionManager: SessionManager
    private var hasNotificationPermissionGranted = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val notificationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            } else {
                openNextFragment()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllowNotificationsBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textNotnow2.setOnClickListener(this)
        binding.btnNotification.setOnClickListener(this)
        sessionManager = context?.let { SessionManager(it) }!!

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.textNotnow2 -> {
                Log.e("User Type == ",sessionManager.getUserType().toString())
                openNextFragment()
            }
            R.id.btnNotification -> {
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    hasNotificationPermissionGranted = true
                    openNextFragment()
                }
            }
        }
    }

    private fun openNextFragment() {
        /*  if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
            findNavController().navigate(R.id.action_signUpVerificationFragment_to_profileFragment)
        } else {
            findNavController().navigate(R.id.action_signUpVerificationFragment_to_allowLocationFragment)
        }*/
        if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
           /* val intent = Intent(requireActivity(), AttronyHomeActivity::class.java)
            startActivity(intent)*/

            findNavController().navigate(R.id.action_allowNotificationsFragment_to_profileFragment)
        } else if (sessionManager.getUserType() == AppConstant.CONSUMER) {
            sessionManager.setIsLogin(true)
            val intent = Intent(requireActivity(), ConsumerHomeActivity::class.java)
            startActivity(intent)
        }else{
            Log.e("User Type == ",sessionManager.getUserType().toString())
        }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required. Please allow notification permission from settings.")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Alert")
            .setMessage("Notification permission is required to show notifications.")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



}
