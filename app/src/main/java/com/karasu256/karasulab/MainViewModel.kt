package com.karasu256.karasulab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.remote.attempt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    accountRepository: AccountRepository,
) : ViewModel() {
    val isSignedIn: StateFlow<Boolean?> = authRepository.isSignedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { attempt { accountRepository.ensureActiveAccount() } }
    }
}
