package com.karasu256.karasulab.data

data class Account(
    val id: String,
    val name: String?,
    val email: String,
    val emailVerified: Boolean,
    val image: String?,
    val isActive: Boolean,
) {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: email
}

data class Passkey(
    val id: String,
    val name: String?,
    val createdOn: String?,
)

data class SecuritySettings(
    val hasPassword: Boolean,
    val passkeys: List<Passkey>,
)

data class ProviderProfile(
    val name: String?,
    val email: String?,
    val accountId: String?,
    val avatarUrl: String?,
)

data class AuthorizedApp(
    val id: String,
    val clientId: String,
    val clientName: String?,
    val createdAt: String,
)

data class AuthorizedAppDetail(
    val id: String,
    val clientId: String,
    val clientName: String?,
    val logoUri: String?,
    val scopes: List<String>,
    val createdAt: String,
)

data class ActiveSession(
    val id: String,
    val token: String,
    val createdAt: String,
    val ipAddress: String?,
    val userAgent: String?,
    val current: Boolean,
)
