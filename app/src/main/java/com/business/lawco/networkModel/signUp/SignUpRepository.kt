package com.business.lawco.networkModel.signUp

import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent

interface SignUpRepository {

    suspend fun signUp(
        name: String,
        emailOrPhone: String,
        password: String,
        type: String
    ): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun signUpOtp(emailOrPhone: String,type: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun socialLogin(
        name: String,
        email: String,
        userType: String,
        fcmToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>>

}
