package com.karasu256.karasulab.data

import com.karasu256.karasulab.logging.Log
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.auth.SessionTokenExchanger
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.IdTokenPayload
import com.karasu256.karasulab.data.remote.LinkSocialRequest
import com.karasu256.karasulab.data.remote.ProviderProfileResponse
import com.karasu256.karasulab.data.remote.UnlinkAccountRequest
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkedProviderRepository @Inject constructor(
    private val authApi: AuthApi,
    private val accountRepository: AccountRepository,
    private val sessionCookie: ActiveSessionCookie,
    private val exchanger: SessionTokenExchanger,
    private val json: Json,
) {
    suspend fun profiles(): Map<String, ProviderProfile?> {
        val userId = accountRepository.activeAccountId()
        return apiCall { authApi.linkedProviders(sessionCookie.header()) }.mapValues { (providerId, value) ->
            (value as? JsonObject)?.takeIf { it.isNotEmpty() }
                ?.let { json.decodeFromJsonElement<ProviderProfileResponse>(it) }
                ?.let { ProviderProfile(it.name, it.email, it.accountId, avatarUrl(userId, providerId, it.avatarUrl)) }
        }
    }

    private fun avatarUrl(userId: String?, providerId: String, served: String?): String? {
        if (served == null || providerId == "minecraft" || userId == null) return served
        return "${BuildConfig.CDN_URL}u/profiles/$userId/avatars/$providerId/avatar.png"
    }

    suspend fun linkGoogle(idToken: String) {
        authApi.linkSocial(
            sessionCookie.header(),
            LinkSocialRequest("google", IdTokenPayload(idToken))
        ).ensureSuccess()
        exchanger.reexchange(staleToken = null)
    }

    suspend fun verifyMinecraft(code: String) {
        authApi.verifyMinecraft("https://${BuildConfig.MC_AUTH_HOST}/api/mc/verify", sessionCookie.header(), code).ensureSuccess()
    }

    suspend fun unlink(providerId: String) {
        runCatching {
            authApi.unlinkAccount(sessionCookie.header(), UnlinkAccountRequest(providerId)).ensureSuccess()
        }.onFailure { Log.e("LinkedProviderRepository", "Unlinking $providerId failed", it) }.getOrThrow()
    }
}
