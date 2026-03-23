package com.business.lawco.utility

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale
import java.util.regex.Pattern

object ValidationData {

    fun emailValidate(email : String) : Boolean {

        val pattern = """^(?:[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}|(?:\+?\d{1,4}[\s-]?)?(?:\(?\d{1,4}\)?[\s-]?)?\d{1,4}[\s-]?\d{1,4}[\s-]?\d{1,9})$"""
        val regex = Regex(pattern)

        if (regex.matches(email)) {
            return true
        } else {
            return false
        }

    }


    fun isEmail(input: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return input.matches(emailPattern)
    }

    fun isPhoneNumber(input: String): Boolean {
        val phoneNumberPattern = Regex("[0-9]+")
        return input.matches(phoneNumberPattern)
    }

    fun passCheck(pass : String) : Boolean {
        val passwordRegex = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$"
        val patternPass = Pattern.compile(passwordRegex)
        val password = patternPass.matcher(pass)
        return  password.matches()
    }


    @SuppressLint("DefaultLocale")
    fun formatDistance(distanceInMeters: Double): String {
       /* return if (distanceInMeters >= 1000) {
            String.format("%.2f km away", distanceInMeters / 1000)
        } else {
            String.format("%.0f meters away", distanceInMeters)
        }*/
        val value = String.format("%.2f meters away", distanceInMeters)
        return value
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
     fun getAddress(contaxt:Context,lat: Double, longi: Double): List<Address?>? {
        var addresses: List<Address?>? = null
        try {
            val geocoder = Geocoder(contaxt, Locale.getDefault())
            addresses = geocoder.getFromLocation(lat, longi, 1)
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return addresses
    }


}