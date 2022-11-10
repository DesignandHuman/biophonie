package com.example.biophonie.network

import android.util.Log
import com.example.biophonie.util.AppPrefs
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Response

class AuthenticationInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (chain.request().url.toString().contains("/restricted/")) {
            val token = AppPrefs.token
            if (token != null) {
                val newRequest = chain.request().signWithToken(token)
                chain.proceed(newRequest)
            } else {
                chain.proceed(chain.request())
            }
        } else {
            chain.proceed(chain.request())
        }
    }
}