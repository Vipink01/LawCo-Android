package com.business.lawco

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object AuthEventManager {

    private val _authRequired = MutableLiveData<Boolean>()
    val authRequired: LiveData<Boolean> get() = _authRequired

    /**
     * Notify observers that authentication is required.
     */
    fun notifyAuthRequired() {
        _authRequired.postValue(true)
    }

    fun notifyAuthFalse(){
        _authRequired.postValue(false)
    }


}