package com.karasu256.karasulab.data.remote

import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UserApi {
    @GET("api/oauth/providers")
    suspend fun oauthProviders(@Query("usage") usage: String): OAuthProvidersResponse

    @GET("user/profile")
    suspend fun profile(): ProfileResponse

    @GET("meta")
    suspend fun meta(): MetaResponse

    @Multipart
    @POST("user/profile/image")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): ProfileImageResponse
}
