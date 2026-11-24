package com.karasu256.karasulab.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.Account
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.ProfileRepository
import com.karasu256.karasulab.data.remote.ApiException
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AccountMenuBusy { None, Switching, SigningOut }

data class AccountMenuUiState(
    val busy: AccountMenuBusy = AccountMenuBusy.None,
    val removingId: String? = null,
    val message: UiText? = null,
)

@HiltViewModel
class AccountMenuViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    val accounts: StateFlow<List<Account>> = accountRepository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeAccount: StateFlow<Account?> = accountRepository.activeAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(AccountMenuUiState())
    val uiState: StateFlow<AccountMenuUiState> = _uiState.asStateFlow()

    private val _switched = Channel<Unit>(Channel.BUFFERED)
    val switched: Flow<Unit> = _switched.receiveAsFlow()

    fun switchTo(id: String) {
        if (_uiState.value.busy != AccountMenuBusy.None || id == activeAccount.value?.id) return
        _uiState.update { it.copy(busy = AccountMenuBusy.Switching) }
        viewModelScope.launch {
            val switched = attempt { accountRepository.switchTo(id) }.getOrDefault(false)
            _uiState.update {
                it.copy(busy = AccountMenuBusy.None, message = if (switched) null else UiText(R.string.account_switch_failed))
            }
            if (switched) _switched.send(Unit)
        }
    }

    fun remove(id: String) {
        if (_uiState.value.busy != AccountMenuBusy.None || _uiState.value.removingId != null) return
        val wasActive = id == activeAccount.value?.id
        _uiState.update { it.copy(removingId = id) }
        viewModelScope.launch {
            attempt { accountRepository.remove(id) }
            _uiState.update { it.copy(removingId = null) }
            if (wasActive && accountRepository.activeAccountId() != null) _switched.send(Unit)
        }
    }

    fun signOut() {
        if (_uiState.value.busy != AccountMenuBusy.None) return
        _uiState.update { it.copy(busy = AccountMenuBusy.SigningOut) }
        viewModelScope.launch {
            attempt { accountRepository.signOutActive() }
            _uiState.update { it.copy(busy = AccountMenuBusy.None) }
            if (accountRepository.activeAccountId() != null) _switched.send(Unit)
        }
    }

    fun resendVerification() {
        viewModelScope.launch {
            val message = attempt { profileRepository.resendVerificationEmail() }.fold(
                onSuccess = { UiText(R.string.email_unverified_sent) },
                onFailure = { error ->
                    if (error is ApiException && error.status == 429) {
                        UiText(R.string.email_unverified_rate_limited)
                    } else {
                        UiText(R.string.email_unverified_failed)
                    }
                },
            )
            _uiState.update { it.copy(message = message) }
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }
}
