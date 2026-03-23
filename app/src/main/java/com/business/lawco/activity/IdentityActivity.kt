package com.business.lawco.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.business.lawco.utility.AppConstant
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.databinding.ActivityIdentityBinding

class IdentityActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityIdentityBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager= SessionManager(this)
        binding.rlConsumer.setOnClickListener (this)
        binding.rlAttorney.setOnClickListener (this)


        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.rlAttorney -> {
                binding.rlAttorney.setBackgroundResource(R.drawable.orange_button_identity)
                binding.imageAttorney.setImageResource(R.drawable.attorney_white)
                binding.textAttorney.setTextColor(Color.parseColor("#FFFFFF"))
                sessionManager.setUserType(AppConstant.ATTORNEY)
                val intent = Intent(this, OnBoardingActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.rlConsumer -> {
                binding.rlConsumer.setBackgroundResource(R.drawable.orange_button_identity)
                binding.imageConsumer.setImageResource(R.drawable.person_white)
                binding.textConsumer.setTextColor(Color.parseColor("#FFFFFF"))
                sessionManager.setUserType(AppConstant.CONSUMER)
                val intent = Intent(this, OnBoardingActivity::class.java)
                startActivity(intent)
                finish()
            }
        }}}
