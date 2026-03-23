package com.business.lawco

import android.content.Context

object AppContextProvider {

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun getContext(): Context {
        return appContext
    }

}