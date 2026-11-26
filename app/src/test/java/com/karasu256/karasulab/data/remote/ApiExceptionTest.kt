package com.karasu256.karasulab.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiExceptionTest {
    @Test
    fun propagatesServerReasonToExceptionMessage() {
        val response = Response.error<okhttp3.ResponseBody>(
            400,
            """{"error":"invalid_oauth_link_request","reason":"callbackURL: Invalid input"}"""
                .toResponseBody("application/json".toMediaType()),
        )

        assertTrue(HttpException(response).toApiException().message.orEmpty().contains("callbackURL: Invalid input"))
    }
}
