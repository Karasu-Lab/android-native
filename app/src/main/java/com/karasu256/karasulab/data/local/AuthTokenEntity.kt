package com.karasu256.karasulab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_token")
data class AuthTokenEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val sessionCookieName: String,
    val sessionCookieValue: String,
    val sessionExpiresAtMs: Long,
    val customToken: String,
    val customTokenExpiresAtMs: Long,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
