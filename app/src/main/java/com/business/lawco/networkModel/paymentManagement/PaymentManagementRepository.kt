package com.business.lawco.networkModel.paymentManagement

import com.google.gson.JsonObject
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent

interface PaymentManagementRepository {

    suspend fun saveCard(
        cardToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun removeCard(
         cardId :String
    ) : SingleLiveEvent<BaseResponse<JsonObject>>


    suspend fun getAllCard() : SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getSubcription() : SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun getTransactionList() : SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun paymentForSubcription(
        price :String,
        planId :String,
        status :String,
        customerId :String,
        token :String
    ) : SingleLiveEvent<BaseResponse<JsonObject>>

    suspend fun cancelSubscription(
        subscriptionId :String
    ) : SingleLiveEvent<BaseResponse<JsonObject>>

}
