package com.karasu256.karasulab.data.remote

import okhttp3.Cookie
import retrofit2.Response

data class SessionCookie(
    val name: String,
    val value: String,
    val expiresAtMillis: Long,
) {
    fun toHeader(): String = "$name=$value"

    companion object {
        fun from(response: Response<*>): SessionCookie? = response.setCookies()
            .firstOrNull { it.name.endsWith("better-auth.session_token") && it.value.isNotEmpty() }
            ?.let { SessionCookie(it.name, it.value, it.expiresAt) }
    }
}

fun Response<*>.setCookies(): List<Cookie> {
    val url = raw().request.url
    return headers().values("Set-Cookie").mapNotNull { Cookie.parse(url, it) }
}
