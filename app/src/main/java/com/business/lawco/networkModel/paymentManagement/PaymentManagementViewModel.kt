package com.business.lawco.networkModel.paymentManagement

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PaymentManagementViewModel @Inject constructor(private val paymentManagementRepository: PaymentManagementRepository) :
    ViewModel() {

    var isLoading = ObservableBoolean(false)

    suspend fun saveCard(
        cardToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.saveCard(cardToken)
    }

    suspend fun removeCard(
        cardId: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.removeCard(cardId)
    }

    suspend fun getAllCard(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.getAllCard()
    }

    suspend fun getSubcription(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.getSubcription()


    } suspend fun getTransactionList(): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.getTransactionList()
    }

    suspend fun paymentForSubcription(
        price: String,
        planId: String,
        status: String,  // 0 -> Save and Payment 1->  Direct Payment
        customerId: String,
        token: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.paymentForSubcription(price, planId, status, customerId,token)
    }

    suspend fun cancelSubscription(
        subscriptionId: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        return paymentManagementRepository.cancelSubscription(subscriptionId)
    }

}