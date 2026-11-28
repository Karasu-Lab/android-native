package com.karasu256.karasulab.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.SectionHeader
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.Tone
import com.karasu256.karasulab.ui.component.UserAvatar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ActiveSessionsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.AuthorizedAppsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.DebugScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LicensesScreenDestination
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
    val developerOptionsEnabled by viewModel.developerOptionsEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.toast.collect { text ->
            Toast.makeText(context, context.getString(text.id, *text.args.toTypedArray()), Toast.LENGTH_SHORT).show()
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.settings_title),
        navigator = navigator,
        showBack = false,
        loading = loading,
        menuViewModel = menuViewModel,
    ) {
        account?.let { current ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navigator.navigate(ProfileScreenDestination) }
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UserAvatar(id = current.id, label = current.displayName, imageUrl = current.image, size = 88.dp)
                Text(
                    text = current.displayName,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = current.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            HorizontalDivider()
        }
        SectionHeader(stringResource(R.string.settings_section_general))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_connections),
                    leading = { IconTile(Icons.Filled.Link) },
                    onClick = { navigator.navigate(LinkedAccountsScreenDestination) },
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_authorized_apps),
                    leading = { IconTile(Icons.Filled.Apps) },
                    onClick = { navigator.navigate(AuthorizedAppsScreenDestination) },
                )
            }
        }
        SectionHeader(stringResource(R.string.settings_security))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_security),
                    leading = { IconTile(Icons.Filled.Security) },
                    onClick = { navigator.navigate(SecurityScreenDestination) },
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_active_sessions),
                    leading = { IconTile(Icons.Filled.Devices) },
                    onClick = { navigator.navigate(ActiveSessionsScreenDestination) },
                )
            }
        }
        SectionHeader(stringResource(R.string.settings_section_other))
        SettingsGroup {
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_client_version),
                    description = BuildConfig.VERSION_NAME,
                    leading = { IconTile(Icons.Filled.Info) },
                    onClick = viewModel::onVersionTapped,
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_licenses),
                    leading = { IconTile(Icons.Filled.Description) },
                    onClick = { navigator.navigate(LicensesScreenDestination) },
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.settings_release_channel),
                    description = stringResource(
                        when (BuildConfig.RELEASE_CHANNEL) {
                            "open-beta" -> R.string.settings_release_channel_open_beta
                            "closed-beta" -> R.string.settings_release_channel_closed_beta
                            "internal-test" -> R.string.settings_release_channel_internal_test
                            else -> R.string.settings_release_channel_production
                        }
                    ),
                    leading = { IconTile(Icons.Filled.Update) },
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        if (developerOptionsEnabled) {
            SettingsItem(
                title = stringResource(R.string.settings_developer_options),
                description = stringResource(R.string.settings_developer_options_description),
                leading = { IconTile(Icons.Filled.Code) },
                onClick = { navigator.navigate(DebugScreenDestination) },
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        SettingsItem(
            title = stringResource(R.string.settings_sign_out),
            leading = { IconTile(Icons.AutoMirrored.Filled.Logout, Tone.Error) },
            onClick = menuViewModel::signOut,
            destructive = true,
        )
    }
}
