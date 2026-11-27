package com.karasu256.karasulab.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsLogger {
    fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    fun recordError(throwable: Throwable, message: String? = null) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        if (message != null) {
            crashlytics.log(message)
        }
        crashlytics.recordException(throwable)
    }
}
