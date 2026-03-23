package com.business.lawco.networkModel.homeScreen.attorney

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AttorneyHomeScreenViewModel @Inject constructor(private val homeRepository: AttorneyHomeRepository) : ViewModel() {

    var isLoading = ObservableBoolean(false)

    suspend fun getCredit(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.getCredit()
    }

    suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.getUserProfile()
    }

    suspend fun getAllRegisterLocation(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.getAllRegisterLocation()
    }

    suspend fun getAllRequest(requestType : String,latitude: String,longitude: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.getAllRequest(requestType,latitude,longitude)
        // 0 -> pending
        // 1 -> accepted
        // 2 -> decline
    }

    suspend fun setOnlineStatus(latitude : String , longitude :String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.setOnlineStatus(latitude,longitude)
    }

    suspend fun getOnlineStatus(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.getOnlineStatus()
    }

    suspend fun requestAction(requestId: String, action:  String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return homeRepository.requestAction(requestId,action)
    }


}