package com.karasu256.karasulab.data.auth

import android.net.Uri
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.OneTimeTokenRequest
import com.karasu256.karasulab.data.remote.SessionCookie
import com.karasu256.karasulab.data.remote.ensureSuccess
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

object OAuthCallback {
    private const val PATH = "/auth/app/callback"

    fun matches(uri: Uri): Boolean =
        uri.scheme == "https" && uri.host?.endsWith(BuildConfig.ACCOUNTS_HOST) == true && uri.path?.startsWith(
            PATH
        ) == true

    fun error(uri: Uri): String? = uri.getQueryParameter("error")

    fun token(uri: Uri): String? = uri.getQueryParameter("token")

    fun state(uri: Uri): String? = uri.getQueryParameter("state")
}

@Singleton
class OAuthCallbackHub @Inject constructor() {
    private val _callbacks = MutableSharedFlow<Uri>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val callbacks: SharedFlow<Uri> = _callbacks.asSharedFlow()

    fun deliver(uri: Uri) {
        if (OAuthCallback.matches(uri)) _callbacks.tryEmit(uri)
    }
}

@Singleton
class OAuthRedirect @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend fun exchangeToken(token: String, state: String): SessionCookie? {
        val response = authApi.verifyOneTimeToken(state, OneTimeTokenRequest(token))
        response.ensureSuccess()
        return SessionCookie.from(response)
    }
}
