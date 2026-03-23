package com.business.lawco.networkModel.common

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class CommonViewModel @Inject constructor(private val commonRepository: CommonRepository) :
    ViewModel() {

    var isLoading = ObservableBoolean(false)

    private val _userProfileData = MutableLiveData<JsonObject?>()
    val userProfileData: LiveData<JsonObject?>
        get() = _userProfileData

    fun setUserProfile(data: JsonObject?) {
        _userProfileData.value = data
    }

    fun getProfile(): JsonObject? {
        return _userProfileData.value
    }

    fun updateEmail(newEmail: String) {
        val currentProfile = _userProfileData.value
        if (currentProfile != null) {
            val updatedProfile = currentProfile.deepCopy()
            updatedProfile.addProperty("email", newEmail)
            _userProfileData.value = updatedProfile
        }
    }

    fun updatePhone(newPhone: String) {
        val currentProfile = _userProfileData.value
        if (currentProfile != null) {
            val updatedProfile = currentProfile.deepCopy()
            updatedProfile.addProperty("phone", newPhone)
            _userProfileData.value = updatedProfile
        }
    }


    fun clearProfile() {
        _userProfileData.value = null
    }
    suspend fun getContent(type: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.getContent(type)
    }

    suspend fun contactUs(
        name: String,
        email: String,
        phone: String,
        message: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.contactUs(name, email, phone, message)
    }


    suspend fun getNotification(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.getNotification()
    }

    suspend fun changeNotificationStatus(notificationStatus: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.changeNotificationStatus(notificationStatus)
    }

    suspend fun logOutAccount(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.logOutAccount()
    }

    suspend fun deleteAccount(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.deleteAccount()
    }

    suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.getUserProfile()
    }


    suspend fun sendOtpEmailPhone(emailPhone: String, userType: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.sendOtpEmailPhone(emailPhone,userType)
    }

    suspend fun otpEmailPhoneVerify(emailPhone: String, userType: String, userOTP: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.otpEmailPhoneVerify(emailPhone,userType,userOTP)
    }

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
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.editUserProfile(
            name,
            phone,
            email,
            location,
            areaOfPractice,
            registrationNumber,
            about,
            latitude,
            longitude,
            type,
            profilePicture
        )
    }

    suspend fun getAreaOfPractice(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.getAreaOfPractice()
    }

    suspend fun getAddress(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.getAddress()
    }

    suspend fun saveAddress(
        addressId: String,
        longitude: String,
        latitude: String,
        addressType: String,
        address: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.saveAddress(addressId, longitude, latitude, addressType, address)

    }

    suspend fun deleteAddress(
        addressId: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.deleteAddress(addressId)

    }

    suspend fun setDefaultAddress(
        addressId: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.setDefaultAddress(addressId)
    }

}