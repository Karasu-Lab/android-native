package com.karasu256.karasulab.auth

import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.LinkedProviderRepository

sealed interface LinkStart {
    data class Browser(val url: String) : LinkStart

    data class Credential(val value: String) : LinkStart

    data object Dialog : LinkStart

    data object Cancelled : LinkStart
}

open class ProviderLinkFlow(
    protected val authRepository: AuthRepository,
    protected val linkedProviderRepository: LinkedProviderRepository,
) {
    open suspend fun start(providerId: String, credentials: CredentialProvider): LinkStart =
        LinkStart.Browser(authRepository.startOAuthLink(providerId))

    open suspend fun save(providerId: String, credential: String) = Unit

    companion object {
        fun forProvider(providerId: String, authRepository: AuthRepository, linkedProviderRepository: LinkedProviderRepository): ProviderLinkFlow =
            when (providerId) {
                "google" -> GoogleLinkFlow(authRepository, linkedProviderRepository)
                "minecraft" -> MinecraftLinkFlow(authRepository, linkedProviderRepository)
                else -> ProviderLinkFlow(authRepository, linkedProviderRepository)
            }
    }
}

class GoogleLinkFlow(authRepository: AuthRepository, linkedProviderRepository: LinkedProviderRepository) :
    ProviderLinkFlow(authRepository, linkedProviderRepository) {
    override suspend fun start(providerId: String, credentials: CredentialProvider): LinkStart {
        val idToken = credentials.requestGoogleIdToken() ?: return LinkStart.Cancelled
        return LinkStart.Credential(idToken)
    }

    override suspend fun save(providerId: String, credential: String) {
        linkedProviderRepository.linkGoogle(credential)
    }
}

class MinecraftLinkFlow(authRepository: AuthRepository, linkedProviderRepository: LinkedProviderRepository) :
    ProviderLinkFlow(authRepository, linkedProviderRepository) {
    override suspend fun start(providerId: String, credentials: CredentialProvider): LinkStart = LinkStart.Dialog

    override suspend fun save(providerId: String, credential: String) {
        linkedProviderRepository.verifyMinecraft(credential)
    }
}
