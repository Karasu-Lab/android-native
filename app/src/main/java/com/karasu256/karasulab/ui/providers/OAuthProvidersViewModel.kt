package com.karasu256.karasulab.ui.providers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.OAuthProvider
import com.karasu256.karasulab.data.OAuthProviderRepository
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OAuthProvidersUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val providers: List<OAuthProvider> = emptyList(),
) {
    val oauthProviders: List<OAuthProvider> get() = providers.filter { it.kind != "passkey" && it.id != "minecraft" }

    val passkeyProvider: OAuthProvider? get() = providers.firstOrNull { it.kind == "passkey" }

    val minecraftProvider: OAuthProvider? get() = providers.firstOrNull { it.id == "minecraft" }
}

@HiltViewModel
class OAuthProvidersViewModel @Inject constructor(
    private val oauthProviderRepository: OAuthProviderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OAuthProvidersUiState())
    val uiState: StateFlow<OAuthProvidersUiState> = _uiState.asStateFlow()

    fun load(usage: String) {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            attempt { oauthProviderRepository.list(usage) }
                .onSuccess { fetched ->
                    val providers = when (usage) {
                        "signIn" -> fetched.filter(OAuthProvider::signIn)
                        "link" -> fetched.filter(OAuthProvider::link)
                        else -> fetched
                    }
                    _uiState.update { it.copy(loading = false, providers = providers) }
                }
                .onFailure { error ->
                    Log.e("OAuthProvidersViewModel", "Could not load OAuth providers for $usage", error)
                    _uiState.update { it.copy(loading = false, error = UiText(R.string.error_unknown)) }
                }
        }
    }
}
