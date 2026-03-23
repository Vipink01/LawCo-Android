package com.business.lawco.networkModel.common

import com.google.gson.JsonObject
import com.business.lawco.networkModel.ApiRequest
import com.business.lawco.networkModel.BaseResponse
import com.business.lawco.networkModel.SingleLiveEvent
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class CommonRepositoryImplement @Inject constructor(private val apiInterface: ApiRequest) :
    CommonRepository {

    override suspend fun getContent(type: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getContent(
            type,
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

    override suspend fun contactUs(
        name
        : String,
        email: String,
        phone: String,
        message: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {

        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()


        apiInterface.contactUs(
            name,
            email,
            phone,
            message,
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


    override suspend fun getNotification(): SingleLiveEvent<BaseResponse<JsonObject>> {

        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getNotification().enqueue(object : Callback<JsonObject?> {
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

    override suspend fun logOutAccount(): SingleLiveEvent<BaseResponse<JsonObject>> {

        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.logOutAccount().enqueue(object : Callback<JsonObject?> {
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

    override suspend fun deleteAccount(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.deleteAccount().enqueue(object : Callback<JsonObject?> {
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


    override suspend fun sendOtpEmailPhone(emailPhone: String, userType: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.sendOtpEmailPhone(emailPhone,userType).enqueue(object : Callback<JsonObject?> {
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

    override suspend fun sendOtpClaimEmailPhone(emailPhone: String, userID: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.sendOtpClaimEmailPhone(emailPhone,userID).enqueue(object : Callback<JsonObject?> {
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

    override suspend fun personaVerifyUser(userID: String,status: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.personaVerifyUser(userID,status).enqueue(object : Callback<JsonObject?> {
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

    override suspend fun otpClaimEmailPhoneVerify(emailPhone: String, userID: String,otp: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.otpClaimEmailPhoneVerify(emailPhone,userID,otp).enqueue(object : Callback<JsonObject?> {
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

    override suspend fun searchAttorneyList(search: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.searchAttorneyList(search).enqueue(object : Callback<JsonObject?> {
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

    override suspend fun otpEmailPhoneVerify(
        emailPhone: String,
        userType: String,
        userOTP: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.otpEmailPhoneVerify(emailPhone,userType,userOTP).enqueue(object : Callback<JsonObject?> {
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


    override suspend fun editUserProfile(
        name: RequestBody,
        phone: RequestBody,
        email: RequestBody,
        location: RequestBody,
        areaOfPractice: RequestBody,
        registrationNumber: RequestBody,
        about: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        type: RequestBody,
        profilePicture: MultipartBody.Part?
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()
        val obj: BaseResponse<JsonObject> = BaseResponse()
        apiInterface.editUserProfile(
            name,
            phone,
            email,
            location,
            areaOfPractice,
            registrationNumber,
            about,
            latitude,
            longitude,
            type,
            profilePicture
        ).enqueue(
            object : Callback<JsonObject?> {
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

    override suspend fun changeNotificationStatus(notificationStatus: String): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.changeNotificationStatus(
            notificationStatus
        ).enqueue(
            object : Callback<JsonObject?> {

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

    override suspend fun getAddress(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getAddress().enqueue(object : Callback<JsonObject?> {
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

    override suspend fun saveAddress(
        addressId: String,
        longitude: String,
        latitude: String,
        addressType: String,
        address: String
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.saveAddress(addressId, longitude, latitude, addressType, address)
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

    override suspend fun deleteAddress(
        addressId: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.deleteAddress(addressId)
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

    override suspend fun setDefaultAddress(
        addressId: String,
    ): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.setDefaultAddress(addressId)
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

    override suspend fun getAreaOfPractice(): SingleLiveEvent<BaseResponse<JsonObject>> {
        val data: SingleLiveEvent<BaseResponse<JsonObject>> = SingleLiveEvent()

        val obj: BaseResponse<JsonObject> = BaseResponse()

        apiInterface.getAreaOfPractice().enqueue(object : Callback<JsonObject?> {
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