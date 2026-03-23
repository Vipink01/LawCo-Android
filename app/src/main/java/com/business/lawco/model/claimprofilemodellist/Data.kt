package com.business.lawco.model.claimprofilemodellist

data class Data(
    val address: String?,
    val area_of_practice: String?,
    val email: String?,
    val full_name: String?,
    var id: Int?,
    val is_claimed: Int?,
    val latitude: String?,
    val longitude: String?,
    val phone: Long?,
    val profile_picture: String?,
    val emailStatus: Boolean?=false,
    val phoneStatus: Boolean?=false,
    val user_type: String?
)