package com.karasu256.karasulab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val email: String,
    val emailVerified: Boolean,
    val image: String?,
    val sessionCookieName: String,
    val sessionCookieValue: String,
    val sessionExpiresAtMs: Long,
    val isActive: Boolean,
    val addedAtMs: Long,
)
