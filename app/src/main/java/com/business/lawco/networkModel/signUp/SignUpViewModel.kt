package com.business.lawco.networkModel.signUp

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SignUpViewModel @Inject constructor(private val signUpRepository: SignUpRepository) : ViewModel() {

    var isLoading = ObservableBoolean(false)

    suspend fun signUp(
        name: String,
        emailOrPhone: String,
        password: String,
        type: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return signUpRepository.signUp(name, emailOrPhone, password, type)
    }

    suspend fun signUpOtp(emailOrPhone: String,type: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return signUpRepository.signUpOtp(emailOrPhone,type)
    }

    suspend fun socialLogin(
        name: String,
        email: String,
        userType: String,
        fcmToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return signUpRepository.socialLogin(name, email, userType, fcmToken)
    }

}