package com.karasu256.karasulab.data.remote

import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/sign-in/social")
    suspend fun signInSocial(@Body body: SocialSignInRequest): Response<ResponseBody>

    @GET("api/sign-in/lookup")
    suspend fun lookupSignIn(@Query("email") email: String): Response<ResponseBody>

    @POST("api/auth/sign-in/email-challenge")
    suspend fun startEmailChallenge(@Body body: EmailChallengeRequest): Response<ResponseBody>

    @POST("api/auth/email-challenge/verify-otp")
    suspend fun verifyEmailOtp(
        @Header("Cookie") cookie: String,
        @Body body: VerifyOtpRequest,
    ): Response<ResponseBody>

    @GET("api/auth/passkey/generate-authenticate-options")
    suspend fun generatePasskeyAuthenticateOptions(): Response<ResponseBody>

    @POST("api/auth/passkey/verify-authentication")
    suspend fun verifyPasskeyAuthentication(
        @Header("Cookie") cookie: String,
        @Body body: PasskeyVerifyRequest,
    ): Response<ResponseBody>

    @GET("api/auth/passkey/generate-register-options")
    suspend fun generatePasskeyRegisterOptions(
        @Header("Cookie") cookie: String,
        @Query("name") name: String? = null,
    ): Response<ResponseBody>

    @POST("api/auth/passkey/verify-registration")
    suspend fun verifyPasskeyRegistration(
        @Header("Cookie") cookie: String,
        @Body body: PasskeyRegisterRequest,
    ): Response<ResponseBody>

    @GET("api/auth/passkey/list-user-passkeys")
    suspend fun listPasskeys(@Header("Cookie") cookie: String): List<PasskeyResponse>

    @POST("api/auth/passkey/delete-passkey")
    suspend fun deletePasskey(
        @Header("Cookie") cookie: String,
        @Body body: DeletePasskeyRequest,
    ): Response<ResponseBody>

    @GET("api/auth/ok")
    suspend fun ok()

    @GET("api/auth/get-session")
    suspend fun getSession(@Header("Cookie") cookie: String): Response<ResponseBody>

    @POST("api/auth/sign-out")
    suspend fun signOut(
        @Header("Cookie") cookie: String,
        @Body body: JsonObject = JsonObject(emptyMap()),
    ): Response<ResponseBody>

    @POST("api/auth/update-user")
    suspend fun updateUserName(
        @Header("Cookie") cookie: String,
        @Body body: UpdateNameRequest,
    ): Response<ResponseBody>

    @POST("api/auth/update-user")
    suspend fun updateUserImage(
        @Header("Cookie") cookie: String,
        @Body body: UpdateImageRequest,
    ): Response<ResponseBody>

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Cookie") cookie: String,
        @Body body: ChangePasswordRequest,
    ): Response<ResponseBody>

    @POST("api/auth/set-password")
    suspend fun setPassword(
        @Header("Cookie") cookie: String,
        @Body body: SetPasswordRequest,
    ): Response<ResponseBody>

    @POST("api/auth/delete-user")
    suspend fun deleteUser(
        @Header("Cookie") cookie: String,
        @Body body: DeleteUserRequest,
    ): Response<ResponseBody>

    @POST("api/email-verification/resend")
    suspend fun resendVerificationEmail(@Header("Cookie") cookie: String): Response<ResponseBody>

    @GET("api/users/me/security")
    suspend fun securitySettings(@Header("Cookie") cookie: String): SecuritySettingsResponse

    @GET("api/users/me/authorized-apps")
    suspend fun authorizedApps(@Header("Cookie") cookie: String): List<AuthorizedAppSummaryResponse>

    @GET("api/users/me/authorized-apps/{id}")
    suspend fun authorizedApp(
        @Header("Cookie") cookie: String,
        @Path("id") id: String,
    ): AuthorizedAppDetailResponse

    @POST("api/auth/oauth2/delete-consent")
    suspend fun deleteConsent(
        @Header("Cookie") cookie: String,
        @Body body: DeleteConsentRequest,
    ): Response<ResponseBody>

    @GET("api/users/me/providers")
    suspend fun linkedProviders(@Header("Cookie") cookie: String): Map<String, ProviderProfileResponse?>

    @POST("api/auth/unlink-account")
    suspend fun unlinkAccount(
        @Header("Cookie") cookie: String,
        @Body body: UnlinkAccountRequest,
    ): Response<ResponseBody>

    @POST("api/auth/link-social")
    suspend fun linkSocial(
        @Header("Cookie") cookie: String,
        @Body body: LinkSocialRequest,
    ): Response<ResponseBody>

    @GET("api/auth/list-sessions")
    suspend fun listSessions(@Header("Cookie") cookie: String): List<SessionEntryResponse>

    @POST("api/auth/revoke-session")
    suspend fun revokeSession(
        @Header("Cookie") cookie: String,
        @Body body: RevokeSessionRequest,
    ): Response<ResponseBody>
}
