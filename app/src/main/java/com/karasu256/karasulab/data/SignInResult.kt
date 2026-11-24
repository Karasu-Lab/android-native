package com.karasu256.karasulab.data

sealed interface SignInResult {
    data object Success : SignInResult

    data object InvalidCredentials : SignInResult

    data object NetworkError : SignInResult

    data object Failed : SignInResult

    data object AccountAlreadySignedIn : SignInResult

    data object PasskeyMismatch : SignInResult

    data object InvalidCode : SignInResult

    data object ExpiredCode : SignInResult
}

data class PasskeyChallenge(
    val optionsJson: String,
    val cookieHeader: String,
)

data class EmailChallenge(
    val cookieHeader: String,
)

data class SignInLookup(
    val sso: Boolean,
    val hasPassword: Boolean,
    val hasPasskey: Boolean,
)

sealed interface LookupOutcome {
    data class Found(val lookup: SignInLookup) : LookupOutcome

    data object NotFound : LookupOutcome

    data class RateLimited(val minutes: Int, val exact: Boolean) : LookupOutcome
}

sealed interface ChallengeOutcome {
    data class Started(val challenge: EmailChallenge) : ChallengeOutcome

    data object RateLimited : ChallengeOutcome

    data object Failed : ChallengeOutcome
}
