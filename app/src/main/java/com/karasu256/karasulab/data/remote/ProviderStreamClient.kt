package com.karasu256.karasulab.data.remote

import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.auth.ActiveSessionCookie
import com.karasu256.karasulab.di.AuthHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ProviderProfileEvent(
    val providerId: String,
    val profile: ProviderProfileResponse? = null,
)

@Serializable
private data class ProviderLinkStateEvent(
    val providerId: String,
    val providerUserId: String,
    val linkable: Boolean,
)

sealed interface ProviderStreamEvent {
    data class Profile(val providerId: String, val profile: ProviderProfileResponse?) : ProviderStreamEvent

    data class LinkState(val providerId: String, val providerUserId: String, val linkable: Boolean) : ProviderStreamEvent
}

@Singleton
class ProviderStreamClient @Inject constructor(
    @AuthHttpClient private val client: OkHttpClient,
    private val sessionCookie: ActiveSessionCookie,
    private val json: Json,
) {
    fun events(): Flow<ProviderStreamEvent> = flow {
        val request = Request.Builder()
            .url("https://${BuildConfig.AUTH_HOST}/api/users/me/providers/stream")
            .header("Cookie", sessionCookie.header())
            .header("Accept", "text/event-stream")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("The provider stream failed with ${response.code}")
            val source = response.body.source()
            var event: String? = null
            val data = StringBuilder()
            while (true) {
                val line = source.readUtf8Line() ?: break
                when {
                    line.isEmpty() -> {
                        val payload = data.toString()
                        data.clear()
                        if (payload.isNotEmpty()) {
                            when (event) {
                                "profile" -> {
                                    val parsed = json.decodeFromString<ProviderProfileEvent>(payload)
                                    emit(ProviderStreamEvent.Profile(parsed.providerId, parsed.profile))
                                }
                                "link-state" -> {
                                    val parsed = json.decodeFromString<ProviderLinkStateEvent>(payload)
                                    emit(ProviderStreamEvent.LinkState(parsed.providerId, parsed.providerUserId, parsed.linkable))
                                }
                            }
                        }
                        event = null
                    }
                    line.startsWith("event:") -> event = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> data.append(line.removePrefix("data:").trim())
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
