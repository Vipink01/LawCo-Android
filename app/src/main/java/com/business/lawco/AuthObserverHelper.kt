package com.business.lawco

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.business.lawco.activity.IdentityActivity
import com.business.lawco.activity.authactivity.AuthActivity
import com.stripe.android.customersheet.injection.CustomerSheetViewModelModule_Companion_ContextFactory.context

object AuthObserverHelper {


    fun observeAuthEvents(activity: AppCompatActivity, authRequiredLiveData: LiveData<Boolean>) {
        authRequiredLiveData.observe(activity) { isAuthRequired ->
            if (isAuthRequired == true) {
                Log.d("TESTING_Auth", "Response Error 23")
                activity.handleAuthEvent()
            }
        }
    }

    private fun AppCompatActivity.handleAuthEvent() {
        /*Toast.makeText(this,"Session Expired", Toast.LENGTH_LONG).show()
        val intent = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()*/

    }
}