package com.karasu256.karasulab.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmsFirebaseRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : FirebaseRepository {
    override fun initialize() {
        FirebaseApp.initializeApp(context)
        FirebaseAuth.getInstance()
        FirebaseCrashlytics.getInstance()
        FirebaseAnalytics.getInstance(context)
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    override fun log(message: String) {
        Firebase.crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }

    override fun projectInfo(): FirebaseProjectInfo? =
        runCatching { FirebaseApp.getInstance().options }.getOrNull()?.let { options ->
            FirebaseProjectInfo(
                projectId = options.projectId,
                projectNumber = options.gcmSenderId,
                appId = options.applicationId,
            )
        }
}
