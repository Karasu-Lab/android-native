package com.karasu256.karasulab.data.remote

import com.karasu256.karasulab.logging.Log
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class ApiException(
    val status: Int,
    val code: String?,
    message: String?,
    cause: Throwable? = null,
) : Exception("HTTP $status${code?.let { " $it" }.orEmpty()}: $message", cause)

private val errorJson = Json { ignoreUnknownKeys = true }

private fun parseErrorBody(raw: String?): ErrorBody? =
    raw?.takeIf { it.isNotBlank() }?.let {
        runCatching { errorJson.decodeFromString<ErrorBody>(it) }
            .onFailure { error -> Log.e("ApiException", "Unreadable error body: $it", error) }
            .getOrNull()
    }

fun HttpException.toApiException(): ApiException {
    val body = parseErrorBody(response()?.errorBody()?.use { it.string() })
    return ApiException(code(), body?.code, body?.reason ?: body?.message ?: body?.error ?: message(), this)
}

fun Response<ResponseBody>.ensureSuccess() {
    if (isSuccessful) {
        body()?.close()
        return
    }
    val body = parseErrorBody(errorBody()?.use { it.string() })
    throw ApiException(code(), body?.code, body?.reason ?: body?.message ?: body?.error ?: message())
}

fun Response<ResponseBody>.bodyString(): String =
    body()?.use { it.string() } ?: throw ApiException(code(), null, "Empty response body")

suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: kotlin.coroutines.cancellation.CancellationException) {
    throw e
} catch (e: Exception) {
    Log.e("Attempt", "Operation failed: $e", e)
    Result.failure(e)
}

suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: HttpException) {
    throw e.toApiException()
}
