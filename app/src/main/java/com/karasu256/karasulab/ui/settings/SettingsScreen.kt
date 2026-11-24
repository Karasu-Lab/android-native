package com.karasu256.karasulab.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.IconTile
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
        SettingsItem(
            title = stringResource(R.string.settings_security),
            leading = { IconTile(Icons.Filled.Security) },
            onClick = { navigator.navigate(SecurityScreenDestination) },
        )
        SettingsItem(
            title = stringResource(R.string.settings_connections),
            leading = { IconTile(Icons.Filled.Link) },
            onClick = { navigator.navigate(LinkedAccountsScreenDestination) },
        )
        SettingsItem(
            title = stringResource(R.string.settings_authorized_apps),
            leading = { IconTile(Icons.Filled.Apps) },
            onClick = { navigator.navigate(AuthorizedAppsScreenDestination) },
        )
        SettingsItem(
            title = stringResource(R.string.settings_active_sessions),
            leading = { IconTile(Icons.Filled.Devices) },
            onClick = { navigator.navigate(ActiveSessionsScreenDestination) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SettingsItem(
            title = stringResource(R.string.settings_debug),
            description = stringResource(R.string.settings_debug_description),
            leading = { IconTile(Icons.Filled.BugReport) },
            onClick = { navigator.navigate(DebugScreenDestination) },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SettingsItem(
            title = stringResource(R.string.settings_sign_out),
            leading = { IconTile(Icons.AutoMirrored.Filled.Logout, Tone.Error) },
            onClick = menuViewModel::signOut,
            destructive = true,
        )
    }
}
