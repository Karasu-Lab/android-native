package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.DeleteConsentRequest
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthorizedAppRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionCookie: ActiveSessionCookie,
) {
    suspend fun list(): List<AuthorizedApp> =
        apiCall { authApi.authorizedApps(sessionCookie.header()) }.map {
            AuthorizedApp(it.id, it.clientId, it.clientName, it.createdAt)
        }

    suspend fun detail(id: String): AuthorizedAppDetail {
        val response = apiCall { authApi.authorizedApp(sessionCookie.header(), id) }
        return AuthorizedAppDetail(
            id = response.id,
            clientId = response.clientId,
            clientName = response.clientName,
            logoUri = response.logoUri,
            scopes = response.scopes,
            createdAt = response.createdAt,
        )
    }

    suspend fun revoke(id: String) {
        authApi.deleteConsent(sessionCookie.header(), DeleteConsentRequest(id)).ensureSuccess()
    }
}
