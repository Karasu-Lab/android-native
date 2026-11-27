package com.karasu256.karasulab.ui.linking

import com.karasu256.karasulab.logging.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.auth.LinkStart
import com.karasu256.karasulab.auth.ProviderLinkFlow
import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.LinkedProviderRepository
import com.karasu256.karasulab.data.ProviderClient
import com.karasu256.karasulab.data.ProviderClientRepository
import com.karasu256.karasulab.data.CachedProviderProfile
import com.karasu256.karasulab.data.ProviderProfileCacheRepository
import com.karasu256.karasulab.data.SignInResult
import com.karasu256.karasulab.data.SecurityRepository
import com.karasu256.karasulab.data.remote.ApiException
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkedAccountsUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val clients: List<ProviderClient> = emptyList(),
    val profiles: Map<String, CachedProviderProfile?> = emptyMap(),
    val linkable: Map<String, Boolean> = emptyMap(),
    val hasPasskeys: Boolean = false,
    val hasPassword: Boolean = false,
    val busyProviderId: String? = null,
    val unlinkTarget: ProviderClient? = null,
    val minecraftDialog: Boolean = false,
    val minecraftError: UiText? = null,
    val message: UiText? = null,
) {
    val linkedCount: Int get() = profiles.values.count { it != null }

    val canUnlink: Boolean get() = linkedCount > 1 || hasPasskeys || hasPassword
}

