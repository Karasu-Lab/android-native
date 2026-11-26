package com.karasu256.karasulab.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SocialSignInRequest(
    val provider: String,
    val idToken: IdTokenPayload,
)

@Serializable
data class OneTimeTokenRequest(
    val token: String,
)

@Serializable
data class OAuthLinkRequest(
    val provider: String,
    @SerialName("callbackURL")
    val callbackURL: String,
)

@Serializable
data class OAuthLinkResponse(
    val url: String,
    val redirect: Boolean = false,
)

@Serializable
data class IdTokenPayload(
    val token: String,
)

@Serializable
data class OAuthProvidersResponse(
    val providers: List<OAuthProviderResponse>,
)

@Serializable
data class OAuthProviderResponse(
    val id: String,
    val label: String,
    val kind: String,
    val signIn: Boolean,
    val link: Boolean,
    val backgroundColor: OAuthProviderColors,
    val foregroundColor: OAuthProviderColors,
    val iconUrl: String,
)

@Serializable
data class OAuthProviderColors(
    val default: String,
    val dark: String,
)

@Serializable
data class PasskeyVerifyRequest(
    val response: JsonElement,
)

@Serializable
data class PasskeyRegisterRequest(
    val response: JsonElement,
    val name: String,
)

@Serializable
data class DeletePasskeyRequest(
    val id: String,
)

@Serializable
data class PasskeyResponse(
    val id: String,
    val name: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class GetSessionResponse(
    val user: SessionUser,
)

@Serializable
data class SessionUser(
    val id: String,
    val name: String? = null,
    val email: String = "",
    val emailVerified: Boolean = true,
    val image: String? = null,
)

@Serializable
data class MetaResponse(
    val apiUrl: String,
    val gitBranch: String,
)

@Serializable
data class ProfileResponse(
    val id: String,
    val name: String? = null,
    val image: String? = null,
)

@Serializable
data class SignInLookupResponse(
    val sso: Boolean = false,
    val hasPassword: Boolean = false,
    val hasPasskey: Boolean = false,
)

@Serializable
data class RateLimitBody(
    val retryAfterSeconds: Int? = null,
)

@Serializable
data class EmailChallengeRequest(
    val email: String,
)

@Serializable
data class VerifyOtpRequest(
    val otp: String,
)

@Serializable
data class ErrorBody(
    val code: String? = null,
    val message: String? = null,
    val error: String? = null,
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

@Serializable
data class SetPasswordRequest(
    val newPassword: String,
)

@Serializable
data class DeleteUserRequest(
    val password: String? = null,
)

@Serializable
data class SecuritySettingsResponse(
    val hasPassword: Boolean,
    val passkeys: List<PasskeyResponse> = emptyList(),
)

@Serializable
data class AuthorizedAppSummaryResponse(
    val id: String,
    val clientId: String,
    val clientName: String? = null,
    val createdAt: String,
)

@Serializable
data class AuthorizedAppDetailResponse(
    val id: String,
    val clientId: String,
    val clientName: String? = null,
    val logoUri: String? = null,
    val scopes: List<String> = emptyList(),
    val createdAt: String,
)

@Serializable
data class DeleteConsentRequest(
    val id: String,
)

@Serializable
data class ProviderProfileResponse(
    val name: String? = null,
    val email: String? = null,
    val accountId: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class UnlinkAccountRequest(
    val providerId: String,
)

@Serializable
data class LinkSocialRequest(
    val provider: String,
    val idToken: IdTokenPayload,
)

@Serializable
data class SessionEntryResponse(
    val id: String,
    val token: String,
    val createdAt: String,
    val ipAddress: String? = null,
    val userAgent: String? = null,
)

@Serializable
data class RevokeSessionRequest(
    val token: String,
)

@Serializable
data class PasskeyOptionsUser(
    val name: String? = null,
    val displayName: String? = null,
)

@Serializable
data class PasskeyRegisterOptions(
    val user: PasskeyOptionsUser = PasskeyOptionsUser(),
)

@Serializable
data class UpdateNameRequest(
    val name: String,
)

@Serializable
data class UpdateImageRequest(
    val image: JsonElement,
)

@Serializable
data class ProfileImageResponse(
    val image: String,
)
