package com.business.lawco.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.business.lawco.utility.AppConstant
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.activity.consumer.ConsumerHomeActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

     /*   val isDarkTheme =
            resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = !isDarkTheme
*/
        //WindowCompat.setDecorFitsSystemWindows(window, true)


        Handler(Looper.myLooper()!!).postDelayed({
            openNextScreen()
        }, 3000)
    }

    private fun openNextScreen() {
      //  if (SessionManager(this).getRememberMe() == 1) {
        if (SessionManager(this).getUserLogin()){
            if (SessionManager(this).getUserType() == AppConstant.ATTORNEY) {
                val intent = Intent(this, AttronyHomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, ConsumerHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            val intent = Intent(this, IdentityActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}