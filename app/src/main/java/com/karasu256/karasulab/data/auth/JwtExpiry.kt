package com.karasu256.karasulab.data.auth

import android.util.Base64
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

fun interface JwtExpiry {
    fun expiresAtMillis(token: String): Long?
}

@Serializable
private data class JwtPayload(val exp: Long? = null)

class Base64JwtExpiry @Inject constructor(
    private val json: Json,
) : JwtExpiry {
    override fun expiresAtMillis(token: String): Long? {
        val payload = token.split('.').getOrNull(1) ?: return null
        return runCatching {
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            json.decodeFromString<JwtPayload>(decoded.toString(Charsets.UTF_8)).exp?.times(1000)
        }.onFailure { Log.e("JwtExpiry", "Could not read the token expiry", it) }.getOrNull()
    }
}
