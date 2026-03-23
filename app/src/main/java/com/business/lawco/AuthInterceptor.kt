package com.business.lawco

import android.content.Context
import android.util.Log
import com.business.lawco.networkModel.SessionEventBus
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor( private var context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getBearerToken()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            Log.d("AUTH_TOKEN","Auth token is :- "+token)
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val response = chain.proceed(requestBuilder.build())
        Log.d("AUTH_TOKEN", " Status Code is :- "+ response.code)
        if (response.code == 401) {
            Log.d("TESTING_Auth", "Response Error Interceptor")
//            AuthEventManager.notifyAuthRequired()
            SessionEventBus.emitSessionExpired()
        }
        return response
    }

    private fun getBearerToken(): String {
        val sessionManager = SessionManager(context)
        val token = sessionManager.getBearerToken()
        Log.d("TESTING_TOKEN", token)
        return token
    }


}