package com.karasu256.karasulab.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.ActiveSession
import com.karasu256.karasulab.data.ActiveSessionRepository
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveSessionsUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val sessions: List<ActiveSession> = emptyList(),
    val pendingRevoke: ActiveSession? = null,
    val revokingIds: Set<String> = emptySet(),
    val message: UiText? = null,
)

@HiltViewModel
class ActiveSessionsViewModel @Inject constructor(
    private val repository: ActiveSessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActiveSessionsUiState())
    val uiState: StateFlow<ActiveSessionsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            attempt { repository.list() }
                .onSuccess { sessions -> _uiState.update { it.copy(loading = false, sessions = sessions) } }
                .onFailure { _uiState.update { it.copy(loading = false, loadFailed = true) } }
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun askRevoke(session: ActiveSession) = _uiState.update { it.copy(pendingRevoke = session) }

    fun cancelRevoke() = _uiState.update { it.copy(pendingRevoke = null) }

    fun revoke() {
        val session = _uiState.value.pendingRevoke ?: return
        if (session.id in _uiState.value.revokingIds) {
            _uiState.update { it.copy(pendingRevoke = null) }
            return
        }
        _uiState.update { it.copy(pendingRevoke = null, revokingIds = it.revokingIds + session.id) }
        viewModelScope.launch {
            attempt { repository.revoke(session) }
                .onSuccess { revoked ->
                    if (revoked) {
                        _uiState.update {
                            it.copy(
                                sessions = it.sessions.filterNot { existing -> existing.id == session.id },
                                message = UiText(R.string.active_sessions_revoked),
                            )
                        }
                    }
                }
                .onFailure { _uiState.update { it.copy(message = UiText(R.string.error_unknown)) } }
            _uiState.update { it.copy(revokingIds = it.revokingIds - session.id) }
        }
    }
}
