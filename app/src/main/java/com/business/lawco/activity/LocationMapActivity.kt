package com.business.lawco.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseActivity
import com.business.lawco.databinding.ActivityLocationMapBinding
import com.business.lawco.networkModel.common.CommonViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.ValidationData.getAddress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class LocationMapActivity : BaseActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityLocationMapBinding

    private var gMap: GoogleMap? = null

    private var addressId: String = ""
    var latitude: String? = null
    var longitude: String? = null
    var address: String? = null
    val DEFAULT_ZOOM = 14f


    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    binding.etSearch?.text = place.name
                    showmarkeronmap(place.getLatLng(),gMap!!)
                    Log.i(ContentValues.TAG, "Place: ${place.name}, ${place.id}"
                    )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i(ContentValues.TAG, "User canceled autocomplete")
            }
        }

    lateinit var sessionManager: SessionManager
    lateinit var commonViewModel: CommonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        binding = ActivityLocationMapBinding.inflate(LayoutInflater.from(this))
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel
        setContentView(binding.root)
        enableEdgeToEdge()
        applyInsets()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
/*
        val isDarkTheme =
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
        //WindowCompat.setDecorFitsSystemWindows(window, true)


        val apiKey = getString(R.string.map_api_key)
        if (intent.extras != null) {
            addressId = intent.getStringExtra(AppConstant.ADDRESS_ID).toString()
            latitude = intent.getStringExtra(AppConstant.LATITUDE)
            longitude = intent.getStringExtra(AppConstant.LONGITUDE)
        }


        Log.e("Latitude",latitude.toString())
        Log.e("longitude",longitude.toString())

        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }

        (supportFragmentManager.findFragmentById(R.id.fm_mapid) as SupportMapFragment?)?.getMapAsync(this)

        binding.etSearch.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this)
            startAutocomplete.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            val lan = try {
                latitude!!.toDouble()
            } catch (e: Exception) {
                0.0
            }

            val long = try {
                longitude!!.toDouble()
            } catch (e: Exception) {
                0.0
            }
            enterCompleteAddressPopup(lan, long, binding.tvAddress.text.toString())
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
           // googleMap.isMyLocationEnabled = true

            if (latitude != null && longitude != null) {
               val currentLatLng = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
                /*  gMap?.addMarker(MarkerOptions().position(currentLatLng).title("My Location"))
                   gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))*/
                showmarkeronmap(currentLatLng,gMap!!)
            } else {
                showCurrentLocation()
            }


        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {

                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()

                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    /* gMap?.addMarker(MarkerOptions().position(currentLatLng).title("My Location"))
                     Log.e("Location Is ", "Success")
                     gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))*/
                    showmarkeronmap(currentLatLng,gMap!!)
                } else {
                    Log.e("Location Is ", "Null")
                }
            }
    }

    @SuppressLint("SetTextI18n")
    fun showmarkeronmap(latLng: LatLng, googleMap: GoogleMap) {
        try {
            googleMap.clear()
            //  this.latLng = latLng;
            latitude = latLng.latitude.toString()
            longitude = latLng.longitude.toString()
            val markerOptions = MarkerOptions().position(latLng).title("I am here!")
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
            googleMap.addMarker(markerOptions)
           val fullAddress = getAddress(this,latLng.latitude, latLng.longitude)
            if (!fullAddress!!.isEmpty()) {
                val country: String = fullAddress.get(0)!!.getCountryName()
                val state: String = fullAddress.get(0)!!.getAdminArea()
                val address: String = fullAddress.get(0)!!.getAddressLine(0)
                binding.tvCity.setText("$country $state")
                binding.tvAddress.setText(address)
            }
        } catch (e: Exception) {
            Log.d("***** showmarkeronmap", e.message!!)
        }
    }


    private fun enterAddressPopup() {
        var bottomSheetDialog: BottomSheetDialog? = null

        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.enter_complete_address)
        bottomSheetDialog.show()

        val tvAddress: TextView = bottomSheetDialog.findViewById(R.id.tvAddress)!!
        val btnSubmit: TextView = bottomSheetDialog.findViewById(R.id.btn_submit)!!
        val tvCity: TextView = bottomSheetDialog.findViewById(R.id.tvCity)!!

        val lan = try {
            latitude!!.toDouble()
        } catch (e: Exception) {
            0.0
        }

        val long = try {
            longitude!!.toDouble()
        } catch (e: Exception) {
            0.0
        }

        val address = getAddressFromLocation(lan, long, tvAddress, tvCity)

        btnSubmit.setOnClickListener {
            bottomSheetDialog.dismiss()
            enterCompleteAddressPopup(lan, long, address)
        }
    }

    private fun enterCompleteAddressPopup(lat: Double, long: Double, address: String) {
        var bottomSheetDialog1: BottomSheetDialog? = null

        bottomSheetDialog1 = BottomSheetDialog(this, R.style.BottomSheetDialog)
        bottomSheetDialog1.setContentView(R.layout.enter_main_complete_address)
        bottomSheetDialog1.show()

        val llOthers: LinearLayout = bottomSheetDialog1.findViewById(R.id.llothers)!!
        val llHome: LinearLayout = bottomSheetDialog1.findViewById(R.id.llHome)!!
        val llOffice: LinearLayout = bottomSheetDialog1.findViewById(R.id.llOffice)!!
        val llHotel: LinearLayout = bottomSheetDialog1.findViewById(R.id.llHotel)!!
        val btnSave: TextView = bottomSheetDialog1.findViewById(R.id.btn_save)!!
        val imageCross1: ImageView = bottomSheetDialog1.findViewById(R.id.imageCross1)!!
        val addressFiled: TextInputEditText = bottomSheetDialog1.findViewById(R.id.addressFiled)!!

        addressFiled.setText(address)

        var addressType = "Home"

        llOthers.setOnClickListener {
            llOthers.setBackgroundResource(R.drawable.background_add)
            llHome.setBackgroundResource(R.drawable.background_card_edit1)
            llOffice.setBackgroundResource(R.drawable.background_card_edit1)
            llHotel.setBackgroundResource(R.drawable.background_card_edit1)
            addressType = "Other"
        }

        llHome.setOnClickListener {
            llOthers.setBackgroundResource(R.drawable.background_card_edit1)
            llHome.setBackgroundResource(R.drawable.background_add)
            llOffice.setBackgroundResource(R.drawable.background_card_edit1)
            llHotel.setBackgroundResource(R.drawable.background_card_edit1)

            addressType = "Home"
        }

        llOffice.setOnClickListener {
            llOthers.setBackgroundResource(R.drawable.background_card_edit1)
            llHome.setBackgroundResource(R.drawable.background_card_edit1)
            llOffice.setBackgroundResource(R.drawable.background_add)
            llHotel.setBackgroundResource(R.drawable.background_card_edit1)

            addressType = "Office"
        }

        llHotel.setOnClickListener {
            llOthers.setBackgroundResource(R.drawable.background_card_edit1)
            llHome.setBackgroundResource(R.drawable.background_card_edit1)
            llOffice.setBackgroundResource(R.drawable.background_card_edit1)
            llHotel.setBackgroundResource(R.drawable.background_add)

            addressType = "Hotel"
        }

        btnSave.setOnClickListener {
            bottomSheetDialog1.dismiss()
            saveAddress(lat.toString(), long.toString(), addressType,
                addressFiled.text.toString())
        }

        imageCross1.setOnClickListener {
            bottomSheetDialog1.dismiss()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressFromLocation(
        latitude: Double,
        longitude: Double,
        tvAddress: TextView,
        tvCity: TextView
    ): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var returnAddress = ""
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressParts = mutableListOf<String>()
                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }
                tvAddress.text = addressParts.joinToString(separator = "\n")

                Log.e("Address", address.toString())

                if (address.featureName != null) {
                    tvCity.text = address.featureName
                } else {
                    if (address.thoroughfare != null) {
                        tvCity.text = address.thoroughfare
                    } else {
                        if (address.subLocality != null) {
                            tvCity.text = address.subLocality
                        } else {
                            if (address.locality != null) {
                                tvCity.text = address.locality
                            } else {
                                if (address.adminArea != null) {
                                    tvCity.text = address.adminArea
                                }
                            }
                        }
                    }
                }

                returnAddress = addressParts.joinToString(separator = "\n")
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
        }

        return returnAddress
    }


    private fun saveAddress(
        latitude: String,
        longitude: String,
        addressType: String,
        address: String
    ) {

        Log.e("Address Type", addressType.toString())
        showMe()
        lifecycleScope.launch {
            commonViewModel.saveAddress(addressId, latitude, longitude, addressType, address)
                .observe(this@LocationMapActivity) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)

                    if (jsonObjectData != null) {
                        try {
                            finish()
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }

    }
    private fun applyInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->

            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,   // 👈 status bar height
                view.paddingRight,
                navBar.bottom
            )

            insets
        }
    }
}
