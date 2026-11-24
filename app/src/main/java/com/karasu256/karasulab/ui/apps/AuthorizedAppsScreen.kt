package com.karasu256.karasulab.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.AuthorizedAppDetail
import com.karasu256.karasulab.ui.component.ConfirmDialog
import com.karasu256.karasulab.ui.component.EmptyText
import com.karasu256.karasulab.ui.component.ErrorRetry
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.LoadingView
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.Tone
import com.karasu256.karasulab.ui.component.UserAvatar
import com.karasu256.karasulab.ui.formatDateTime
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AuthorizedAppsScreen(
    navigator: DestinationsNavigator,
    viewModel: AuthorizedAppsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.resolve()

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.authorized_apps_title),
        navigator = navigator,
        loading = state.loading,
        snackbarHostState = snackbarHostState,
    ) {
        when {
            state.loadFailed -> ErrorRetry(message = stringResource(R.string.load_error), onRetry = viewModel::load)
            !state.loading && state.apps.isEmpty() -> EmptyText(stringResource(R.string.authorized_apps_empty))
            else -> SettingsGroup {
                state.apps.forEach { app ->
                    item {
                        SettingsItem(
                            title = app.clientName ?: app.clientId,
                            description = formatDateTime(app.createdAt),
                            leading = { IconTile(Icons.Filled.Apps, Tone.Tertiary) },
                            trailing = {
                                IconButton(onClick = { viewModel.askRevoke(app) }, enabled = state.revokingId != app.id) {
                                    if (state.revokingId == app.id) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Filled.LinkOff, contentDescription = stringResource(R.string.authorized_apps_revoke))
                                    }
                                }
                            },
                            onClick = { viewModel.openInfo(app) },
                        )
                    }
                }
            }
        }

        state.infoApp?.let { app ->
            ModalBottomSheet(onDismissRequest = viewModel::closeInfo) {
                val detail = state.infoDetail
                if (detail == null) {
                    LoadingView(modifier = Modifier.padding(32.dp))
                } else {
                    AppInfo(detail = detail, name = app.clientName ?: app.clientId)
                }
            }
        }

        state.pendingRevoke?.let { app ->
            ConfirmDialog(
                title = stringResource(R.string.authorized_apps_revoke_title),
                description = stringResource(R.string.authorized_apps_revoke_description, app.clientName ?: app.clientId),
                confirmLabel = stringResource(R.string.authorized_apps_revoke),
                onConfirm = viewModel::revoke,
                onDismiss = viewModel::cancelRevoke,
            )
        }
    }
}

@Composable
private fun AppInfo(detail: AuthorizedAppDetail, name: String) {
    Column(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            UserAvatar(id = detail.clientId, label = name, imageUrl = detail.logoUri, size = 48.dp)
            Text(text = name, style = MaterialTheme.typography.titleLarge)
        }
        InfoLine(label = stringResource(R.string.authorized_apps_client_id), value = detail.clientId)
        InfoLine(label = stringResource(R.string.authorized_apps_authorized_at), value = formatDateTime(detail.createdAt))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.authorized_apps_permissions),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (detail.scopes.isEmpty()) {
                Text(text = stringResource(R.string.authorized_apps_no_permissions), style = MaterialTheme.typography.bodyMedium)
            }
            detail.scopes.forEach { scope ->
                Text(text = scopeLabel(scope)?.let { stringResource(it) } ?: scope, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
