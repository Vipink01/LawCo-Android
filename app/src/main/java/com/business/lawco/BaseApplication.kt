package com.business.lawco

import android.app.Application
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.HiltAndroidApp
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(this)
        keyHash()
    }

    private fun keyHash(){
        try {
            val info = packageManager.getPackageInfo(
                "com.business.lawco",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }

}