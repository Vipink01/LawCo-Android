package com.business.lawco.networkModel.homeScreen.attorney

import com.google.gson.JsonObject
import com.business.lawco.networkModel.ApiRequest
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class AttorneyHomeRepositoryImplement @Inject constructor(private val apiInterface: ApiRequest) : AttorneyHomeRepository {

    override suspend fun getCredit(): SingleLiveEvent<BaseResponse<JsonObject>> {

        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getCredit().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }

    override suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getUserProfile().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }

   override suspend fun getAllRegisterLocation(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getAllRegisterLocation().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }

    override suspend fun getAllRequest(requestType: String,latitude: String,longitude: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getAllRequest(requestType,latitude,
            longitude).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                try {
                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }

    override suspend fun setOnlineStatus(
        latitude: String,
        longitude: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.setOnlineStatus(latitude , longitude).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }

    override suspend fun getOnlineStatus(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getOnlineStatus().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                try {

                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }

    override suspend fun requestAction(
        requestId: String,
        action:  String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.respondToRequest(requestId,action).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>) {
                try {
                    if (  response.body() != null) {
                        obj.setResponseAlt(response.body()!!)
                        obj.setIsErrorAlt(false)
                    } else {
                        obj.setMessageAlt("Server error")
                        obj.setIsErrorAlt(true)
                    }
                    data.value =obj
                } catch (e: Exception) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(e.message.toString())
                    data.value =obj
                }
            }

            override fun onFailure(
                call: Call<JsonObject?>,
                t: Throwable
            ) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value =obj
            }

        })

        return data
    }



}