package com.business.lawco.networkModel.forgotPassword

import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent

interface ForgotPasswordRepository {

    suspend fun sendForgotVerificationOtp(emailOrPhone: String,userType: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun resetPassword(emailPhone: String ,password: String , passwordConfirmation :String): SingleLiveEvent<BaseResponse<JsonObject>>

}
