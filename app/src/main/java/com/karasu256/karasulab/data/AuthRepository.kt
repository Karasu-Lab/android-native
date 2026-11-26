package com.karasu256.karasulab.data

import android.util.Log
import com.karasu256.karasulab.data.auth.AccountAlreadySignedInException
import com.karasu256.karasulab.data.auth.OAuthRedirect
import com.karasu256.karasulab.data.auth.SessionTokenExchanger
import com.karasu256.karasulab.data.local.SessionStore
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.EmailChallengeRequest
import com.karasu256.karasulab.data.remote.ErrorBody
import com.karasu256.karasulab.data.remote.IdTokenPayload
import com.karasu256.karasulab.data.remote.PasskeyVerifyRequest
import com.karasu256.karasulab.data.remote.RateLimitBody
import com.karasu256.karasulab.data.remote.SessionCookie
import com.karasu256.karasulab.data.remote.SignInLookupResponse
import com.karasu256.karasulab.data.remote.SocialSignInRequest
import com.karasu256.karasulab.data.remote.VerifyOtpRequest
import com.karasu256.karasulab.data.remote.bodyString
import com.karasu256.karasulab.data.remote.cookieHeader
import com.karasu256.karasulab.data.remote.setCookies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val store: SessionStore,
    private val exchanger: SessionTokenExchanger,
    private val json: Json,
    private val oauthRedirect: OAuthRedirect,
) {
    val isSignedIn: Flow<Boolean> = store.token.map { it != null }.distinctUntilChanged()

    suspend fun lookup(email: String): LookupOutcome {
        val response = authApi.lookupSignIn(email)
        return when {
            response.code() == 404 -> {
                response.errorBody()?.close()
                LookupOutcome.NotFound
            }

            response.code() == 429 -> {
                val seconds = response.errorBody()?.use { body ->
                    runCatching { json.decodeFromString<RateLimitBody>(body.string()).retryAfterSeconds }
                        .onFailure { Log.e("AuthRepository", "Unreadable rate limit body", it) }
                        .getOrNull()
                } ?: response.headers()["Retry-After"]?.toIntOrNull()?.takeIf { it > 0 } ?: 60
                LookupOutcome.RateLimited(
                    minutes = maxOf(1, (seconds + 59) / 60),
                    exact = seconds % 60 == 0
                )
            }

            !response.isSuccessful -> {
                response.errorBody()?.close()
                throw HttpException(response)
            }

            else -> {
                val parsed = json.decodeFromString<SignInLookupResponse>(response.bodyString())
                LookupOutcome.Found(SignInLookup(parsed.sso, parsed.hasPassword, parsed.hasPasskey))
            }
        }
    }

    suspend fun signInWithGoogle(idToken: String, rejectKnown: Boolean = false): SignInResult =
        signIn("Google", rejectKnown) {
            authApi.signInSocial(
                SocialSignInRequest(
                    provider = "google",
                    idToken = IdTokenPayload(idToken)
                )
            )
        }

    suspend fun completeSocialSignIn(
        token: String,
        state: String,
        rejectKnown: Boolean
    ): SignInResult = try {
        val cookie = oauthRedirect.exchangeToken(token, state)
        if (cookie == null) {
            Log.e("AuthRepository", "The one-time token response set no session cookie")
            SignInResult.Failed
        } else {
            exchangeSession(cookie, rejectKnown)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Log.e("AuthRepository", "Social sign-in failed on the network: $e", e)
        SignInResult.NetworkError
    } catch (e: Exception) {
        Log.e("AuthRepository", "Social sign-in failed: $e", e)
        SignInResult.Failed
    }

    suspend fun requestPasskeyChallenge(): PasskeyChallenge {
        val response = authApi.generatePasskeyAuthenticateOptions()
        if (!response.isSuccessful) throw HttpException(response)
        val options = response.bodyString()
        return PasskeyChallenge(optionsJson = options, cookieHeader = response.cookieHeader())
    }

    suspend fun signInWithPasskey(
        challenge: PasskeyChallenge,
        responseJson: String,
        expectedEmail: String? = null,
        rejectKnown: Boolean = false,
    ): SignInResult {
        val result = signIn("Passkey", rejectKnown) {
            authApi.verifyPasskeyAuthentication(
                cookie = challenge.cookieHeader,
                body = PasskeyVerifyRequest(json.parseToJsonElement(responseJson)),
            )
        }
        if (result != SignInResult.Success || expectedEmail == null) return result
        val account = store.currentAccount() ?: run {
            Log.e("AuthRepository", "No active account after the passkey sign-in")
            return SignInResult.Failed
        }
        if (account.email.equals(expectedEmail.trim(), ignoreCase = true)) return result
        revoke(
            SessionCookie(
                account.sessionCookieName,
                account.sessionCookieValue,
                account.sessionExpiresAtMs
            )
        )
        store.removeAccount(account.id)
        return SignInResult.PasskeyMismatch
    }

    suspend fun startEmailChallenge(email: String): ChallengeOutcome = try {
        val response = authApi.startEmailChallenge(EmailChallengeRequest(email))
        when {
            response.code() == 429 -> {
                response.errorBody()?.close()
                ChallengeOutcome.RateLimited
            }

            !response.isSuccessful -> {
                response.errorBody()?.close()
                ChallengeOutcome.Failed
            }

            else -> {
                response.body()?.close()
                ChallengeOutcome.Started(EmailChallenge(response.cookieHeader()))
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("AuthRepository", "Could not start the email challenge: $e", e)
        ChallengeOutcome.Failed
    }

    suspend fun verifyEmailOtp(
        challenge: EmailChallenge,
        otp: String,
        rejectKnown: Boolean = false
    ): SignInResult {
        val response = try {
            authApi.verifyEmailOtp(challenge.cookieHeader, VerifyOtpRequest(otp.trim().uppercase()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Log.e("AuthRepository", "Could not verify the email code: $e", e)
            return SignInResult.NetworkError
        }
        if (!response.isSuccessful) {
            val code = response.errorBody()?.use { body ->
                runCatching { json.decodeFromString<ErrorBody>(body.string()).code }
                    .onFailure { Log.e("AuthRepository", "Unreadable email code error body", it) }
                    .getOrNull()
            }
            return when (response.code()) {
                400, 401, 429 -> if (code == "INVALID_OTP") SignInResult.InvalidCode else SignInResult.ExpiredCode
                else -> SignInResult.Failed
            }
        }
        response.body()?.close()
        return finish(response, rejectKnown)
    }

    private suspend fun signIn(
        method: String,
        rejectKnown: Boolean,
        request: suspend () -> Response<ResponseBody>
    ): SignInResult = try {
        val response = request()
        response.body()?.close()
        response.errorBody()?.close()
        when {
            response.code() in 400..403 -> {
                Log.e("AuthRepository", "$method sign-in was rejected with ${response.code()}")
                SignInResult.InvalidCredentials
            }

            !response.isSuccessful -> {
                Log.e("AuthRepository", "$method sign-in failed with ${response.code()}")
                SignInResult.Failed
            }

            else -> finish(response, rejectKnown)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Log.e("AuthRepository", "$method sign-in failed on the network: $e", e)
        SignInResult.NetworkError
    } catch (e: Exception) {
        Log.e("AuthRepository", "$method sign-in failed: $e", e)
        SignInResult.Failed
    }

    private suspend fun finish(
        response: Response<ResponseBody>,
        rejectKnown: Boolean
    ): SignInResult {
        val cookie = SessionCookie.from(response) ?: run {
            Log.e(
                "AuthRepository",
                "The sign-in response set no session cookie: ${response.code()} " +
                    "set-cookie names=${response.headers().values("Set-Cookie").map { it.substringBefore('=') }} " +
                    "parsed names=${response.setCookies().map { it.name }}"
            )
            return SignInResult.Failed
        }
        return exchangeSession(cookie, rejectKnown)
    }

    private suspend fun exchangeSession(cookie: SessionCookie, rejectKnown: Boolean): SignInResult =
        try {
            exchanger.exchange(cookie, rejectKnown)?.let { SignInResult.Success } ?: run {
                Log.e("AuthRepository", "The session could not be exchanged for a custom token")
                SignInResult.Failed
            }
        } catch (e: AccountAlreadySignedInException) {
            revoke(e.cookie)
            SignInResult.AccountAlreadySignedIn
        }

    private suspend fun revoke(cookie: SessionCookie) {
        try {
            authApi.signOut(cookie.toHeader()).also {
                it.body()?.close()
                it.errorBody()?.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("AuthRepository", "Could not revoke the session: $e", e)
        }
    }
}