@HiltViewModel
class LinkedAccountsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val linkedProviderRepository: LinkedProviderRepository,
    private val securityRepository: SecurityRepository,
    profileCache: ProviderProfileCacheRepository,
    clientRepository: ProviderClientRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LinkedAccountsUiState())
    val uiState: StateFlow<LinkedAccountsUiState> = _uiState.asStateFlow()

    private val _openBrowser = Channel<String>(Channel.BUFFERED)
    val openBrowser: Flow<String> = _openBrowser.receiveAsFlow()

    private var activeLinkFlow: ProviderLinkFlow? = null
    private var minecraftProvider: ProviderClient? = null

    init {
        linkedProviderRepository.startStream(viewModelScope)
        viewModelScope.launch {
            clientRepository.clients.collect { clients ->
                _uiState.update { it.copy(clients = clients) }
            }
        }
        viewModelScope.launch {
            profileCache.profiles.collect { profiles ->
                _uiState.update { it.copy(profiles = profiles) }
            }
        }
        viewModelScope.launch {
            linkedProviderRepository.linkable.collect { linkable ->
                _uiState.update { it.copy(linkable = linkable) }
            }
        }
        viewModelScope.launch {
            authRepository.oauthCompletions.collect { completion ->
                if (!completion.isLinking) return@collect
                _uiState.update { it.copy(busyProviderId = null) }
                if (completion.result == SignInResult.Success) {
                    refreshProvider(completion.providerId)
                    val label = _uiState.value.clients.find { it.providerId == completion.providerId }?.label ?: completion.providerId
                    _uiState.update { it.copy(message = UiText(R.string.connections_linked, listOf(label))) }
                } else {
                    Log.e("LinkedAccountsViewModel", "OAuth link failed for ${completion.providerId}: ${completion.result}")
                    _uiState.update { it.copy(message = UiText(R.string.connections_link_failed)) }
                }
            }
        }
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false, busyProviderId = null) }
        viewModelScope.launch {
            val profiles = attempt { linkedProviderRepository.profiles() }
            val security = attempt { securityRepository.settings() }
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    loadFailed = profiles.isFailure,
                    hasPasskeys = security.getOrNull()?.passkeys?.isNotEmpty() ?: state.hasPasskeys,
                    hasPassword = security.getOrNull()?.hasPassword ?: state.hasPassword,
                )
            }
        }
    }

    private suspend fun refreshProvider(providerId: String) {
        attempt { linkedProviderRepository.profiles() }.onFailure { error ->
            Log.e("LinkedAccountsViewModel", "Refreshing provider $providerId failed", error)
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun link(provider: ProviderClient, credentials: CredentialProvider) {
        if (_uiState.value.busyProviderId != null) return
        val flow = ProviderLinkFlow.forProvider(provider.providerId, authRepository, linkedProviderRepository)
        activeLinkFlow = flow
        _uiState.update { it.copy(busyProviderId = provider.providerId) }
        viewModelScope.launch {
            val start = attempt { flow.start(provider.providerId, credentials) }.getOrElse { error ->
                Log.e("LinkedAccountsViewModel", "Linking ${provider.providerId} failed", error)
                linkFailed()
                _uiState.update { it.copy(busyProviderId = null) }
                return@launch
            }
            when (start) {
                is LinkStart.Browser -> _openBrowser.send(start.url)
                is LinkStart.Credential -> {
                    attempt { flow.save(provider.providerId, start.value) }
                        .onSuccess { linkSucceeded(provider) }
                        .onFailure { error ->
                            Log.e("LinkedAccountsViewModel", "Linking ${provider.providerId} failed", error)
                            linkFailed()
                        }
                    refreshProvider(provider.providerId)
                    _uiState.update { it.copy(busyProviderId = null) }
                }
                LinkStart.Dialog -> {
                    minecraftProvider = provider
                    _uiState.update { it.copy(busyProviderId = null, minecraftDialog = true, minecraftError = null) }
                }
                LinkStart.Cancelled -> {
                    refreshProvider(provider.providerId)
                    _uiState.update { it.copy(busyProviderId = null) }
                }
            }
        }
    }

    fun profileTitle(provider: ProviderClient, profile: CachedProviderProfile?): String =
        profile?.name ?: profile?.description ?: provider.label

    fun addressCopied() = _uiState.update { it.copy(message = UiText(R.string.minecraft_verify_address_copied)) }

    fun dismissMinecraft() = _uiState.update { it.copy(minecraftDialog = false, minecraftError = null) }

    fun verifyMinecraft(code: String) {
        val provider = minecraftProvider ?: return
        val flow = activeLinkFlow ?: return
        _uiState.update { it.copy(busyProviderId = provider.providerId, minecraftError = null) }
        viewModelScope.launch {
            attempt { flow.save(provider.providerId, code) }
                .onSuccess {
                    _uiState.update { it.copy(minecraftDialog = false) }
                    linkSucceeded(provider)
                    refreshProvider(provider.providerId)
                }
                .onFailure { error ->
                    Log.e("LinkedAccountsViewModel", "Verifying the Minecraft code failed", error)
                    val message = if ((error as? ApiException)?.status == 409) R.string.minecraft_verify_already_linked else R.string.minecraft_verify_failed
                    _uiState.update { it.copy(minecraftError = UiText(message)) }
                }
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }

    private fun linkFailed() = _uiState.update { it.copy(message = UiText(R.string.connections_link_failed)) }

    private fun linkSucceeded(provider: ProviderClient) =
        _uiState.update { it.copy(message = UiText(R.string.connections_linked, listOf(provider.label))) }

    fun askUnlink(provider: ProviderClient) = _uiState.update { it.copy(unlinkTarget = provider) }

    fun cancelUnlink() = _uiState.update { it.copy(unlinkTarget = null) }

    fun unlink() {
        val provider = _uiState.value.unlinkTarget ?: return
        _uiState.update { it.copy(unlinkTarget = null, busyProviderId = provider.providerId) }
        viewModelScope.launch {
            attempt { linkedProviderRepository.unlink(provider.providerId) }
                .onFailure { error ->
                    Log.e("LinkedAccountsViewModel", "Unlinking ${provider.providerId} failed", error)
                    _uiState.update { it.copy(message = UiText(R.string.error_unknown)) }
                }
            refreshProvider(provider.providerId)
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }
}
