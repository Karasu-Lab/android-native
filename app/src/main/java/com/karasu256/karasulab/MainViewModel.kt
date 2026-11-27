package com.karasu256.karasulab

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.data.SignInResult
import com.karasu256.karasulab.data.auth.NativeOAuthCallbackUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    accountRepository: AccountRepository,
) : ViewModel() {
    val isSignedIn: StateFlow<Boolean?> = authRepository.isSignedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { attempt { accountRepository.ensureActiveAccount() } }
    }

    fun onOAuthCallback(uri: String?) {
        if (uri == null || NativeOAuthCallbackUri.parse(uri) == null) return
        viewModelScope.launch {
            attempt { authRepository.completeOAuthCallback(uri) }
                .onSuccess { result ->
                    if (result != SignInResult.Success) {
                        Log.e("MainViewModel", "Native OAuth callback did not complete successfully: $result")
                        FirebaseCrashlytics.getInstance().log("Native OAuth callback did not complete successfully: $result")
                    }
                }
                .onFailure { error ->
                    Log.e("MainViewModel", "Could not complete native OAuth callback", error)
                    FirebaseCrashlytics.getInstance().log("Could not complete native OAuth callback")
                    FirebaseCrashlytics.getInstance().recordException(error)
                }
        }
    }
}
