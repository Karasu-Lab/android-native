package com.karasu256.karasulab.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.AuthorizedApp
import com.karasu256.karasulab.data.AuthorizedAppDetail
import com.karasu256.karasulab.data.AuthorizedAppRepository
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthorizedAppsUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val apps: List<AuthorizedApp> = emptyList(),
    val infoApp: AuthorizedApp? = null,
    val infoDetail: AuthorizedAppDetail? = null,
    val pendingRevoke: AuthorizedApp? = null,
    val revokingId: String? = null,
    val message: UiText? = null,
)

@HiltViewModel
class AuthorizedAppsViewModel @Inject constructor(
    private val repository: AuthorizedAppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthorizedAppsUiState())
    val uiState: StateFlow<AuthorizedAppsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            attempt { repository.list() }
                .onSuccess { apps -> _uiState.update { it.copy(loading = false, apps = apps) } }
                .onFailure { _uiState.update { it.copy(loading = false, loadFailed = true) } }
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun openInfo(app: AuthorizedApp) {
        _uiState.update { it.copy(infoApp = app, infoDetail = null) }
        viewModelScope.launch {
            attempt { repository.detail(app.id) }
                .onSuccess { detail -> _uiState.update { if (it.infoApp?.id == app.id) it.copy(infoDetail = detail) else it } }
                .onFailure { _uiState.update { it.copy(infoApp = null, message = UiText(R.string.error_unknown)) } }
        }
    }

    fun closeInfo() = _uiState.update { it.copy(infoApp = null, infoDetail = null) }

    fun askRevoke(app: AuthorizedApp) = _uiState.update { it.copy(pendingRevoke = app) }

    fun cancelRevoke() = _uiState.update { it.copy(pendingRevoke = null) }

    fun revoke() {
        val app = _uiState.value.pendingRevoke ?: return
        _uiState.update { it.copy(pendingRevoke = null, revokingId = app.id) }
        viewModelScope.launch {
            attempt { repository.revoke(app.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            apps = it.apps.filterNot { existing -> existing.id == app.id },
                            message = UiText(R.string.authorized_apps_revoked, listOf(app.clientName ?: app.clientId)),
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(message = UiText(R.string.error_unknown)) } }
            _uiState.update { it.copy(revokingId = null) }
        }
    }
}
