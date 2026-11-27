package com.karasu256.karasulab.auth

interface CredentialProvider {
    suspend fun requestPasskeyAssertion(requestJson: String): String?

    suspend fun createPasskey(requestJson: String): String?
}
