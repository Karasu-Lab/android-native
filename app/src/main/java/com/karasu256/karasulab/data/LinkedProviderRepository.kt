package com.karasu256.karasulab.data

import com.karasu256.karasulab.logging.Log
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.ProviderProfileResponse
import com.karasu256.karasulab.data.remote.ProviderStreamClient
import com.karasu256.karasulab.data.remote.ProviderStreamEvent
import com.karasu256.karasulab.data.remote.UnlinkAccountRequest
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val json: Json,
    private val streamClient: ProviderStreamClient,
    private val profileCache: ProviderProfileCacheRepository,
) {
    private val _linkable = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val linkable: StateFlow<Map<String, Boolean>> = _linkable.asStateFlow()

    private var streamJob: Job? = null

    suspend fun profiles(): Map<String, ProviderProfile?> {
        val userId = accountRepository.activeAccountId()
        val profiles = apiCall { authApi.linkedProviders(sessionCookie.header()) }.mapValues { (providerId, value) ->
            (value as? JsonObject)?.takeIf { it.isNotEmpty() }
                ?.let { json.decodeFromJsonElement<ProviderProfileResponse>(it) }
                ?.let { ProviderProfile(it.name, it.email, it.accountId, avatarUrl(userId, providerId, it.avatarUrl)) }
        }
        profiles.forEach { (providerId, profile) -> profileCache.apply(userId, providerId, profile) }
        return profiles
    }

    fun startStream(scope: CoroutineScope) {
        if (streamJob?.isActive == true) return
        streamJob = scope.launch {
            try {
                streamClient.events().collect { event ->
                    when (event) {
                        is ProviderStreamEvent.Profile -> profileCache.apply(
                            accountRepository.activeAccountId(),
                            event.providerId,
                            event.profile?.let { ProviderProfile(it.name, it.email, it.accountId, it.avatarUrl) },
                        )
                        is ProviderStreamEvent.LinkState -> _linkable.update { it + (event.providerId to event.linkable) }
                    }
                }
            } catch (error: Exception) {
                Log.e("LinkedProviderRepository", "The provider stream ended", error)
            }
        }
    }

    private fun avatarUrl(userId: String?, providerId: String, served: String?): String? {
        if (served == null || providerId == "minecraft" || userId == null) return served
        return "${BuildConfig.CDN_URL}u/profiles/$userId/avatars/$providerId/avatar.png"
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
