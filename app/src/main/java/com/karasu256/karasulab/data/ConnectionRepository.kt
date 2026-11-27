package com.karasu256.karasulab.data

import com.karasu256.karasulab.logging.Log
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.data.remote.AuthApi
import com.karasu256.karasulab.data.remote.UserApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

@Singleton
class ConnectionRepository @Inject constructor(
    private val userApi: UserApi,
    private val authApi: AuthApi,
) {
    fun observe(): Flow<ConnectionInfo> = channelFlow {
        val lock = Mutex()
        var info = ConnectionInfo(
            api = HostConnection(BuildConfig.API_HOST, HostStatus.Connecting),
            auth = HostConnection(BuildConfig.AUTH_HOST, HostStatus.Connecting),
            branch = null,
        )
        send(info)

        suspend fun update(change: (ConnectionInfo) -> ConnectionInfo) = lock.withLock {
            info = change(info)
            send(info)
        }

        launch {
            val (status, meta) = probe(BuildConfig.API_HOST) { userApi.meta() }
            update {
                it.copy(
                    api = HostConnection(
                        meta?.apiUrl?.toHttpUrl()?.host ?: BuildConfig.API_HOST, status
                    ), branch = meta?.gitBranch
                )
            }
        }
        launch {
            val (status) = probe(BuildConfig.AUTH_HOST) { authApi.ok() }
            update { it.copy(auth = it.auth.copy(status = status)) }
        }
    }

    private suspend fun <T> probe(host: String, block: suspend () -> T): Pair<HostStatus, T?> =
        try {
            HostStatus.Connected to withTimeout(5.seconds) { block() }
        } catch (e: TimeoutCancellationException) {
            Log.w("ConnectionProbe", "$host did not answer within 5 seconds: $e", e)
            HostStatus.TimedOut to null
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            Log.w("ConnectionProbe", "$host timed out: $e", e)
            HostStatus.TimedOut to null
        } catch (e: Exception) {
            Log.w("ConnectionProbe", "$host could not be reached: $e", e)
            HostStatus.Failed to null
        }
}
