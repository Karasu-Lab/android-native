package com.karasu256.karasulab.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class HttpErrorLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            val body = response.peekBody(4_096L).string()
            Log.w("HttpError", "${response.request.method} ${response.request.url} -> ${response.code} $body")
        }
        return response
    }
}
