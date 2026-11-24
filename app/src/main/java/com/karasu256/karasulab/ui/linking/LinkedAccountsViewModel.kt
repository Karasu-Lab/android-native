package com.karasu256.karasulab.ui.linking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.data.LinkedProviderRepository
import com.karasu256.karasulab.data.ProviderProfile
import com.karasu256.karasulab.data.SecurityRepository
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkProvider(val id: String, val label: String)

val linkProviders = listOf(
    LinkProvider("google", "Google"),
    LinkProvider("github", "GitHub"),
    LinkProvider("microsoft", "Microsoft"),
    LinkProvider("xbox", "Xbox"),
    LinkProvider("minecraft", "Minecraft"),
)

data class LinkedAccountsUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val profiles: Map<String, ProviderProfile?> = emptyMap(),
    val hasPasskeys: Boolean = false,
    val hasPassword: Boolean = false,
    val busyProviderId: String? = null,
    val unlinkTarget: LinkProvider? = null,
    val message: UiText? = null,
) {
    val linkedCount: Int get() = profiles.values.count { it != null }

    val canUnlink: Boolean get() = linkedCount > 1 || hasPasskeys || hasPassword
}

@HiltViewModel
class LinkedAccountsViewModel @Inject constructor(
    private val linkedProviderRepository: LinkedProviderRepository,
    private val securityRepository: SecurityRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LinkedAccountsUiState())
    val uiState: StateFlow<LinkedAccountsUiState> = _uiState.asStateFlow()

    private val _openBrowser = Channel<String>(Channel.BUFFERED)
    val openBrowser: Flow<String> = _openBrowser.receiveAsFlow()

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            val profiles = attempt { linkedProviderRepository.profiles() }
            val security = attempt { securityRepository.settings() }
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    loadFailed = profiles.isFailure,
                    profiles = profiles.getOrDefault(state.profiles),
                    hasPasskeys = security.getOrNull()?.passkeys?.isNotEmpty() ?: state.hasPasskeys,
                    hasPassword = security.getOrNull()?.hasPassword ?: state.hasPassword,
                )
            }
        }
    }

    private suspend fun refreshProvider(providerId: String) {
        attempt { linkedProviderRepository.profiles() }.onSuccess { fetched ->
            _uiState.update { state -> state.copy(profiles = state.profiles + (providerId to fetched[providerId])) }
        }.onFailure { error ->
            Log.e("LinkedAccountsViewModel", "Refreshing provider $providerId failed", error)
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun link(provider: LinkProvider, credentials: CredentialProvider) {
        if (_uiState.value.busyProviderId != null) return
        if (provider.id != "google") {
            _openBrowser.trySend("https://${BuildConfig.AUTH_HOST.replace("api-auth", "accounts")}/settings/linking")
            return
        }
        _uiState.update { it.copy(busyProviderId = provider.id) }
        viewModelScope.launch {
            val result = attempt {
                val idToken = credentials.requestGoogleIdToken() ?: return@attempt false
                linkedProviderRepository.linkGoogle(idToken)
                true
            }
            result.onSuccess { linked ->
                if (linked) _uiState.update { it.copy(message = UiText(R.string.connections_linked, listOf(provider.label))) }
            }.onFailure { error ->
                Log.e("LinkedAccountsViewModel", "Linking ${provider.id} failed", error)
                _uiState.update { it.copy(message = UiText(R.string.connections_link_failed)) }
            }
            refreshProvider(provider.id)
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }

    fun askUnlink(provider: LinkProvider) = _uiState.update { it.copy(unlinkTarget = provider) }

    fun cancelUnlink() = _uiState.update { it.copy(unlinkTarget = null) }

    fun unlink() {
        val provider = _uiState.value.unlinkTarget ?: return
        _uiState.update { it.copy(unlinkTarget = null, busyProviderId = provider.id) }
        viewModelScope.launch {
            attempt { linkedProviderRepository.unlink(provider.id) }
                .onSuccess { _uiState.update { it.copy(profiles = it.profiles + (provider.id to null)) } }
                .onFailure { error ->
                    Log.e("LinkedAccountsViewModel", "Unlinking ${provider.id} failed", error)
                    _uiState.update { it.copy(message = UiText(R.string.error_unknown)) }
                }
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }
}
