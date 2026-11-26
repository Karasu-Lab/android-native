package com.karasu256.karasulab.data.remote

import com.karasu256.karasulab.BuildConfig
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
            .firstOrNull { it.name.endsWith("${BuildConfig.COOKIE_PREFIX}.session_token") && it.value.isNotEmpty() }
            ?.let { SessionCookie(it.name, it.value, it.expiresAt) }
    }
}

fun Response<*>.cookieHeader(): String = setCookies().filter { it.value.isNotEmpty() }
    .associateBy { it.name }.values.joinToString("; ") { "${it.name}=${it.value}" }

fun String.withoutCookieDomain(): String =
    replace(Regex(";\\s*Domain=[^;]*", RegexOption.IGNORE_CASE), "")

fun Response<*>.setCookies(): List<Cookie> {
    val url = raw().request.url
    return headers().values("Set-Cookie").mapNotNull { header ->
        Cookie.parse(url, header) ?: Cookie.parse(url, header.withoutCookieDomain())
    }
}
