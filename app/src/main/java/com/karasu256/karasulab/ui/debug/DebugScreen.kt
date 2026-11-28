package com.karasu256.karasulab.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.HostConnection
import com.karasu256.karasulab.data.HostStatus
import com.karasu256.karasulab.ui.component.SectionHeader
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@Destination<RootGraph>
@Composable
fun DebugScreen(
    navigator: DestinationsNavigator,
    viewModel: DebugViewModel = hiltViewModel(),
) {
    val info by viewModel.connection.collectAsStateWithLifecycle()
    val developerOptionsEnabled by viewModel.developerOptionsEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fingerprints = remember(context) { signingFingerprints(context) }
    val firebaseOptions = viewModel.firebaseProjectInfo
    val copiedMessage = stringResource(R.string.debug_fingerprint_copied)

    fun copyFingerprint(label: String, value: String) {
        context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(label, value))
        scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
    }

    SettingsScaffold(title = stringResource(R.string.developer_options_title), navigator = navigator, snackbarHostState = snackbarHostState) {
        SectionHeader(text = stringResource(R.string.settings_section_general))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.developer_options_use),
                    trailing = {
                        Switch(
                            checked = developerOptionsEnabled,
                            onCheckedChange = viewModel::setDeveloperOptionsEnabled,
                        )
                    },
                )
            }
        }
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
        SectionHeader(text = stringResource(R.string.debug_fingerprint_section))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.debug_sha1),
                    description = fingerprints?.sha1 ?: stringResource(R.string.debug_unavailable),
                    onClick = fingerprints?.let { value -> { copyFingerprint("SHA-1", value.sha1) } },
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.debug_sha256),
                    description = fingerprints?.sha256 ?: stringResource(R.string.debug_unavailable),
                    onClick = fingerprints?.let { value -> { copyFingerprint("SHA-256", value.sha256) } },
                )
            }
        }
        if (firebaseOptions != null) {
            SectionHeader(text = stringResource(R.string.debug_firebase_section))
            SettingsGroup {
                item {
                    SettingsItem(
                        title = stringResource(R.string.debug_firebase_project_id),
                        description = firebaseOptions.projectId ?: stringResource(R.string.debug_unavailable),
                    )
                }
                item {
                    SettingsItem(
                        title = stringResource(R.string.debug_firebase_project_number),
                        description = firebaseOptions.projectNumber ?: stringResource(R.string.debug_unavailable),
                    )
                }
                item {
                    SettingsItem(
                        title = stringResource(R.string.debug_firebase_app_id),
                        description = firebaseOptions.appId ?: stringResource(R.string.debug_unavailable),
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
