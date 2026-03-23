package com.business.lawco.networkModel.forgotPassword

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val forgotPasswordRepository: ForgotPasswordRepository) : ViewModel() {

    var isLoading = ObservableBoolean(false)

    suspend fun sendForgotVerificationOtp(emailOrPhone: String,userType :String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return forgotPasswordRepository.sendForgotVerificationOtp(emailOrPhone,userType)
    }

    suspend fun resetPassword(emailOrPhone: String,password: String , passwordConfirmation :String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return forgotPasswordRepository.resetPassword(emailOrPhone,password,passwordConfirmation)
    }

}