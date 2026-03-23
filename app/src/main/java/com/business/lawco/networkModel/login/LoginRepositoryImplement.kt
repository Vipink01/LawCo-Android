package com.business.lawco.networkModel.login

import com.google.gson.JsonObject
import com.business.lawco.networkModel.ApiRequest
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class LoginRepositoryImplement @Inject constructor(private val apiInterface: ApiRequest) : LoginRepository {

    override suspend fun login(
        emailOrPhone: String,
        getPassword: String,
        userType: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()


        apiInterface.signIn(emailOrPhone, getPassword, userType,
        ).enqueue(object : Callback<JsonObject?> { override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                try {
                    if (response.body() != null) {
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

    override suspend fun socialLogin(
        name: String,
        email: String,
        userType: String,
        fcmToken: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.socialLogIn(
            name,
            email,
            userType,
            fcmToken,
            "social"
        ).enqueue(object : Callback<JsonObject?> {
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


}