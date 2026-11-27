package com.karasu256.karasulab.ui.signin

import android.net.Uri
import com.karasu256.karasulab.logging.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialProvider
import com.karasu256.karasulab.data.AccountRepository
import com.karasu256.karasulab.data.AuthRepository
import com.karasu256.karasulab.data.ChallengeOutcome
import com.karasu256.karasulab.data.EmailChallenge
import com.karasu256.karasulab.data.LookupOutcome
import com.karasu256.karasulab.data.SignInResult
import com.karasu256.karasulab.data.auth.OAuthCallback
import com.karasu256.karasulab.data.auth.OAuthCallbackHub
import com.karasu256.karasulab.data.remote.attempt
import com.karasu256.karasulab.ui.UiText
import com.karasu256.karasulab.ui.component.ProviderMethod
import com.karasu256.karasulab.ui.toUiText
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

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    callbackHub: OAuthCallbackHub,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { callbackHub.callbacks.collect(::onOAuthCallback) }
    }

    private val _signedIn = Channel<Unit>(Channel.BUFFERED)
    val signedIn: Flow<Unit> = _signedIn.receiveAsFlow()

    private val _openBrowser = Channel<String>(Channel.BUFFERED)
    val openBrowser: Flow<String> = _openBrowser.receiveAsFlow()

    private var challenge: EmailChallenge? = null
    private var previousAccountId: String? = null
    private var switchToNewAccount = false

    init {
        viewModelScope.launch {
            authRepository.oauthSignInInProgress.collect { inProgress ->
                _uiState.update { it.copy(completingOAuth = inProgress) }
            }
        }
        viewModelScope.launch {
            authRepository.oauthCompletions.collect { completion ->
                if (completion.isLinking) return@collect
                if (completion.result == SignInResult.Success) {
                    finish()
                } else {
                    Log.e("SignInViewModel", "Native OAuth failed for ${completion.providerId}: ${completion.result}")
                    _uiState.update { it.copy(emailError = completion.result.toError(R.string.error_unknown)) }
                }
            }
        }
    }

    fun configure(addAccount: Boolean, switchToNew: Boolean) {
        if (!addAccount || _uiState.value.addingAccount) return
        switchToNewAccount = switchToNew
        _uiState.update { it.copy(addingAccount = true) }
        viewModelScope.launch { previousAccountId = accountRepository.activeAccountId() }
    }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, emailError = null) }

    fun onCodeChange(code: String) = _uiState.update { it.copy(code = code, codeError = null) }

    fun goBack(): Boolean {
        val state = _uiState.value
        if (state.busy) return true
        if (state.step == SignInStep.Email) return false
        _uiState.update { it.copy(step = SignInStep.Email) }
        return true
    }

    fun goToMethods() = _uiState.update { it.copy(step = SignInStep.Methods, methodsError = null) }

    fun submitEmail() {
        val state = _uiState.value
        if (state.email.isBlank() || state.step != SignInStep.Email) return
        launchBusy {
            _uiState.update { it.copy(emailError = null, passkeyError = null, methodsError = null) }
            val outcome = attempt { authRepository.lookup(state.email.trim()) }.getOrElse { error ->
                _uiState.update { it.copy(emailError = error.toUiText()) }
                return@launchBusy
            }
            when (outcome) {
                is LookupOutcome.RateLimited -> _uiState.update {
                    val message = if (outcome.exact) R.string.error_rate_limited_within else R.string.error_rate_limited_less_than
                    it.copy(emailError = UiText(message, listOf(outcome.minutes)))
                }
                LookupOutcome.NotFound -> _uiState.update { it.copy(emailError = UiText(R.string.error_account_not_found)) }
                is LookupOutcome.Found -> when {
                    outcome.lookup.sso -> _uiState.update { it.copy(emailError = UiText(R.string.error_sso_unsupported)) }
                    outcome.lookup.hasPasskey -> _uiState.update { it.copy(step = SignInStep.Passkey) }
                    else -> beginChallenge(SignInStep.Email)
                }
            }
        }
    }

    fun startCode() {
        val step = _uiState.value.step
        launchBusy { beginChallenge(step) }
    }

    fun signInWithPasskey(credentials: CredentialProvider, fromStep: Boolean) {
        val email = _uiState.value.email.takeIf { fromStep }
        launchBusy(if (fromStep) null else ProviderMethod.Passkey) {
            if (fromStep) _uiState.update { it.copy(passkeyError = null) }
            val result = attempt {
                val passkeyChallenge = authRepository.requestPasskeyChallenge()
                val assertion = credentials.requestPasskeyAssertion(passkeyChallenge.optionsJson) ?: return@attempt null
                authRepository.signInWithPasskey(passkeyChallenge, assertion, email, _uiState.value.addingAccount)
            }.getOrElse { error ->
                reportPasskeyError(fromStep, error.toUiText(R.string.error_passkey_failed))
                return@launchBusy
            } ?: return@launchBusy
            result.toError(R.string.error_passkey_failed)?.let { reportPasskeyError(fromStep, it) }
            if (result == SignInResult.Success) finish()
        }
    }

    fun signInWithGoogle(credentials: CredentialProvider) {
        launchBusy(ProviderMethod.Google) {
            val result = attempt {
                val idToken = credentials.requestGoogleIdToken() ?: return@attempt null
                authRepository.signInWithGoogle(idToken, _uiState.value.addingAccount)
            }.getOrElse { error ->
                _uiState.update { it.copy(emailError = error.toUiText(R.string.error_google_failed)) }
                return@launchBusy
            } ?: return@launchBusy
            result.toError(R.string.error_google_failed)?.let { error -> _uiState.update { it.copy(emailError = error) } }
            if (result == SignInResult.Success) finish()
        }
    }

    private fun onOAuthCallback(uri: Uri) {
        _uiState.update { it.copy(completingOAuth = true) }
        val token = OAuthCallback.token(uri)
        val state = OAuthCallback.state(uri)
        if (OAuthCallback.error(uri) != null || token == null || state == null) {
            failOAuth()
            return
        }
        launchBusy {
            val outcome = authRepository.completeSocialSignIn(token, state, _uiState.value.addingAccount)
            outcome.toError(R.string.error_oauth_failed)?.let(::failOAuth)
            if (outcome == SignInResult.Success) finish()
        }
    }

    private fun failOAuth(error: UiText = UiText(R.string.error_oauth_failed)) =
        _uiState.update { it.copy(emailError = error, completingOAuth = false) }

    fun signInWithOAuth(providerId: String) {
        launchBusy(providerId = providerId) {
            val url = attempt { authRepository.startOAuthSignIn(providerId, _uiState.value.addingAccount) }
                .getOrElse { error ->
                    Log.e("SignInViewModel", "Could not start $providerId sign-in", error)
                    _uiState.update { it.copy(emailError = error.toUiText(R.string.error_unknown)) }
                    return@launchBusy
                }
            _openBrowser.send(url)
        }
    }
    fun submitCode() {
        val state = _uiState.value
        val current = challenge ?: return
        if (state.code.length != 6 || state.step != SignInStep.Code) return
        launchBusy {
            val result = attempt { authRepository.verifyEmailOtp(current, state.code, state.addingAccount) }
                .getOrElse { error ->
                    _uiState.update { it.copy(codeError = error.toUiText()) }
                    return@launchBusy
                }
            _uiState.update { it.copy(codeError = result.toError(R.string.error_unknown)) }
            if (result == SignInResult.Success) finish()
        }
    }

    private suspend fun beginChallenge(from: SignInStep) {
        _uiState.update { it.copy(code = "", codeError = null, methodsError = null) }
        val error = when (val outcome = authRepository.startEmailChallenge(_uiState.value.email.trim())) {
            is ChallengeOutcome.Started -> {
                challenge = outcome.challenge
                _uiState.update { it.copy(step = SignInStep.Code) }
                return
            }
            ChallengeOutcome.RateLimited -> UiText(R.string.error_rate_limited_less_than, listOf(1))
            ChallengeOutcome.Failed -> UiText(R.string.error_unknown)
        }
        _uiState.update {
            when (from) {
                SignInStep.Code -> it.copy(codeError = error)
                SignInStep.Methods -> it.copy(methodsError = error)
                else -> it.copy(emailError = error)
            }
        }
    }

    private fun reportPasskeyError(fromStep: Boolean, error: UiText) {
        _uiState.update { if (fromStep) it.copy(passkeyError = error) else it.copy(emailError = error) }
    }

    private suspend fun finish() {
        if (!switchToNewAccount && _uiState.value.addingAccount) previousAccountId?.let { accountRepository.switchTo(it) }
        _signedIn.send(Unit)
    }

    private fun launchBusy(method: ProviderMethod? = null, providerId: String? = null, block: suspend () -> Unit) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, busyMethod = method, busyProviderId = providerId) }
        viewModelScope.launch {
            try {
                block()
            } finally {
                _uiState.update { it.copy(busy = false, busyMethod = null, busyProviderId = null) }
            }
        }
    }
}
