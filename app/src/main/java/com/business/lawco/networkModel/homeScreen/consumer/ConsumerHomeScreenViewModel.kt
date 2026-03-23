package com.business.lawco.networkModel.homeScreen.consumer

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ConsumerHomeScreenViewModel @Inject constructor(private val consumerRepository: ConsumerHomeRepository) : ViewModel() {

    var isLoading = ObservableBoolean(false)

    suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return consumerRepository.getUserProfile()
    }

    suspend fun getAllRegisterLocation(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return consumerRepository.getAllRegisterLocation()
    }

    suspend fun getAreaOfPractice(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return consumerRepository.getAreaOfPractice()
    }

    suspend fun getAllAttorneyList(isConnected: String, latitude: String, longitude: String,/*  address: List<String>,*/
        areaOfPractice: List<String>
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return consumerRepository.getAllAttorneyList(isConnected, latitude, longitude,/*address,*/areaOfPractice)
    }

    suspend fun sendRequestToAttorney(
        attorneyId: String,
        action: String,
        latitude:String,
        longitude:String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return consumerRepository.sendRequestToAttorney(attorneyId,
            action,latitude, longitude)
    }

    suspend fun sendRequestToAttorneyWithDoc(
        attorneyId: String,
        action: String,
        latitude: String,
        longitude: String,
        etSubject: String,
        etCardNumber: String,
        uriList: List<MultipartBody.Part>
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return consumerRepository.sendRequestToAttorneyWithDoc(attorneyId,
            action,latitude, longitude,etSubject,etCardNumber,uriList)
    }


}