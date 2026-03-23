package com.business.lawco.utility

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    private const val LOCATION_PERMISSION_REQUEST_CODE = 100


    // Function to check if location permissions are granted
    fun isLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request location permissions
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Function to handle the permission result
    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }


        }
    }


    fun isCallPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }



}
