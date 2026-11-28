package com.karasu256.karasulab.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoGmsFirebaseRepository @Inject constructor() : FirebaseRepository {
    override fun initialize() = Unit

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) = Unit

    override fun log(message: String) = Unit

    override fun recordException(throwable: Throwable) = Unit

    override fun projectInfo(): FirebaseProjectInfo? = null
}
