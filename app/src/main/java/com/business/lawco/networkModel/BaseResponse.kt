package com.business.lawco.networkModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class BaseResponse<T> {

    var response: T? = null

    var isIsError: Boolean = false

    @SerializedName("message")
    @Expose
    var message: String = ""

    @SerializedName("code")
    @Expose
    var code: Int? = null

    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("data")
    @Expose
    var data: T? = null

    fun setIsErrorAlt(isError: Boolean) {
        this.isIsError = isError
    }

    fun setMessageAlt(message : String) {
        this.message = message
    }

    fun setResponseAlt(response : T) {
            this.response = response
    }
}