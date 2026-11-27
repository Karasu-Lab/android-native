package com.karasu256.karasulab.auth

import android.app.Activity
import com.karasu256.karasulab.logging.Log
import androidx.credentials.Credential
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException

class CredentialAuthenticator(private val activity: Activity) : CredentialProvider {
    private val credentialManager = CredentialManager.create(activity)

    override suspend fun requestPasskeyAssertion(requestJson: String): String? {
        val credential = request(GetPublicKeyCredentialOption(requestJson)) ?: return null
        check(credential is PublicKeyCredential) { "Unexpected credential type: ${credential.type}" }
        return credential.authenticationResponseJson
    }

    override suspend fun createPasskey(requestJson: String): String? = try {
        val response = credentialManager.createCredential(activity, CreatePublicKeyCredentialRequest(requestJson))
        check(response is CreatePublicKeyCredentialResponse) { "Unexpected credential response: ${response.type}" }
        response.registrationResponseJson
    } catch (e: CreateCredentialCancellationException) {
        Log.e("CredentialAuthenticator", "Credential request cancelled: ${e.type}: ${e.errorMessage}", e)
        null
    } catch (e: CreateCredentialException) {
        Log.e("CredentialAuthenticator", "Credential request failed: ${e.type}: ${e.errorMessage}", e)
        throw e
    }

    private suspend fun request(option: CredentialOption): Credential? = try {
        credentialManager.getCredential(activity, GetCredentialRequest(listOf(option))).credential
    } catch (e: GetCredentialCancellationException) {
        Log.e("CredentialAuthenticator", "Credential request cancelled: ${e.type}: ${e.errorMessage}", e)
        null
    } catch (e: GetCredentialException) {
        Log.e("CredentialAuthenticator", "Credential request failed: ${e.type}: ${e.errorMessage}", e)
        throw e
    }
}
