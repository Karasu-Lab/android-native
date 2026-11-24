package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.IdTokenPayload
import com.karasu256.karasulab.data.remote.LinkSocialRequest
import com.karasu256.karasulab.data.remote.UnlinkAccountRequest
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkedProviderRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionCookie: ActiveSessionCookie,
) {
    suspend fun profiles(): Map<String, ProviderProfile?> =
        apiCall { authApi.linkedProviders(sessionCookie.header()) }.mapValues { (_, profile) ->
            profile?.let { ProviderProfile(it.name, it.email, it.accountId, it.avatarUrl) }
        }

    suspend fun linkGoogle(idToken: String) {
        authApi.linkSocial(sessionCookie.header(), LinkSocialRequest("google", IdTokenPayload(idToken))).ensureSuccess()
    }

    suspend fun unlink(providerId: String) {
        authApi.unlinkAccount(sessionCookie.header(), UnlinkAccountRequest(providerId)).ensureSuccess()
    }
}
