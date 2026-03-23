package com.business.lawco.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.business.lawco.R
import java.util.regex.Matcher
import java.util.regex.Pattern
import androidx.core.graphics.drawable.toDrawable

open class BaseFragment() : Fragment() {
    private var dialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    @SuppressLint("InflateParams")
    fun showMe(): Dialog? {
        if (!isAdded) return null
        if (dialog?.isShowing == true) {
            return dialog
        }
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.my_progress, null)
        dialog = Dialog(requireContext(), R.style.CustomProgressBarTheme).apply {
            setContentView(view)
            setCancelable(false)
            window?.apply {
                setDimAmount(0f)
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            }
            show()
        }
        return dialog
    }

    fun dismissMe() {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }
        dialog = null
    }

    open fun expiryValidator(email: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN = "(?:0[1-9]|1[0-2])/[0-9]{2}"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }

    override fun onDestroyView() {
        dismissMe()
        super.onDestroyView()
    }


}