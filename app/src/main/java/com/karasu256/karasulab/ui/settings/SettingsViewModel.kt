package com.karasu256.karasulab.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.Account
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.DeveloperOptionsStore
import com.karasu256.karasulab.data.ProfileRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    accountRepository: AccountRepository,
    private val profileRepository: ProfileRepository,
    private val developerOptionsStore: DeveloperOptionsStore,
) : ViewModel() {
    val activeAccount: StateFlow<Account?> = accountRepository.activeAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val developerOptionsEnabled: StateFlow<Boolean> = developerOptionsStore.enabled

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _toast = Channel<UiText>(Channel.BUFFERED)
    val toast: Flow<UiText> = _toast.receiveAsFlow()

    private var versionTapCount = 0
    private var lastVersionTapAt = 0L

    init {
        viewModelScope.launch {
            attempt { profileRepository.refresh() }
            _loading.value = false
        }
    }

    fun onVersionTapped() {
        if (developerOptionsStore.enabled.value) {
            _toast.trySend(UiText(R.string.developer_options_already_enabled))
            return
        }
        val now = System.currentTimeMillis()
        versionTapCount = if (now - lastVersionTapAt <= TAP_TIMEOUT_MS) versionTapCount + 1 else 1
        lastVersionTapAt = now
        when {
            versionTapCount >= REQUIRED_TAPS -> {
                versionTapCount = 0
                developerOptionsStore.setEnabled(true)
                _toast.trySend(UiText(R.string.developer_options_enabled))
            }
            versionTapCount >= 2 -> {
                _toast.trySend(UiText(R.string.developer_options_tap_remaining, listOf(REQUIRED_TAPS - versionTapCount)))
            }
        }
    }

    companion object {
        private const val REQUIRED_TAPS = 5
        private const val TAP_TIMEOUT_MS = 1_000L
    }
}
