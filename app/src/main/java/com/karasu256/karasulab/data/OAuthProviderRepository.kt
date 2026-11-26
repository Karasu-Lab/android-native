package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.remote.OAuthProviderColors
import com.karasu256.karasulab.data.remote.OAuthProviderResponse
import com.karasu256.karasulab.data.remote.UserApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthProviderRepository @Inject constructor(
    private val userApi: UserApi,
) {
    suspend fun list(usage: String): List<OAuthProvider> =
        userApi.oauthProviders(usage).providers.map(OAuthProviderResponse::toModel)
}

internal fun OAuthProviderResponse.toModel() = OAuthProvider(
    id = id,
    label = label,
    kind = kind,
    signIn = signIn,
    link = link,
    backgroundColor = backgroundColor.toModel(),
    foregroundColor = foregroundColor.toModel(),
    iconUrl = iconUrl,
)

private fun OAuthProviderColors.toModel() = ProviderColors(default = default, dark = dark)
