package com.business.lawco.networkModel.login

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    ViewModel() {

    var isLoading = ObservableBoolean(false)

    suspend fun login(
        emailOrPhone: String,
        getPassword: String,
        userType: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return loginRepository.login(emailOrPhone, getPassword, userType)
    }

    suspend fun socialLogin(
        name: String,
        email: String,
        userType: String,
        fcmToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return loginRepository.socialLogin(name, email, userType, fcmToken)
    }

}