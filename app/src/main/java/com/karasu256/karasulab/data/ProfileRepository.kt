package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.local.SessionStore
import com.karasu256.karasulab.data.local.requireAccount
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.UpdateImageRequest
import com.karasu256.karasulab.data.remote.UpdateNameRequest
import com.karasu256.karasulab.data.remote.UserApi
import com.karasu256.karasulab.data.remote.apiCall
import com.karasu256.karasulab.data.remote.ensureSuccess
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val store: SessionStore,
    private val sessionCookie: ActiveSessionCookie,
) {
    suspend fun refresh() {
        val profile = apiCall { userApi.profile() }
        store.updateProfile(profile.id, profile.name, profile.image)
    }

    suspend fun updateName(name: String) {
        val account = store.requireAccount()
        authApi.updateUserName(sessionCookie.header(), UpdateNameRequest(name)).ensureSuccess()
        store.updateProfile(account.id, name, account.image)
    }

    suspend fun uploadIcon(bytes: ByteArray, mimeType: String) {
        val account = store.requireAccount()
        val part = MultipartBody.Part.createFormData(
            "file",
            "avatar",
            bytes.toRequestBody(mimeType.toMediaType())
        )
        val response = apiCall { userApi.uploadProfileImage(part) }
        store.updateProfile(account.id, account.name, response.image)
    }

    suspend fun setIcon(url: String?) {
        val account = store.requireAccount()
        authApi.updateUserImage(
            sessionCookie.header(),
            UpdateImageRequest(if (url == null) JsonNull else JsonPrimitive(url))
        ).ensureSuccess()
        store.updateProfile(account.id, account.name, url)
    }

    suspend fun resendVerificationEmail() {
        authApi.resendVerificationEmail(sessionCookie.header()).ensureSuccess()
    }
}
