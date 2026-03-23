package com.business.lawco.networkModel.paymentManagement

import com.google.gson.JsonObject
import com.business.lawco.networkModel.ApiRequest
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class PaymentManagementRepositoryImplement @Inject constructor(private val apiInterface: ApiRequest) :
    PaymentManagementRepository {


    override suspend fun saveCard(
        cardToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {

        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.saveCard(
            cardToken,
        ).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value = obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value = obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }

        })

        return data
    }

    override suspend fun removeCard(cardId: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.removeCard(
            cardId,
        ).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value = obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value = obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }

        })

        return data
    }

    override suspend fun getAllCard(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getAllCard().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value = obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value = obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }

        })

        return data
    }

    override suspend fun getSubcription(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getSubcription().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value = obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value = obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }

        })

        return data
    }

    override suspend fun getTransactionList(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getTransactionList().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value = obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value = obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }

        })

        return data
    }

    override suspend fun paymentForSubcription(
        price: String,
        planId: String,
        status: String,
        customerId: String,
        token: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.paymentForSubcription(price, planId, status, customerId, token)
            .enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: Response<JsonObject?>
                ) {
                    try {

                        if (response.body() != null) {
                            obj.setResponseAlt(response.body()!!)
                            obj.setIsErrorAlt(false)
                        } else {
                            obj.setMessageAlt("Server error")
                            obj.setIsErrorAlt(true)
                        }
                        data.value = obj
                    } catch (e: Exception) {
                        obj.setIsErrorAlt(true)
                        obj.setMessageAlt(e.message.toString())
                        data.value = obj
                    }
                }

                override fun onFailure(
                    call: Call<JsonObject?>,
                    t: Throwable
                ) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(t.message.toString())
                    data.value = obj
                }

            })

        return data
    }


    override suspend fun cancelSubscription(subscriptionId: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.cancelSubscription(
            subscriptionId,
        ).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value = obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value = obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }

        })

        return data
    }

}