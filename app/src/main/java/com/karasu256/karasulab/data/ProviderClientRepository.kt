package com.karasu256.karasulab.data

import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.remote.ProviderClientResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ProviderClient(
    val providerId: String,
    val clientId: String?,
    val iconUrl: String,
) {
    val label: String get() = providerId.replaceFirstChar { it.uppercase() }
}

@Singleton
class ProviderClientRepository @Inject constructor() {
    private val _clients = MutableStateFlow<List<ProviderClient>>(emptyList())
    val clients: StateFlow<List<ProviderClient>> = _clients.asStateFlow()

    fun apply(clients: List<ProviderClientResponse>) {
        _clients.value = clients.map { client ->
            ProviderClient(
                providerId = client.providerId,
                clientId = client.clientId,
                iconUrl = "https://${BuildConfig.API_HOST}${client.iconPath}",
            )
        }
    }
}
