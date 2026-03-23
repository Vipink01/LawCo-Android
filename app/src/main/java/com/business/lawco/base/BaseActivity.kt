package com.business.lawco.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.business.lawco.R
abstract class BaseActivity(): AppCompatActivity() {

    private var dialog: Dialog? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    fun showMe(): Dialog? {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val view: View =
            layoutInflater.inflate(R.layout.my_progress, null)
//        val mProgressTv = view.findViewById<TextView>(R.id.mProgressTv_ids)
//        mProgressTv.text = "Please wait..."
        dialog = Dialog(this, R.style.CustomProgressBarTheme)
        dialog?.setContentView(view)
        dialog?.show()
        return dialog
    }

    fun dismissMe() {
        if (dialog != null) dialog!!.dismiss()
    }



}