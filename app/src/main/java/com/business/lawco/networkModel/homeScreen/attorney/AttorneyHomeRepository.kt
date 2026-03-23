package com.business.lawco.networkModel.homeScreen.attorney

import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent

interface AttorneyHomeRepository {

    suspend fun getCredit(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getAllRegisterLocation(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getAllRequest(requestType : String, latitude: String,longitude: String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun setOnlineStatus(latitude : String , longitude :String): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getOnlineStatus(): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun requestAction(requestId : String, action :String): SingleLiveEvent<BaseResponse<JsonObject>>


}
