package com.karasu256.karasulab.ui.debug

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.HostConnection
import com.karasu256.karasulab.data.HostStatus
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun DebugScreen(
    navigator: DestinationsNavigator,
    viewModel: DebugViewModel = hiltViewModel(),
) {
    val info by viewModel.connection.collectAsStateWithLifecycle()

    SettingsScaffold(title = stringResource(R.string.debug_title), navigator = navigator) {
        info?.let { connection ->
            SettingsGroup {
                item { HostItem(connection.api) }
                item { HostItem(connection.auth) }
                item {
                    SettingsItem(
                        title = stringResource(R.string.debug_branch),
                        description = connection.branch ?: stringResource(R.string.debug_unavailable),
                    )
                }
            }
        }
    }
}

@Composable
private fun HostItem(connection: HostConnection) {
    SettingsItem(
        title = stringResource(R.string.debug_api_host),
        description = connection.host,
        trailing = {
            when (connection.status) {
                HostStatus.Connecting -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                HostStatus.Connected -> Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.connection_connected),
                    tint = MaterialTheme.colorScheme.primary,
                )
                HostStatus.Failed -> Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.connection_failed),
                    tint = MaterialTheme.colorScheme.error,
                )
                HostStatus.TimedOut -> Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = stringResource(R.string.connection_timed_out),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
