package com.business.lawco.networkModel.login

import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent

interface LoginRepository {

    suspend fun login(
        emailOrPhone: String,
        getPassword: String,
        userType: String
    ): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun socialLogin(
        name: String,
        email: String,
        userType: String,
        fcmToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>>

}
