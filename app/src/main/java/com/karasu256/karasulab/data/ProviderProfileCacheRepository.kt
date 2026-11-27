package com.karasu256.karasulab.data

import com.karasu256.karasulab.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class CachedProviderProfile(
    val iconUrl: String?,
    val name: String?,
    val description: String?,
)

@Singleton
class ProviderProfileCacheRepository @Inject constructor() {
    private val _profiles = MutableStateFlow<Map<String, CachedProviderProfile>>(emptyMap())
    val profiles: StateFlow<Map<String, CachedProviderProfile>> = _profiles.asStateFlow()

    fun apply(userId: String?, providerId: String, profile: ProviderProfile?) {
        _profiles.update { current ->
            if (profile == null) {
                if (providerId in current) current - providerId else current
            } else {
                current + (providerId to CachedProviderProfile(
                    iconUrl = iconUrl(userId, providerId, profile.avatarUrl),
                    name = profile.name,
                    description = profile.email ?: profile.accountId,
                ))
            }
        }
    }

    private fun iconUrl(userId: String?, providerId: String, served: String?): String? {
        if (served == null || providerId == "minecraft" || userId == null) return served
        return "${BuildConfig.CDN_URL}u/profiles/$userId/avatars/$providerId/avatar.png"
    }
}
