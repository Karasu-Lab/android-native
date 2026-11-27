package com.karasu256.karasulab.ui.linking

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.LinkedProviderRepository
import com.karasu256.karasulab.data.OAuthProvider
import com.karasu256.karasulab.data.OAuthProviderRepository
import com.karasu256.karasulab.data.ProviderProfile
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
    val providers: List<OAuthProvider> = emptyList(),
    val profiles: Map<String, ProviderProfile?> = emptyMap(),
    val hasPasskeys: Boolean = false,
    val hasPassword: Boolean = false,
    val busyProviderId: String? = null,
    val unlinkTarget: OAuthProvider? = null,
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
    private val oauthProviderRepository: OAuthProviderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LinkedAccountsUiState())
    val uiState: StateFlow<LinkedAccountsUiState> = _uiState.asStateFlow()

    private val _openBrowser = Channel<String>(Channel.BUFFERED)
    val openBrowser: Flow<String> = _openBrowser.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.oauthCompletions.collect { completion ->
                if (!completion.isLinking) return@collect
                _uiState.update { it.copy(busyProviderId = null) }
                if (completion.result == SignInResult.Success) {
                    refreshProvider(completion.providerId)
                    val label = _uiState.value.providers.firstOrNull { it.id == completion.providerId }?.label
                        ?: completion.providerId
                    _uiState.update { it.copy(message = UiText(R.string.connections_linked, listOf(label))) }
                } else {
                    Log.e("LinkedAccountsViewModel", "OAuth link failed for ${completion.providerId}: ${completion.result}")
                    FirebaseCrashlytics.getInstance().log("OAuth link failed for ${completion.providerId}: ${completion.result}")
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
            val providers = attempt { oauthProviderRepository.list("link") }
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    loadFailed = profiles.isFailure || providers.isFailure,
                    providers = providers.getOrDefault(state.providers),
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
            FirebaseCrashlytics.getInstance().log("Refreshing provider $providerId failed")
            FirebaseCrashlytics.getInstance().recordException(error)
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun link(provider: OAuthProvider, credentials: CredentialProvider) {
        if (_uiState.value.busyProviderId != null) return
        if (provider.id == "minecraft") {
            _uiState.update { it.copy(minecraftDialog = true, minecraftError = null) }
            return
        }
        if (provider.id == "google") linkGoogle(provider, credentials) else linkOAuth(provider)
    }

    fun profileTitle(provider: OAuthProvider, profile: ProviderProfile?): String = when (provider.id) {
        "xbox", "minecraft" -> profile?.name ?: profile?.accountId ?: provider.label
        else -> profile?.name ?: profile?.email ?: provider.label
    }

    private fun linkGoogle(provider: OAuthProvider, credentials: CredentialProvider) {
        _uiState.update { it.copy(busyProviderId = provider.id) }
        viewModelScope.launch {
            val result = attempt {
                val idToken = credentials.requestGoogleIdToken() ?: return@attempt false
                linkedProviderRepository.linkGoogle(idToken)
                true
            }
            result.onSuccess { linked ->
                if (linked) linkSucceeded(provider)
            }.onFailure { error ->
                Log.e("LinkedAccountsViewModel", "Linking ${provider.id} failed", error)
                FirebaseCrashlytics.getInstance().log("Linking ${provider.id} failed")
                FirebaseCrashlytics.getInstance().recordException(error)
                linkFailed()
            }
            refreshProvider(provider.id)
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }

    fun addressCopied() = _uiState.update { it.copy(message = UiText(R.string.minecraft_verify_address_copied)) }

    fun dismissMinecraft() = _uiState.update { it.copy(minecraftDialog = false, minecraftError = null) }

    fun verifyMinecraft(code: String) {
        val provider = _uiState.value.providers.firstOrNull { it.id == "minecraft" } ?: return
        _uiState.update { it.copy(busyProviderId = provider.id, minecraftError = null) }
        viewModelScope.launch {
            attempt { linkedProviderRepository.verifyMinecraft(code) }
                .onSuccess {
                    _uiState.update { it.copy(minecraftDialog = false) }
                    linkSucceeded(provider)
                    refreshProvider(provider.id)
                }
                .onFailure { error ->
                    Log.e("LinkedAccountsViewModel", "Verifying the Minecraft code failed", error)
                    FirebaseCrashlytics.getInstance().log("Verifying the Minecraft code failed")
                    FirebaseCrashlytics.getInstance().recordException(error)
                    val message = if ((error as? ApiException)?.status == 409) R.string.minecraft_verify_already_linked else R.string.minecraft_verify_failed
                    _uiState.update { it.copy(minecraftError = UiText(message)) }
                }
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }

    private fun linkFailed() = _uiState.update { it.copy(message = UiText(R.string.connections_link_failed)) }

    private fun linkSucceeded(provider: OAuthProvider) =
        _uiState.update { it.copy(message = UiText(R.string.connections_linked, listOf(provider.label))) }

    fun askUnlink(provider: OAuthProvider) = _uiState.update { it.copy(unlinkTarget = provider) }

    private fun linkOAuth(provider: OAuthProvider) {
        _uiState.update { it.copy(busyProviderId = provider.id) }
        viewModelScope.launch {
            val result = attempt { authRepository.startOAuthLink(provider.id) }
                .onSuccess { url -> _openBrowser.send(url) }
                .onFailure { error ->
                    Log.e("LinkedAccountsViewModel", "Starting OAuth link for ${provider.id} failed", error)
                    FirebaseCrashlytics.getInstance().log("Starting OAuth link for ${provider.id} failed")
                    FirebaseCrashlytics.getInstance().recordException(error)
                    _uiState.update { it.copy(message = UiText(R.string.connections_link_failed)) }
                }
            if (result.isFailure) _uiState.update { it.copy(busyProviderId = null) }
        }
    }

    fun cancelUnlink() = _uiState.update { it.copy(unlinkTarget = null) }

    fun unlink() {
        val provider = _uiState.value.unlinkTarget ?: return
        _uiState.update { it.copy(unlinkTarget = null, busyProviderId = provider.id) }
        viewModelScope.launch {
            attempt { linkedProviderRepository.unlink(provider.id) }
                .onFailure { error ->
                    Log.e("LinkedAccountsViewModel", "Unlinking ${provider.id} failed", error)
                    FirebaseCrashlytics.getInstance().log("Unlinking ${provider.id} failed")
                    FirebaseCrashlytics.getInstance().recordException(error)
                    _uiState.update { it.copy(message = UiText(R.string.error_unknown)) }
                }
            refreshProvider(provider.id)
            _uiState.update { it.copy(busyProviderId = null) }
        }
    }
}
