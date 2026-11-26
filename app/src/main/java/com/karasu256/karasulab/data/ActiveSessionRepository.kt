package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.RevokeSessionRequest
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveSessionRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionCookie: ActiveSessionCookie,
) {
    suspend fun list(): List<ActiveSession> {
        val currentToken = URLDecoder.decode(sessionCookie.value(), "UTF-8").substringBefore('.')
        return apiCall { authApi.listSessions(sessionCookie.header()) }.map {
            ActiveSession(
                id = it.id,
                token = it.token,
                createdAt = it.createdAt,
                ipAddress = it.ipAddress,
                userAgent = it.userAgent,
                current = it.token == currentToken,
            )
        }
    }

    suspend fun revoke(session: ActiveSession) {
        authApi.revokeSession(sessionCookie.header(), RevokeSessionRequest(session.token)).ensureSuccess()
    }
}
