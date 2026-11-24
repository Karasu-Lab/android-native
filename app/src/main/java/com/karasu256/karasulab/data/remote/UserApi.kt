package com.karasu256.karasulab.data.remote

import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserApi {
    @GET("user/profile")
    suspend fun profile(): ProfileResponse

    @GET("meta")
    suspend fun meta(): MetaResponse

    @Multipart
    @POST("user/profile/image")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): ProfileImageResponse
}
