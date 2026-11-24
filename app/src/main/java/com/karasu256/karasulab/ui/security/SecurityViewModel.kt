package com.karasu256.karasulab.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.data.Passkey
import com.karasu256.karasulab.data.PasskeyRepository
import com.karasu256.karasulab.data.PendingPasskey
import com.karasu256.karasulab.data.SecurityRepository
import com.karasu256.karasulab.data.SecuritySettings
import com.karasu256.karasulab.data.remote.ApiException
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordDialogState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val saving: Boolean = false,
    val error: UiText? = null,
)

data class DeleteDialogState(
    val password: String = "",
    val deleting: Boolean = false,
    val error: UiText? = null,
)

data class PasskeyNameDialogState(
    val pending: PendingPasskey,
    val name: String,
    val saving: Boolean = false,
)

data class SecurityUiState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val settings: SecuritySettings? = null,
    val passwordDialog: PasswordDialogState? = null,
    val registering: Boolean = false,
    val passkeyNameDialog: PasskeyNameDialogState? = null,
    val passkeyToDelete: Passkey? = null,
    val deletingPasskeyId: String? = null,
    val deleteDialog: DeleteDialogState? = null,
    val message: UiText? = null,
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val passkeyRepository: PasskeyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, loadFailed = false) }
        viewModelScope.launch {
            attempt { securityRepository.settings() }
                .onSuccess { settings -> _uiState.update { it.copy(loading = false, settings = settings) } }
                .onFailure { _uiState.update { it.copy(loading = false, loadFailed = true) } }
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun openPasswordDialog() = _uiState.update { it.copy(passwordDialog = PasswordDialogState()) }

    fun closePasswordDialog() = _uiState.update { if (it.passwordDialog?.saving == true) it else it.copy(passwordDialog = null) }

    fun onCurrentPasswordChange(value: String) = updatePasswordDialog { it.copy(currentPassword = value, error = null) }

    fun onNewPasswordChange(value: String) = updatePasswordDialog { it.copy(newPassword = value, error = null) }

    fun submitPassword() {
        val state = _uiState.value
        val dialog = state.passwordDialog ?: return
        val hasPassword = state.settings?.hasPassword == true
        if (dialog.saving || dialog.newPassword.isEmpty() || (hasPassword && dialog.currentPassword.isEmpty())) return
        updatePasswordDialog { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            attempt {
                if (hasPassword) {
                    securityRepository.changePassword(dialog.currentPassword, dialog.newPassword)
                } else {
                    securityRepository.setPassword(dialog.newPassword)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        passwordDialog = null,
                        message = UiText(if (hasPassword) R.string.security_password_changed else R.string.security_password_set),
                    )
                }
                if (!hasPassword) load()
            }.onFailure { error ->
                val message = when ((error as? ApiException)?.code) {
                    "INVALID_PASSWORD" -> R.string.security_wrong_password
                    "PASSWORD_TOO_SHORT" -> R.string.security_weak_password
                    else -> R.string.error_unknown
                }
                updatePasswordDialog { it.copy(saving = false, error = UiText(message)) }
            }
        }
    }

    fun addPasskey(credentials: CredentialProvider) {
        if (_uiState.value.registering) return
        _uiState.update { it.copy(registering = true) }
        viewModelScope.launch {
            attempt { passkeyRepository.begin(credentials) }
                .onSuccess { pending ->
                    _uiState.update {
                        it.copy(
                            registering = false,
                            passkeyNameDialog = pending?.let { p -> PasskeyNameDialogState(p, p.defaultName) },
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(registering = false, message = UiText(R.string.passkey_register_failed)) } }
        }
    }

    fun onPasskeyNameChange(name: String) = _uiState.update { it.copy(passkeyNameDialog = it.passkeyNameDialog?.copy(name = name)) }

    fun dismissPasskeyName() = _uiState.update { if (it.passkeyNameDialog?.saving == true) it else it.copy(passkeyNameDialog = null) }

    fun savePasskey() {
        val dialog = _uiState.value.passkeyNameDialog ?: return
        if (dialog.saving || dialog.name.isBlank()) return
        _uiState.update { it.copy(passkeyNameDialog = dialog.copy(saving = true)) }
        viewModelScope.launch {
            attempt { passkeyRepository.complete(dialog.pending, dialog.name.trim()) }
                .onSuccess {
                    _uiState.update { it.copy(passkeyNameDialog = null, message = UiText(R.string.passkey_registered)) }
                    load()
                }
                .onFailure {
                    _uiState.update { it.copy(passkeyNameDialog = dialog.copy(saving = false), message = UiText(R.string.passkey_register_failed)) }
                }
        }
    }

    fun askDeletePasskey(passkey: Passkey) = _uiState.update { it.copy(passkeyToDelete = passkey) }

    fun cancelDeletePasskey() = _uiState.update { it.copy(passkeyToDelete = null) }

    fun deletePasskey() {
        val passkey = _uiState.value.passkeyToDelete ?: return
        _uiState.update { it.copy(passkeyToDelete = null, deletingPasskeyId = passkey.id) }
        viewModelScope.launch {
            attempt { passkeyRepository.delete(passkey.id) }
                .onSuccess { load() }
                .onFailure { _uiState.update { it.copy(message = UiText(R.string.passkey_delete_failed)) } }
            _uiState.update { it.copy(deletingPasskeyId = null) }
        }
    }

    fun openDeleteDialog() = _uiState.update { it.copy(deleteDialog = DeleteDialogState()) }

    fun closeDeleteDialog() = _uiState.update { if (it.deleteDialog?.deleting == true) it else it.copy(deleteDialog = null) }

    fun onDeletePasswordChange(value: String) =
        _uiState.update { it.copy(deleteDialog = it.deleteDialog?.copy(password = value, error = null)) }

    fun deleteAccount() {
        val state = _uiState.value
        val dialog = state.deleteDialog ?: return
        val hasPassword = state.settings?.hasPassword == true
        if (dialog.deleting || (hasPassword && dialog.password.isEmpty())) return
        _uiState.update { it.copy(deleteDialog = dialog.copy(deleting = true, error = null)) }
        viewModelScope.launch {
            attempt { securityRepository.deleteAccount(dialog.password.takeIf { hasPassword }) }
                .onFailure {
                    _uiState.update {
                        it.copy(deleteDialog = dialog.copy(deleting = false, error = UiText(R.string.danger_reauth_required)))
                    }
                }
        }
    }

    private fun updatePasswordDialog(transform: (PasswordDialogState) -> PasswordDialogState) =
        _uiState.update { it.copy(passwordDialog = it.passwordDialog?.let(transform)) }
}
