package com.karasu256.karasulab.data

data class FirebaseProjectInfo(
    val projectId: String?,
    val projectNumber: String?,
    val appId: String?,
)

interface FirebaseRepository {
    fun initialize()

    fun setCrashlyticsCollectionEnabled(enabled: Boolean)

    fun log(message: String)

    fun recordException(throwable: Throwable)

    fun projectInfo(): FirebaseProjectInfo?
}
