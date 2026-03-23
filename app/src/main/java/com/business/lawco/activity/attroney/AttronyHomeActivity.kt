package com.business.lawco.activity.attroney


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.business.lawco.AppContextProvider
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.databinding.ActivityHomeBinding
import com.business.lawco.networkModel.SessionEventBus
import com.business.lawco.networkModel.common.CommonViewModel
import com.stripe.android.customersheet.injection.CustomerSheetViewModelModule_Companion_ResourcesFactory.resources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttronyHomeActivity : AppCompatActivity() ,View.OnClickListener{
    lateinit var binding: ActivityHomeBinding
    lateinit var commonViewModel: CommonViewModel
    private var isDialogShown = false
    private var downloadId: Long = 0


    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            Toast.makeText(
                this@AttronyHomeActivity,
                "Download Completed",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

       // enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        applyInsets()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
 /*       applyInsets()
        val isDarkTheme =
            resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true*/

        //WindowCompat.setDecorFitsSystemWindows(window, true)

        sessionManager = SessionManager(this)
        AppContextProvider.initialize(this)
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.navigationHome.setOnClickListener(this)
        binding.navigationlog.setOnClickListener(this)
        binding.navigationSettings.setOnClickListener(this)
        binding.navigationProfile.setOnClickListener(this)
      //  window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color)
//      AuthObserverHelper.observeAuthEvents(this, AuthEventManager.authRequired)
        observeSessionExpiration()
        // Register receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun observeSessionExpiration() {
        lifecycleScope.launch {
            SessionEventBus.sessionExpiredFlow.collectLatest {
                if (!isDialogShown) {
                    isDialogShown = true
                    showSessionExpiredDialog()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSessionExpiredDialog(){
        val dialog= Dialog(this, R.style.BottomSheetDialog)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alertbox_error)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = layoutParams
        val tvTitle: TextView =dialog.findViewById(R.id.tv_title)
        val btnOk: TextView =dialog.findViewById(R.id.btn_ok)
        tvTitle.text="Your session has expired. Please log in again to continue."
        btnOk.setOnClickListener {
            dialog.dismiss()
            val sessionManagement = SessionManager(this)
            // Clear user session
            sessionManagement.logOutAccount(this)
        }
        dialog.show()
    }

    fun settingsColor(){
        binding.imageHome.setImageResource(R.drawable.home_bottom1)
        binding.imagelog.setImageResource(R.drawable.log_bottom)
        binding.imageSettings.setImageResource(R.drawable.setting_bottom1)
        binding.imageProfile.setImageResource(R.drawable.profile_bottom)

    }
    fun profileColor(){
        binding.imageHome.setImageResource(R.drawable.home_bottom1)
        binding.imagelog.setImageResource(R.drawable.log_bottom)
        binding.imageSettings.setImageResource(R.drawable.setting_bottom)
        binding.imageProfile.setImageResource(R.drawable.profile_bottom1)

    }
    fun logResume(){
        binding.imageHome.setImageResource(R.drawable.home_bottom1)
        binding.imagelog.setImageResource(R.drawable.bottom_log1)
        binding.imageSettings.setImageResource(R.drawable.setting_bottom)
        binding.imageProfile.setImageResource(R.drawable.profile_bottom)

    }
    fun homeResume(){
        binding.imageHome.setImageResource(R.drawable.home_bottom)
        binding.imagelog.setImageResource(R.drawable.log_bottom)
        binding.imageSettings.setImageResource(R.drawable.setting_bottom)
        binding.imageProfile.setImageResource(R.drawable.profile_bottom)
    }

    fun attronyFooter(status: Boolean){
        if (status){
            binding.lay1.visibility= View.VISIBLE
        }else{
            binding.lay1.visibility= View.GONE
        }
    }
    override fun onClick(view: View?) {
        when(view!!.id){
            R.id.navigationHome ->{
                homeResume()
                findNavController(R.id.fragmentUserContainerView_main).navigate(R.id.attronyHomeFragment)
            }
            R.id.navigationlog ->{
                sessionManager.setSelectType("Requested")
                logResume()
                findNavController(R.id.fragmentUserContainerView_main).navigate(R.id.logFragment)
            }
            R.id.navigationSettings -> {
                settingsColor()
                commonViewModel.clearProfile()
                sessionManager.setEditProfileStatus(true)
                findNavController(R.id.fragmentUserContainerView_main).navigate(R.id.settingsFragment)
            }
            R.id.navigationProfile ->{
                profileColor()
                commonViewModel.clearProfile()
                sessionManager.setEditProfileStatus(true)
                findNavController(R.id.fragmentUserContainerView_main).navigate(R.id.myProfileFragment)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        SessionManager(this).setUserLat("")
        SessionManager(this).setUserLng("")
        SessionManager(this).setFilterUserAddress("")
        unregisterReceiver(downloadReceiver)
    }

    private fun applyInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->

            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,   // 👈 status bar height
                view.paddingRight,
                navBar.bottom
            )

            insets
        }
    }

}


