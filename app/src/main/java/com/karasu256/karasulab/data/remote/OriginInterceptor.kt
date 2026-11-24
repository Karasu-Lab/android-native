package com.karasu256.karasulab.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class OriginInterceptor(private val origin: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method == "GET" || request.header("Origin") != null) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("Origin", origin).build())
    }
}
