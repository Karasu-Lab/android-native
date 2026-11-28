package com.karasu256.karasulab.auth

import android.app.Activity
import androidx.credentials.Credential
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential

open class BaseCredentialAuthenticator(protected val activity: Activity) : CredentialProvider {
    protected val credentialManager: CredentialManager = CredentialManager.create(activity)

    override suspend fun requestGoogleIdToken(): String? = null

    override suspend fun requestPasskeyAssertion(requestJson: String): String? {
        val credential = request(GetPublicKeyCredentialOption(requestJson))
        check(credential is PublicKeyCredential) { "Unexpected credential type: ${credential.type}" }
        return credential.authenticationResponseJson
    }

    override suspend fun createPasskey(requestJson: String): String? {
        val response = credentialManager.createCredential(activity, CreatePublicKeyCredentialRequest(requestJson))
        check(response is CreatePublicKeyCredentialResponse) { "Unexpected credential response: ${response.type}" }
        return response.registrationResponseJson
    }

    protected suspend fun request(option: CredentialOption): Credential =
        credentialManager.getCredential(activity, GetCredentialRequest(listOf(option))).credential
}
