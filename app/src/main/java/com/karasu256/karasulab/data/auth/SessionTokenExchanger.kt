package com.karasu256.karasulab.data.auth

import android.util.Log
import com.karasu256.karasulab.data.local.AccountEntity
import com.karasu256.karasulab.data.local.AuthTokenEntity
import com.karasu256.karasulab.data.local.SessionStore
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.GetSessionResponse
import com.karasu256.karasulab.data.remote.SessionCookie
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

class AccountAlreadySignedInException(val cookie: SessionCookie) : Exception()

@Singleton
class SessionTokenExchanger @Inject constructor(
    private val authApi: AuthApi,
    private val store: SessionStore,
    private val jwtExpiry: JwtExpiry,
    private val json: Json,
) {
    private val mutex = Mutex()

    suspend fun exchange(cookie: SessionCookie, rejectKnown: Boolean = false): AuthTokenEntity? {
        val response = authApi.getSession(cookie.toHeader())
        if (response.code() in 401..403) {
            response.errorBody()?.close()
            Log.e("SessionTokenExchanger", "get-session rejected the session cookie with ${response.code()}")
            return null
        }
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body()?.use { it.string() } ?: run {
            Log.e("SessionTokenExchanger", "get-session returned an empty body")
            return null
        }
        val user = json.decodeFromString<GetSessionResponse?>(body)?.user ?: run {
            Log.e("SessionTokenExchanger", "get-session returned no user: $body")
            return null
        }
        val customToken = response.headers()["set-auth-jwt"] ?: run {
            Log.e("SessionTokenExchanger", "get-session returned no set-auth-jwt header: ${response.headers()}")
            return null
        }
        if (rejectKnown && store.account(user.id) != null) throw AccountAlreadySignedInException(cookie)
        val renewed = SessionCookie.from(response) ?: cookie
        val token = AuthTokenEntity(
            sessionCookieName = renewed.name,
            sessionCookieValue = renewed.value,
            sessionExpiresAtMs = renewed.expiresAtMillis,
            customToken = customToken,
            customTokenExpiresAtMs = jwtExpiry.expiresAtMillis(customToken) ?: 0L,
        )
        val account = AccountEntity(
            id = user.id,
            name = user.name,
            email = user.email,
            emailVerified = user.emailVerified,
            image = user.image,
            sessionCookieName = renewed.name,
            sessionCookieValue = renewed.value,
            sessionExpiresAtMs = renewed.expiresAtMillis,
            isActive = true,
            addedAtMs = System.currentTimeMillis(),
        )
        store.saveSession(token, account)
        return token
    }

    suspend fun validToken(): String? {
        val current = store.currentToken() ?: return null
        if (current.customTokenExpiresAtMs - 30_000L > System.currentTimeMillis()) return current.customToken
        return reexchange(current.customToken)
    }

    suspend fun reexchange(staleToken: String?): String? = mutex.withLock {
        val current = store.currentToken() ?: return@withLock null
        if (staleToken != null && current.customToken != staleToken) return@withLock current.customToken
        val cookie = SessionCookie(current.sessionCookieName, current.sessionCookieValue, current.sessionExpiresAtMs)
        val refreshed = exchange(cookie)
        if (refreshed == null) store.clearActive()
        refreshed?.customToken
    }
}
