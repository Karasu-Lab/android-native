package com.karasu256.karasulab.auth

import android.app.Activity
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.karasu256.karasulab.BuildConfig

class CredentialAuthenticator(activity: Activity) : BaseCredentialAuthenticator(activity) {
    override suspend fun requestGoogleIdToken(): String? {
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_SERVER_CLIENT_ID).build()
        val credential = request(option)
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential type: ${credential.type}"
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}
