package com.business.lawco.networkModel.common

import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface CommonRepository {

    suspend fun getContent(type: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun contactUs(
        name: String,
        email: String,
        phone: String,
        message: String
    ): SingleLiveEvent<BaseResponse<JsonObject>>


    suspend fun getNotification(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun logOutAccount(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun deleteAccount(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>>


    suspend fun sendOtpEmailPhone(emailPhone: String, userType: String): SingleLiveEvent<BaseResponse<JsonObject>>
    suspend fun sendOtpClaimEmailPhone(emailPhone: String, userID: String): SingleLiveEvent<BaseResponse<JsonObject>>
    suspend fun personaVerifyUser(userID: String,status: String): SingleLiveEvent<BaseResponse<JsonObject>>
    suspend fun otpClaimEmailPhoneVerify(emailPhone: String, userID: String, otp: String): SingleLiveEvent<BaseResponse<JsonObject>>
    suspend fun searchAttorneyList(search: String): SingleLiveEvent<BaseResponse<JsonObject>>
    suspend fun otpEmailPhoneVerify(emailPhone: String, userType: String,userOTP: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun editUserProfile(
        name: RequestBody,
        phone: RequestBody,
        email: RequestBody,
        location: RequestBody,
        areaOfPractice: RequestBody,
        registrationNumber: RequestBody,
        about: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        type: RequestBody,
        profilePicture: MultipartBody.Part?
    ): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun changeNotificationStatus(notificationStatus: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getAddress(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun saveAddress(
        addressId: String,
        longitude: String,
        latitude: String,
        addressType: String,
        address: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun deleteAddress(addressId: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun setDefaultAddress(addressId: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getAreaOfPractice(): SingleLiveEvent<BaseResponse<JsonObject>>

}
