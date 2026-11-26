package com.karasu256.karasulab.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PendingNativeOAuthState(
    val rejectKnown: Boolean,
    val isLinking: Boolean,
)

@Singleton
class NativeOAuthStateStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences("native_oauth", Context.MODE_PRIVATE)

    fun save(state: String, providerId: String, rejectKnown: Boolean, isLinking: Boolean = false) {
        synchronized(lock) {
            check(
                preferences.edit()
                    .putString(STATE_KEY, state)
                    .putString(PROVIDER_ID_KEY, providerId)
                    .putBoolean(REJECT_KNOWN_KEY, rejectKnown)
                    .putBoolean(IS_LINKING_KEY, isLinking)
                    .commit(),
            ) {
                "Could not persist the native OAuth state"
            }
        }
    }

    fun consume(state: String, providerId: String): PendingNativeOAuthState? = synchronized(lock) {
        if (
            preferences.getString(STATE_KEY, null) != state ||
            preferences.getString(PROVIDER_ID_KEY, null) != providerId
        ) return@synchronized null
        val pending = PendingNativeOAuthState(
            rejectKnown = preferences.getBoolean(REJECT_KNOWN_KEY, false),
            isLinking = preferences.getBoolean(IS_LINKING_KEY, false),
        )
        check(
            preferences.edit()
                .remove(STATE_KEY)
                .remove(PROVIDER_ID_KEY)
                .remove(REJECT_KNOWN_KEY)
                .remove(IS_LINKING_KEY)
                .commit(),
        ) {
            "Could not consume the native OAuth state"
        }
        pending
    }

    companion object {
        private const val STATE_KEY = "state"
        private const val PROVIDER_ID_KEY = "provider_id"
        private const val REJECT_KNOWN_KEY = "reject_known"
        private const val IS_LINKING_KEY = "is_linking"
        private val lock = Any()
    }
}
