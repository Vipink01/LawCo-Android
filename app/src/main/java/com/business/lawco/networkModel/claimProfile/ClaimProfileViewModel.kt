package com.business.lawco.networkModel.claimProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.lawco.model.claimprofilemodellist.Data
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import com.business.lawco.networkModel.common.CommonRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClaimProfileViewModel @Inject constructor(private val commonRepository: CommonRepository) :
    ViewModel() {


    private val _dataList = MutableLiveData<MutableList<Data>>(mutableListOf())
    val dataList: LiveData<MutableList<Data>> = _dataList


    // Set the full list (e.g., after fetching from API)
    fun setDataList(list: List<Data>) {
        _dataList.value = list.toMutableList()
    }


    // LiveData to hold the selected attorney item
    private val _selectedAttorney = MutableLiveData<Data>()
    val selectedAttorney: LiveData<Data> = _selectedAttorney

    // Function to update the selected attorney when an item is clicked
    fun selectAttorney(attorney: Data) {
        _selectedAttorney.value = attorney
    }

    // Function to update email status
    fun updateEmailStatus(status: Boolean) {
        _selectedAttorney.value = _selectedAttorney.value?.copy(emailStatus = status)
    }

    // Function to update phone status
    fun updatePhoneStatus(status: Boolean) {
        _selectedAttorney.value = _selectedAttorney.value?.copy(phoneStatus = status)
    }

    suspend fun searchAttorneyList(search: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.searchAttorneyList(search)
    }


    suspend fun sendOtpClaimEmailPhone(emailPhone: String, userID: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.sendOtpClaimEmailPhone(emailPhone,userID)
    }

    suspend fun personaVerifyUser(userID: String,status: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.personaVerifyUser(userID,status)
    }

    suspend fun otpClaimEmailPhoneVerify(emailPhone: String, userID: String, otp: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        return commonRepository.otpClaimEmailPhoneVerify(emailPhone,userID,otp)
    }


    fun clearAllData() {
        _dataList.value = mutableListOf()
        _selectedAttorney.value = null
    }


}

