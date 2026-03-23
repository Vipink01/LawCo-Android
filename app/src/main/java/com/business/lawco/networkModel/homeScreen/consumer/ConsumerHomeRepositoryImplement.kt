package com.business.lawco.networkModel.homeScreen.consumer

import android.net.Uri
import com.google.gson.JsonObject
import com.business.lawco.networkModel.ApiRequest
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject


class ConsumerHomeRepositoryImplement @Inject constructor(private val apiInterface: ApiRequest) :
    ConsumerHomeRepository {


    override suspend fun getUserProfile(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getUserProfile().enqueue(object : Callback<JsonObject?> {
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

    override suspend fun getAllRegisterLocation(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.getAllRegisterLocation().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
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

    override suspend fun getAreaOfPractice(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.getAreaOfPractice().enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
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

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }
        })
        return data
    }

    override suspend fun getAllAttorneyList(isConnected: String, latitude: String, longitude: String,/* address: List<String>,*/areaOfPractice: List<String>): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.getAttorneyList(isConnected, latitude, longitude,/*address,*/areaOfPractice).enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
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
                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    obj.setIsErrorAlt(true)
                    obj.setMessageAlt(t.message.toString())
                    data.value = obj
                }
            })
        return data
    }

    override suspend fun sendRequestToAttorney(
        attorneyId: String,
        action: String,
        latitude:String,
        longitude:String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.sendRequestToAttorney(attorneyId, action,
            latitude,longitude)
            .enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
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

    override suspend fun sendRequestToAttorneyWithDoc(
        attorneyId: String,
        action: String,
        latitude: String,
        longitude: String,
        etSubject: String,
        etCardNumber: String,
        uriList: List<MultipartBody.Part>
    ): SingleLiveEvent<BaseResponse<JsonObject>> {

        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()

        // Convert fields to RequestBody
        val attorneyIdBody = createRequestBody(attorneyId)
        val actionBody = createRequestBody(action)
        val latitudeBody = createRequestBody(latitude)
        val longitudeBody = createRequestBody(longitude)
        val subjectBody = createRequestBody(etSubject)
        val discriptionBody = createRequestBody(etCardNumber)
        // Convert URIs to MultipartBody.Part


        apiInterface.sendRequestToAttorneyWithDoc(
            attorneyIdBody,
            actionBody,
            latitudeBody,
            longitudeBody,
            subjectBody,
            discriptionBody,
            uriList
        ).enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
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
            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                obj.setIsErrorAlt(true)
                obj.setMessageAlt(t.message.toString())
                data.value = obj
            }
        })

        return data
    }




    fun createRequestBody(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun createMultipartFiles(uriList: MutableList<Uri>): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()
        uriList.forEachIndexed { index, uri ->
            val file = File(uri.path!!) // convert URI to File
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("files[]", file.name, requestFile)
            parts.add(part)
        }
        return parts
    }



}