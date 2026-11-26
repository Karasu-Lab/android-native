package com.karasu256.karasulab.ui.signin

import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.OAuthProvider
import com.karasu256.karasulab.data.SignInResult
import com.karasu256.karasulab.ui.UiText
import com.karasu256.karasulab.ui.component.ProviderMethod

enum class SignInStep { Email, Passkey, Methods, Code }

data class SignInUiState(
    val step: SignInStep = SignInStep.Email,
    val addingAccount: Boolean = false,
    val email: String = "",
    val code: String = "",
    val busy: Boolean = false,
    val busyMethod: ProviderMethod? = null,
    val busyProviderId: String? = null,
    val providers: List<OAuthProvider> = emptyList(),
    val providersLoading: Boolean = true,
    val providersError: UiText? = null,
    val emailError: UiText? = null,
    val passkeyError: UiText? = null,
    val methodsError: UiText? = null,
    val codeError: UiText? = null,
)

fun SignInResult.toError(fallback: Int): UiText? = when (this) {
    SignInResult.Success -> null
    SignInResult.NetworkError -> UiText(R.string.error_network)
    SignInResult.AccountAlreadySignedIn -> UiText(R.string.error_account_already_signed_in)
    SignInResult.PasskeyMismatch -> UiText(R.string.error_passkey_mismatch)
    SignInResult.InvalidCode -> UiText(R.string.code_invalid)
    SignInResult.ExpiredCode -> UiText(R.string.code_expired)
    SignInResult.InvalidCredentials, SignInResult.Failed -> UiText(fallback)
}
