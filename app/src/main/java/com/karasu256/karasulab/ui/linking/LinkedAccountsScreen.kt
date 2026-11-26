package com.karasu256.karasulab.ui.linking

import androidx.activity.compose.LocalActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.auth.CredentialAuthenticator
import com.karasu256.karasulab.ui.component.CodeField
import com.karasu256.karasulab.ui.component.ConfirmDialog
import com.karasu256.karasulab.ui.component.ErrorRetry
import com.karasu256.karasulab.ui.component.FormDialog
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.MinecraftAvatar
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.Tone
import com.karasu256.karasulab.ui.component.UserAvatar
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

private fun providerIcon(id: String): ImageVector = when (id) {
    "xbox" -> Icons.Filled.SportsEsports
    "minecraft" -> Icons.Filled.ViewInAr
    else -> Icons.Filled.Public
}

@Destination<RootGraph>
@Composable
fun LinkedAccountsScreen(
    navigator: DestinationsNavigator,
    viewModel: LinkedAccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val credentials = remember(activity) { CredentialAuthenticator(checkNotNull(activity)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.resolve()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    LaunchedEffect(viewModel) {
        viewModel.openBrowser.collect { CustomTabsIntent.Builder().build().launchUrl(checkNotNull(activity), it.toUri()) }
    }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.connections_title),
        navigator = navigator,
        loading = state.loading,
        snackbarHostState = snackbarHostState,
    ) {
        if (state.loadFailed) {
            ErrorRetry(message = stringResource(R.string.load_error), onRetry = viewModel::load)
        }
        SettingsGroup {
            linkProviders.forEach { provider ->
                item {
                    val profile = state.profiles[provider.id]
                    val busy = state.busyProviderId == provider.id
                    SettingsItem(
                        title = profile?.let { it.name ?: it.email ?: provider.label } ?: provider.label,
                        description = if (profile != null) {
                            profile.email ?: profile.accountId.orEmpty()
                        } else {
                            stringResource(R.string.connections_not_linked)
                        },
                        leading = {
                            if (provider.id == "minecraft" && profile?.avatarUrl != null) {
                                MinecraftAvatar(textureUrl = profile.avatarUrl)
                            } else if (profile?.avatarUrl != null) {
                                UserAvatar(id = provider.id, label = provider.label, imageUrl = profile.avatarUrl)
                            } else if (provider.id == "google") {
                                IconTile(Icons.Filled.Public, Tone.Neutral)
                            } else {
                                IconTile(providerIcon(provider.id), Tone.Neutral)
                            }
                        },
                        trailing = {
                            IconButton(
                                onClick = {
                                    if (profile != null) {
                                        viewModel.askUnlink(provider)
                                    } else {
                                        viewModel.link(provider, credentials)
                                    }
                                },
                                enabled = state.profiles.isNotEmpty() && !busy && state.busyProviderId == null && (profile == null || state.canUnlink),
                            ) {
                                if (busy) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else if (profile != null) {
                                    Icon(Icons.Filled.LinkOff, contentDescription = stringResource(R.string.connections_unlink))
                                } else {
                                    Icon(Icons.Filled.Link, contentDescription = stringResource(R.string.connections_link))
                                }
                            }
                        },
                    )
                }
            }
        }
        if (!state.loading && !state.canUnlink && state.linkedCount > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.connections_cannot_unlink),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.minecraftDialog) {
            var code by rememberSaveable { mutableStateOf("") }
            val verifying = state.busyProviderId == "minecraft"
            FormDialog(
                title = stringResource(R.string.minecraft_verify_title),
                confirmLabel = stringResource(R.string.minecraft_verify_action),
                onConfirm = { viewModel.verifyMinecraft(code) },
                onDismiss = viewModel::dismissMinecraft,
                busy = verifying,
                confirmEnabled = code.length == 6,
                error = state.minecraftError?.resolve(),
            ) {
                CodeField(
                    value = code,
                    onValueChange = { code = it },
                    label = stringResource(R.string.minecraft_verify_code_label),
                    enabled = !verifying,
                    onImeAction = { if (code.length == 6) viewModel.verifyMinecraft(code) },
                )
            }
        }
        state.unlinkTarget?.let { provider ->
            ConfirmDialog(
                title = stringResource(R.string.connections_unlink_title),
                description = stringResource(R.string.connections_unlink_description, provider.label),
                confirmLabel = stringResource(R.string.connections_unlink),
                onConfirm = viewModel::unlink,
                onDismiss = viewModel::cancelUnlink,
            )
        }
    }
}
