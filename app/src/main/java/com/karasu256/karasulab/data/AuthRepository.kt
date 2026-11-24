package com.karasu256.karasulab.data

import com.karasu256.karasulab.data.auth.AccountAlreadySignedInException
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
                    runCatching { json.decodeFromString<RateLimitBody>(body.string()).retryAfterSeconds }.getOrNull()
                } ?: response.headers()["Retry-After"]?.toIntOrNull()?.takeIf { it > 0 } ?: 60
                LookupOutcome.RateLimited(minutes = maxOf(1, (seconds + 59) / 60), exact = seconds % 60 == 0)
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
        signIn(rejectKnown) {
            authApi.signInSocial(SocialSignInRequest(provider = "google", idToken = IdTokenPayload(idToken)))
        }

    suspend fun requestPasskeyChallenge(): PasskeyChallenge {
        val response = authApi.generatePasskeyAuthenticateOptions()
        if (!response.isSuccessful) throw HttpException(response)
        val options = response.bodyString()
        val cookieHeader = response.setCookies().joinToString("; ") { "${it.name}=${it.value}" }
        return PasskeyChallenge(optionsJson = options, cookieHeader = cookieHeader)
    }

    suspend fun signInWithPasskey(
        challenge: PasskeyChallenge,
        responseJson: String,
        expectedEmail: String? = null,
        rejectKnown: Boolean = false,
    ): SignInResult {
        val result = signIn(rejectKnown) {
            authApi.verifyPasskeyAuthentication(
                cookie = challenge.cookieHeader,
                body = PasskeyVerifyRequest(json.parseToJsonElement(responseJson)),
            )
        }
        if (result != SignInResult.Success || expectedEmail == null) return result
        val account = store.currentAccount() ?: return SignInResult.Failed
        if (account.email.equals(expectedEmail.trim(), ignoreCase = true)) return result
        revoke(SessionCookie(account.sessionCookieName, account.sessionCookieValue, account.sessionExpiresAtMs))
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
                ChallengeOutcome.Started(EmailChallenge(response.setCookies().joinToString("; ") { "${it.name}=${it.value}" }))
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        ChallengeOutcome.Failed
    }

    suspend fun verifyEmailOtp(challenge: EmailChallenge, otp: String, rejectKnown: Boolean = false): SignInResult {
        val response = try {
            authApi.verifyEmailOtp(challenge.cookieHeader, VerifyOtpRequest(otp.trim().uppercase()))
        } catch (e: CancellationException) {
            throw e
        } catch (_: IOException) {
            return SignInResult.NetworkError
        }
        if (!response.isSuccessful) {
            val code = response.errorBody()?.use { body ->
                runCatching { json.decodeFromString<ErrorBody>(body.string()).code }.getOrNull()
            }
            return when (response.code()) {
                400, 401, 429 -> if (code == "INVALID_OTP") SignInResult.InvalidCode else SignInResult.ExpiredCode
                else -> SignInResult.Failed
            }
        }
        response.body()?.close()
        return finish(response, rejectKnown)
    }

    private suspend fun signIn(rejectKnown: Boolean, request: suspend () -> Response<ResponseBody>): SignInResult = try {
        val response = request()
        response.body()?.close()
        response.errorBody()?.close()
        when {
            response.code() in 400..403 -> SignInResult.InvalidCredentials
            !response.isSuccessful -> SignInResult.Failed
            else -> finish(response, rejectKnown)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: IOException) {
        SignInResult.NetworkError
    } catch (_: Exception) {
        SignInResult.Failed
    }

    private suspend fun finish(response: Response<ResponseBody>, rejectKnown: Boolean): SignInResult {
        val cookie = SessionCookie.from(response) ?: return SignInResult.Failed
        return try {
            exchanger.exchange(cookie, rejectKnown)?.let { SignInResult.Success } ?: SignInResult.Failed
        } catch (e: AccountAlreadySignedInException) {
            revoke(e.cookie)
            SignInResult.AccountAlreadySignedIn
        }
    }

    private suspend fun revoke(cookie: SessionCookie) {
        try {
            authApi.signOut(cookie.toHeader()).also {
                it.body()?.close()
                it.errorBody()?.close()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }
}
