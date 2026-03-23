package com.business.lawco.model

data class SavedAddressModel(
    var message  : String,
    var success  : Boolean,
    var data : ArrayList<Address>,
)

data class Address(
    val id : String,
    val latitude : Double,
    val longitude : Double,
    val address : String,
    val address_type : String,
    val address_ty : String
)