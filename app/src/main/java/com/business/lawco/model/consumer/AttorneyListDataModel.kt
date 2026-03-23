package com.business.lawco.model.consumer

import com.google.gson.annotations.SerializedName

data class AttorneyListDataModel(
    val data: List<AttorneyProfile>,
    val status: Boolean
)

data class AttorneyProfile(
    val connected: Int,
    var request: Int,
    var declined: Int,
    val profile_picture_url :String?,
    val full_name: String,
    val address: String,
    val phone: Long,
    val registration_number: String,
    val about: String,
    val online_status: Int,
    val area_of_practice: String,
    val id: Long,
    val email: String,
    val longitude: String?,
    var subject: String?,
    var description: String?,
    val latitude: String?,
    val request_updated_at: String?,
    val request_updated_time: String?,
    val distance: String?,
    var documents: MutableList<String>
)


data class ConnectionsModel(
    val data: List<ConnectionsDataModel>,
    val status: Boolean
)

data class ConnectionsDataModel(
    val about: String,
    @SerializedName("request_updated_at")
    val accepted_request_updated_at: String?,
    @SerializedName("request_updated_time")
    val accepted_request_updated_time: String?,
    val address: String,
    val area_of_practice: String?,
    val connected: Int,
    val declined: Int,
    val distance: Double?,
    val email: String,
    val full_name: String?,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val online_status: Int,
    val phone: Long,
    val profile_picture: String,
    val subject: String?,
    val description: String?,
    val profile_picture_url: String?,
    val registration_number: String,
    val documents: MutableList<String>,
    val request: Int
)

