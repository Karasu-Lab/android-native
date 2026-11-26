package com.karasu256.karasulab.data.auth

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64

data class NativeOAuthCallback(
    val providerId: String,
    val state: String,
    val token: String?,
    val linked: String?,
    val error: String?,
)

object NativeOAuthCallbackUri {
    private val providerPattern = Regex("^[a-z0-9-]+$", RegexOption.IGNORE_CASE)
    private val statePattern = Regex("^[A-Za-z0-9_-]{16,128}$")
    private val tokenPattern = Regex("^[A-Za-z0-9._-]{16,512}$")

    fun newState(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun callback(providerId: String, state: String): String {
        require(providerPattern.matches(providerId))
        require(statePattern.matches(state))
        return URI("karasulab", "api", "/auth/callback/$providerId", "state=$state", null).toASCIIString()
    }

    fun signInUrl(providerId: String, state: String, authUiHost: String): String {
        val callback = callback(providerId, state)
        require(authUiHost.matches(Regex("^[a-z0-9.-]+$", RegexOption.IGNORE_CASE)))
        val next = URLEncoder.encode(callback, StandardCharsets.UTF_8)
        return "https://$authUiHost/sign-in?provider=$providerId&prompt=login&next=$next"
    }

    fun parse(raw: String): NativeOAuthCallback? {
        return try {
            val uri = URI(raw)
            if (
                uri.scheme != "karasulab" ||
                uri.host != "api" ||
                uri.rawUserInfo != null ||
                uri.port != -1 ||
                !uri.path.matches(Regex("^/auth/callback/[a-z0-9-]+$", RegexOption.IGNORE_CASE))
            ) {
                return null
            }

            val parameters = mutableMapOf<String, String>()
            for (entry in uri.rawQuery.orEmpty().split('&').filter(String::isNotEmpty)) {
                val parts = entry.split('=', limit = 2)
                val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8)
                val value = URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                if (key in setOf("state", "token", "linked", "error") && parameters.put(key, value) != null) return null
            }

            val state = parameters["state"]?.takeIf(statePattern::matches) ?: return null
            val token = parameters["token"]?.takeIf(tokenPattern::matches)
            if (parameters.containsKey("token") && token == null) return null
            val providerId = uri.path.substringAfterLast('/')
            if (!providerPattern.matches(providerId)) return null

            NativeOAuthCallback(providerId, state, token, parameters["linked"], parameters["error"])
        } catch (_: Exception) {
            null
        }
    }
}
