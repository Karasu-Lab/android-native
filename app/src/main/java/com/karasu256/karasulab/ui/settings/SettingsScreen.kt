package com.karasu256.karasulab.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
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
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.Tone
import com.karasu256.karasulab.ui.component.UserAvatar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ActiveSessionsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.AuthorizedAppsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.DebugScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LinkedAccountsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SecurityScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun SettingsScreen(
    navigator: DestinationsNavigator,
    viewModel: SettingsViewModel = hiltViewModel(),
    menuViewModel: AccountMenuViewModel = hiltViewModel(),
) {
    val account by viewModel.activeAccount.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    SettingsScaffold(
        title = stringResource(R.string.settings_title),
        navigator = navigator,
        showBack = false,
        loading = loading,
        menuViewModel = menuViewModel,
    ) {
        account?.let { current ->
            SettingsGroup {
                item {
                    SettingsItem(
                        title = current.displayName,
                        description = current.email,
                        leading = { UserAvatar(id = current.id, label = current.displayName, imageUrl = current.image, size = 48.dp) },
                        trailing = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = { navigator.navigate(ProfileScreenDestination) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_security),
                    leading = { IconTile(Icons.Filled.Security, Tone.Primary) },
                    onClick = { navigator.navigate(SecurityScreenDestination) },
                    chevron = true,
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_connections),
                    leading = { IconTile(Icons.Filled.Link, Tone.Secondary) },
                    onClick = { navigator.navigate(LinkedAccountsScreenDestination) },
                    chevron = true,
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_authorized_apps),
                    leading = { IconTile(Icons.Filled.Apps, Tone.Tertiary) },
                    onClick = { navigator.navigate(AuthorizedAppsScreenDestination) },
                    chevron = true,
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_active_sessions),
                    leading = { IconTile(Icons.Filled.Devices, Tone.Neutral) },
                    onClick = { navigator.navigate(ActiveSessionsScreenDestination) },
                    chevron = true,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_debug),
                    description = stringResource(R.string.settings_debug_description),
                    leading = { IconTile(Icons.Filled.BugReport, Tone.Neutral) },
                    onClick = { navigator.navigate(DebugScreenDestination) },
                    chevron = true,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_sign_out),
                    leading = { IconTile(Icons.AutoMirrored.Filled.Logout, Tone.Error) },
                    onClick = menuViewModel::signOut,
                )
            }
        }
    }
}
