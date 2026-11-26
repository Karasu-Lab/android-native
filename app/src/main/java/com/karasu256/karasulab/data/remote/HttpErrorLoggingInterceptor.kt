package com.karasu256.karasulab.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpErrorLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            Log.e("HttpError", "${request.method} ${request.url} failed before a response: $e", e)
            throw e
        }
        if (!response.isSuccessful) {
            val body = response.peekBody(4_096L).string()
            val sent = "cookie=${request.header("Cookie") != null} authorization=${request.header("Authorization") != null} origin=${request.header("Origin")}"
            Log.e("HttpError", "${request.method} ${request.url} -> ${response.code} [$sent] $body")
        }
        return response
    }
}
