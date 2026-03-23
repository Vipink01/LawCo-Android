package com.business.lawco.activity.authactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.business.lawco.R
import com.business.lawco.databinding.ActivityAuthBinding
import com.business.lawco.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

/*        enableEdgeToEdge()
        applyInsets()*/
 /*       val isDarkTheme =
            resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
*/
   /*     if (isDarkTheme) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = !isDarkTheme
*/
       // WindowCompat.setDecorFitsSystemWindows(window, true)
    }
    private fun applyInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->

            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.setPadding(
                0,
                statusBarInsets.top,   // 👈 Status bar height automatically
                0,
                0
            )

            insets
        }
    }
}

