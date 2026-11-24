package com.karasu256.karasulab.data

enum class HostStatus {
    Connecting,
    Connected,
    Failed,
    TimedOut,
}

data class HostConnection(
    val host: String,
    val status: HostStatus,
)

data class ConnectionInfo(
    val api: HostConnection,
    val auth: HostConnection,
    val branch: String?,
)
