package com.business.lawco

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.business.lawco.activity.IdentityActivity
import com.business.lawco.model.consumer.RememberMe
import com.business.lawco.networkModel.BaseResponse
import java.io.IOException
import java.util.Locale
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.NavController
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SessionManager(var context: Context) {

    private var sharedPreferences: SharedPreferences =context.getSharedPreferences("login_session",
        Context.MODE_PRIVATE)
    private var sharedPreferences2: SharedPreferences =context.getSharedPreferences("remember_session",
        Context.MODE_PRIVATE)
    private var editor:SharedPreferences.Editor = sharedPreferences.edit()
    private var editor2:SharedPreferences.Editor = sharedPreferences2.edit()


    fun setUserType(userType: String) {
        editor.putString("user_type", userType)
        editor.apply()
    }

    fun getUserType():String{
        return sharedPreferences.getString("user_type","").toString()
    }

    fun setIsLogin(islogin: Boolean) {
        editor.putBoolean("isLogin", islogin)
        editor.apply()
    }
    fun getUserLogin():Boolean{
        return sharedPreferences.getBoolean("isLogin",false)
    }

    fun setUserLat(lat: String) {
        editor.putString("Lat", lat)
        editor.apply()
    }
    fun getUserLat():String{
        return sharedPreferences.getString("Lat","").toString()
    }

    fun getEditProfileStatus():Boolean{
        return sharedPreferences.getBoolean("editStatus",true)
    }

    fun setEditProfileStatus(status: Boolean){
        editor.putBoolean("editStatus", status)
        editor.apply()
    }


    fun setSelectType(value: String){
        editor.putString("SelectType", value)
        editor.apply()
    }
    fun setUserCurrent(current: Boolean) {
        editor.putBoolean("Current", current)
        editor.apply()
    }
    fun getUserCurrent():Boolean{
        return sharedPreferences.getBoolean("Current",false)
    }

    fun getSelectType(): String? {
        return sharedPreferences.getString("SelectType","Requested")
    }

    fun setUserLng(lng: String) {
        editor.putString("Lng", lng)
        editor.apply()
    }
    fun getUserLng():String{
        return sharedPreferences.getString("Lng","").toString()
    }

    fun setBearerToken(token: String) {
        editor.putString("BearerToken",  token)
        editor.apply()
    }

    fun getBearerToken():String{
        return sharedPreferences.getString("BearerToken","").toString()
    }


   /* fun setRememberMe(value : List<RememberMe>){
        editor2.putString("RememberMe", Gson().toJson(value))
        editor2.apply()
    }*/

    fun setRememberMe(value: RememberMe) {
        editor2.putString("RememberMe", Gson().toJson(value))
        editor2.apply()
    }

    fun getRememberMe(): String? {
        return sharedPreferences2.getString("RememberMe","")
    }

    fun  alertErrorDialog(msg:String?){
        val alertErrorDialog= Dialog(context)
        alertErrorDialog.setCancelable(false)
        alertErrorDialog.setContentView(R.layout.alertbox_error)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertErrorDialog.window!!.attributes)

        Log.e("Error Message",msg.toString())
        alertErrorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        alertErrorDialog.window!!.attributes = layoutParams

        val tvTitle: TextView =alertErrorDialog.findViewById(R.id.tv_title)
        val btnOk: TextView =alertErrorDialog.findViewById(R.id.btn_ok)
        tvTitle.text=msg

        btnOk.setOnClickListener {
            alertErrorDialog.dismiss()
        }

        alertErrorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertErrorDialog.show()
    }

    fun  alertSubscriptionDialog(msg:String?, onUpgradeClick: () -> Unit){
        val alertErrorDialog= Dialog(context)
        alertErrorDialog.setCancelable(true)
        alertErrorDialog.setContentView(R.layout.alertbox_error)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertErrorDialog.window!!.attributes)

        Log.e("Error Message",msg.toString())
        alertErrorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        alertErrorDialog.window!!.attributes = layoutParams

        val tvTitle: TextView =alertErrorDialog.findViewById(R.id.tv_title)
        val btnOk: TextView =alertErrorDialog.findViewById(R.id.btn_ok)
        val imgClose: ImageView =alertErrorDialog.findViewById(R.id.imgClose)
        val imgLogo: ImageView =alertErrorDialog.findViewById(R.id.imgLogo)

        imgClose.visibility = View.VISIBLE
        imgLogo.visibility = View.VISIBLE
        tvTitle.text=msg

        btnOk.text="Upgrade Plan"

        imgClose.setOnClickListener {
            alertErrorDialog.dismiss()
        }

        btnOk.setOnClickListener {
            alertErrorDialog.dismiss()
            onUpgradeClick()
        }

        alertErrorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertErrorDialog.show()
    }

    fun  sessionEndDialog(requireActivity : Activity, msg:String?){
        val sessionEndDialog= Dialog(context)
        sessionEndDialog.setCancelable(false)
        sessionEndDialog.setContentView(R.layout.alertbox_error)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(sessionEndDialog.window!!.attributes)

        sessionEndDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        sessionEndDialog.window!!.attributes = layoutParams
        val tvTitle: TextView =sessionEndDialog.findViewById(R.id.tv_title)
        val btnOk: TextView =sessionEndDialog.findViewById(R.id.btn_ok)
        tvTitle.text=msg

        btnOk.setOnClickListener {
            logOutAccount(requireActivity)
        }

        sessionEndDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        sessionEndDialog.show()
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    fun setFilterUserAddress(Address: String) {
        editor.putString("Address", Address)
        editor.apply()
    }
    fun getFilterUserAddress():String{
        return sharedPreferences.getString("Address","").toString()
    }

    fun setFilterPracticeChecked(id:String){
        editor.putString("Ids",id)
        editor.apply()
    }

    fun getFilterPracticeChecked(): String? {
        return sharedPreferences.getString("Ids","")
    }


    fun checkResponse(jsonObject : BaseResponse<JsonObject>): JsonObject? {
        if (!jsonObject.isIsError) {
            if (jsonObject.response != null) {
                try {
                    val jsonObjectData: JsonObject = jsonObject.response!!
                    val status = jsonObjectData["status"].asBoolean
                    if (status){
                        return jsonObjectData
                    }else{
                        Log.d("checkError2", jsonObjectData["message"].asString)
                       /* if (!jsonObjectData["message"].asString.equals("No requests found",true)){
                            alertErrorDialog(jsonObjectData["message"].asString)
                        }*/
                    }

                }catch (e:Exception){
                    Log.d("Error","******"+e.message)
                    alertErrorDialog("Unable to connect to the server. Please check your internet connection.")
                }

            }else{
                alertErrorDialog("Unable to connect to the server. Please check your internet connection.")
            }
        } else {
            Log.d("Error","******"+jsonObject.message)
        }
        return null
    }

    fun checkResponseShowMsg(jsonObject : BaseResponse<JsonObject>): JsonObject? {
        if (!jsonObject.isIsError) {
            if (jsonObject.response != null) {
                try {
                    val jsonObjectData: JsonObject = jsonObject.response!!
                    val status = jsonObjectData["status"].asBoolean
                    if (status){
                        return jsonObjectData
                    }else{
                        Log.d("checkError2", jsonObjectData["message"].asString)
                        alertErrorDialog(jsonObjectData["message"].asString)
                    }

                }catch (e:Exception){
                    Log.d("Error","******"+e.message)
                    alertErrorDialog("Unable to connect to the server. Please check your internet connection.")
                }

            }else{
                alertErrorDialog("Unable to connect to the server. Please check your internet connection.")
            }
        } else {
           alertErrorDialog(jsonObject.message)
        }
        return null
    }


    fun checkResponseHidemessage(jsonObject : BaseResponse<JsonObject>): JsonObject? {

        if (!jsonObject.isIsError) {
            if (jsonObject.response != null) {
                try {
                    val jsonObjectData: JsonObject = jsonObject.response!!
                    val status = jsonObjectData["status"].asBoolean

                    if (status){
                        return jsonObjectData
                    }else{
                     //   alertErrorDialog(jsonObjectData["message"].asString)
                    }

                }catch (e:Exception){
                    Log.d("Error","******"+e.message)
                    alertErrorDialog("Unable to connect to the server. Please check your internet connection.")
                }
            }else{
                alertErrorDialog("Unable to connect to the server. Please check your internet connection.")
            }
        } else {
            alertErrorDialog(jsonObject.message)
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    fun allowLocationAlertDialog() {
        val alertErrorDialog = Dialog(context)
        alertErrorDialog.setCancelable(false)
        alertErrorDialog.setContentView(R.layout.alertbox_error)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertErrorDialog.window!!.attributes)


        alertErrorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        alertErrorDialog.window!!.attributes = layoutParams

        val tvTitle: TextView = alertErrorDialog.findViewById(R.id.tv_title)
        val btnOk: TextView = alertErrorDialog.findViewById(R.id.btn_ok)
        tvTitle.text = "Please Allow The Location !"

        btnOk.setOnClickListener {
            alertErrorDialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }

        alertErrorDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertErrorDialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun allowCallPermissionAlertDialog() {
        val alertErrorDialog = Dialog(context)
        alertErrorDialog.setCancelable(false)
        alertErrorDialog.setContentView(R.layout.alertbox_error)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(alertErrorDialog.window!!.attributes)

        alertErrorDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        alertErrorDialog.window!!.attributes = layoutParams

        val tvTitle: TextView = alertErrorDialog.findViewById(R.id.tv_title)
        val btnOk: TextView = alertErrorDialog.findViewById(R.id.btn_ok)
        tvTitle.text = "Please Allow The Call Permission !"

        btnOk.setOnClickListener {
            alertErrorDialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
        alertErrorDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertErrorDialog.show()
    }


    fun logOutAccount(requireActivity : Activity){
        editor.clear()
        editor.apply()
        val intent = Intent(requireActivity, IdentityActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        requireActivity.startActivity(intent)
        requireActivity.finish()
    }

    fun getAddressFromLocation(
        latitude: Double,
        longitude: Double,
    ): String? {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressParts = mutableListOf<String>()
                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }

                return addressParts.joinToString(separator = "\n")
            }
        } catch (ioException: IOException) {
            alertErrorDialog(ioException.toString())
        } catch (illegalArgumentException: IllegalArgumentException) {
            alertErrorDialog(illegalArgumentException.toString())
        }

        return  null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateTimeSafe(input: String?): String {

        if (input.isNullOrBlank()) return ""

        return try {
            val instant = Instant.parse(input)
            val zoneId = ZoneId.systemDefault()
            val dateTime = instant.atZone(zoneId)
            val now = ZonedDateTime.now(zoneId)

            val daysDiff = ChronoUnit.DAYS.between(
                dateTime.toLocalDate(),
                now.toLocalDate()
            )

            val timeFormatter =
                DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

            val dateTimeFormatter =
                DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Locale.ENGLISH)

            when (daysDiff) {
                in 1..3 ->
                    "$daysDiff days ago ${dateTime.format(timeFormatter)}"

                0L ->
                    "Today ${dateTime.format(timeFormatter)}"

                else -> dateTime.format(dateTimeFormatter)
            }

        } catch (e: Exception) {
            ""
        }
    }

    fun searchableAlertDialog(searchList: List<String>, etAreaOfPractice: TextView)  {

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_searchable_spinner)

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val editText: EditText = dialog.findViewById(R.id.edit_text)
        val listView: ListView = dialog.findViewById(R.id.list_view)

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, searchList)
        listView.adapter = adapter

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            etAreaOfPractice.text = adapter.getItem(position)
            dialog.dismiss()
        }

    }


}