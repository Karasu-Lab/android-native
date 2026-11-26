package com.karasu256.karasulab.data.auth

import com.karasu256.karasulab.data.local.SessionStore
import com.karasu256.karasulab.data.local.requireToken
import com.karasu256.karasulab.data.remote.SessionCookie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveSessionCookie @Inject constructor(
    private val store: SessionStore,
) {
    suspend fun header(): String = cookie().toHeader()

    suspend fun value(): String = cookie().value

    private suspend fun cookie(): SessionCookie {
        val token = store.requireToken()
        return SessionCookie(
            token.sessionCookieName,
            token.sessionCookieValue,
            token.sessionExpiresAtMs
        )
    }
}
