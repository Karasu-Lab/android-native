package com.karasu256.karasulab.ui.sessions

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.ActiveSession
import com.karasu256.karasulab.ui.component.ConfirmDialog
import com.karasu256.karasulab.ui.component.EmptyText
import com.karasu256.karasulab.ui.component.ErrorRetry
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.Tone
import com.karasu256.karasulab.ui.formatDateTime
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

private fun platformOf(userAgent: String?): String? = when {
    userAgent == null -> null
    "Android" in userAgent -> "Android"
    "iPhone" in userAgent || "iPad" in userAgent -> "iOS"
    "Windows" in userAgent -> "Windows"
    "Mac OS X" in userAgent -> "macOS"
    "Linux" in userAgent -> "Linux"
    else -> null
}

private fun browserOf(userAgent: String?): String? = when {
    userAgent == null -> null
    "Edg/" in userAgent -> "Edge"
    "Firefox/" in userAgent -> "Firefox"
    "Chrome/" in userAgent -> "Chrome"
    "Safari/" in userAgent -> "Safari"
    else -> null
}

@Destination<RootGraph>
@Composable
fun ActiveSessionsScreen(
    navigator: DestinationsNavigator,
    viewModel: ActiveSessionsViewModel = hiltViewModel(),
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
        title = stringResource(R.string.active_sessions_title),
        navigator = navigator,
        loading = state.loading,
        snackbarHostState = snackbarHostState,
    ) {
        when {
            state.loadFailed -> ErrorRetry(message = stringResource(R.string.load_error), onRetry = viewModel::load)
            !state.loading && state.sessions.isEmpty() -> EmptyText(stringResource(R.string.active_sessions_empty))
            else -> SettingsGroup {
                state.sessions.forEach { session ->
                    item {
                        SessionItem(
                            session = session,
                            revoking = session.id in state.revokingIds,
                            onRevoke = { viewModel.askRevoke(session) },
                        )
                    }
                }
            }
        }

        if (state.pendingRevoke != null) {
            ConfirmDialog(
                title = stringResource(R.string.active_sessions_revoke_title),
                description = stringResource(R.string.active_sessions_revoke_description),
                confirmLabel = stringResource(R.string.active_sessions_revoke),
                onConfirm = viewModel::revoke,
                onDismiss = viewModel::cancelRevoke,
            )
        }
    }
}

@Composable
private fun SessionItem(session: ActiveSession, revoking: Boolean, onRevoke: () -> Unit) {
    val platform = platformOf(session.userAgent)
    val title = listOfNotNull(platform, browserOf(session.userAgent)).joinToString(" • ")
        .ifEmpty { stringResource(R.string.active_sessions_unknown_device) }
    val details = listOfNotNull(
        "${stringResource(R.string.active_sessions_created_at)}: ${formatDateTime(session.createdAt)}",
        session.ipAddress?.takeIf { it.isNotBlank() },
        stringResource(R.string.active_sessions_current).takeIf { session.current },
    ).joinToString(" • ")
    SettingsItem(
        title = title,
        description = details,
        leading = {
            val icon = when (platform) {
                "Android" -> Icons.Filled.PhoneAndroid
                "iOS" -> Icons.Filled.PhoneIphone
                else -> Icons.Filled.Computer
            }
            IconTile(icon, if (session.current) Tone.Primary else Tone.Neutral)
        },
        trailing = if (session.current) {
            null
        } else {
            {
                IconButton(onClick = onRevoke, enabled = !revoking) {
                    if (revoking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.LinkOff, contentDescription = stringResource(R.string.active_sessions_revoke))
                    }
                }
            }
        },
    )
}
