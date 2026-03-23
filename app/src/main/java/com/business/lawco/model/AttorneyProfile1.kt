package com.business.lawco.model


import com.google.gson.annotations.SerializedName

data class AttorneyProfile(
    val profileId: String,
    val fullName: String,
    val attorneyType: String,
    val city: String,
    val state: String,
    val profileImage: Int?= null,
    val isClaimed: Boolean
)

/*
// Main response model
data class AttorneyProfileListModel(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ArrayList<AttorneyProfile>
)

// Attorney Profile model
data class AttorneyProfile(
    @SerializedName("id")
    val profileId: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("attorney_type")
    val attorneyType: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("profile_image")
    val profileImage: String,

    @SerializedName("is_claimed")
    var isClaimed: Boolean = false,

    @SerializedName("law_firm_name")
    val lawFirmName: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("years_of_experience")
    val yearsOfExperience: Int? = null
)*/
