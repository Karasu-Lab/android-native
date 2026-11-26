package com.karasu256.karasulab.data

import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.DeletePasskeyRequest
import com.karasu256.karasulab.data.remote.PasskeyRegisterOptions
import com.karasu256.karasulab.data.remote.PasskeyRegisterRequest
import com.karasu256.karasulab.data.remote.bodyString
import com.karasu256.karasulab.data.remote.ensureSuccess
import com.karasu256.karasulab.data.remote.setCookies
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

data class PendingPasskey(
    val defaultName: String,
    val response: JsonElement,
    val cookieHeader: String,
)

@Singleton
class PasskeyRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionCookie: ActiveSessionCookie,
    private val json: Json,
) {
    suspend fun begin(credentials: CredentialProvider): PendingPasskey? {
        val session = sessionCookie.header()
        val options = authApi.generatePasskeyRegisterOptions(session)
        if (!options.isSuccessful) throw HttpException(options)
        val optionsJson = options.bodyString()
        val challenge = options.setCookies().joinToString("; ") { "${it.name}=${it.value}" }
        val registration = credentials.createPasskey(optionsJson) ?: return null
        val user = json.decodeFromString<PasskeyRegisterOptions>(optionsJson).user
        val defaultName = user.displayName ?: user.name.orEmpty()
        return PendingPasskey(defaultName, json.decodeFromString<JsonElement>(registration), "$session; $challenge")
    }

    suspend fun complete(pending: PendingPasskey, name: String) {
        authApi.verifyPasskeyRegistration(
            cookie = pending.cookieHeader,
            body = PasskeyRegisterRequest(response = pending.response, name = name),
        ).ensureSuccess()
    }

    suspend fun delete(id: String) {
        authApi.deletePasskey(sessionCookie.header(), DeletePasskeyRequest(id)).ensureSuccess()
    }
}
