package com.karasu256.karasulab.auth

import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.SignInResult

sealed interface LoginStart {
    data class Browser(val url: String) : LoginStart

    data class Completed(val result: SignInResult) : LoginStart

    data object Cancelled : LoginStart
}

open class ProviderLoginFlow(
    protected val authRepository: AuthRepository,
) {
    protected var completedResult: SignInResult? = null

    open suspend fun start(providerId: String, addingAccount: Boolean, credentials: CredentialProvider): LoginStart =
        LoginStart.Browser(authRepository.startOAuthSignIn(providerId, addingAccount))

    open suspend fun finish(token: String, state: String, addingAccount: Boolean): SignInResult =
        authRepository.completeSocialSignIn(token, state, addingAccount)

    protected fun complete(result: SignInResult): LoginStart {
        completedResult = result
        return LoginStart.Completed(result)
    }

    companion object {
        fun forProvider(providerId: String, authRepository: AuthRepository, email: String? = null): ProviderLoginFlow =
            when (providerId) {
                "google" -> GoogleLoginFlow(authRepository)
                "passkey" -> PasskeyLoginFlow(authRepository, email)
                else -> ProviderLoginFlow(authRepository)
            }
    }
}

class GoogleLoginFlow(authRepository: AuthRepository) : ProviderLoginFlow(authRepository) {
    override suspend fun start(providerId: String, addingAccount: Boolean, credentials: CredentialProvider): LoginStart {
        val idToken = credentials.requestGoogleIdToken() ?: return LoginStart.Cancelled
        return complete(authRepository.signInWithGoogle(idToken, addingAccount))
    }

    override suspend fun finish(token: String, state: String, addingAccount: Boolean): SignInResult =
        checkNotNull(completedResult) { "The Google sign-in completes in the start flow" }
}

class PasskeyLoginFlow(authRepository: AuthRepository, private val email: String?) : ProviderLoginFlow(authRepository) {
    override suspend fun start(providerId: String, addingAccount: Boolean, credentials: CredentialProvider): LoginStart {
        val challenge = authRepository.requestPasskeyChallenge()
        val assertion = credentials.requestPasskeyAssertion(challenge.optionsJson) ?: return LoginStart.Cancelled
        return complete(authRepository.signInWithPasskey(challenge, assertion, email, addingAccount))
    }

    override suspend fun finish(token: String, state: String, addingAccount: Boolean): SignInResult =
        checkNotNull(completedResult) { "The passkey sign-in completes in the start flow" }
}
