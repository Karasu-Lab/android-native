package com.karasu256.karasulab.ui.linking

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.compose.LocalActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.ConfirmDialog
import com.karasu256.karasulab.ui.component.ErrorRetry
import com.karasu256.karasulab.ui.component.MinecraftAvatar
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.UserAvatar
import com.karasu256.karasulab.ui.providers.OAuthProvidersViewModel
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun LinkedAccountsScreen(
    navigator: DestinationsNavigator,
    viewModel: LinkedAccountsViewModel = hiltViewModel(),
    providersViewModel: OAuthProvidersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val providersState by providersViewModel.uiState.collectAsStateWithLifecycle()
    val providers = providersState.oauthProviders + listOfNotNull(providersState.minecraftProvider)
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.resolve()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
        providersViewModel.load("link")
    }
    LaunchedEffect(providers) {
        viewModel.updateProviders(providers)
    }
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
        if (state.loadFailed || providersState.error != null) {
            ErrorRetry(
                message = stringResource(R.string.load_error),
                onRetry = {
                    viewModel.load()
                    providersViewModel.load("link")
                },
            )
        }
        SettingsGroup {
            providers.forEach { provider ->
                item {
                    val profile = state.profiles[provider.id]
                    val busy = state.busyProviderId == provider.id
                    val iconUrl = profile?.iconUrl
                    SettingsItem(
                        title = viewModel.profileTitle(provider, profile),
                        description = if (profile != null) {
                            profile.description.orEmpty()
                        } else {
                            stringResource(R.string.connections_not_linked)
                        },
                        leading = iconUrl?.let { url ->
                            {
                                if (provider.id == "minecraft") {
                                    MinecraftAvatar(textureUrl = url)
                                } else {
                                    UserAvatar(id = provider.id, label = provider.label, imageUrl = url)
                                }
                            }
                        },
                        trailing = {
                            IconButton(
                                onClick = {
                                    if (profile != null) {
                                        viewModel.askUnlink(provider)
                                    } else {
                                        viewModel.link(provider)
                                    }
                                },
                                enabled = !busy && if (profile != null) state.canUnlink else state.linkable[provider.id] != false,
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
            val context = LocalContext.current
            MinecraftVerifyDialog(
                verifying = state.busyProviderId == "minecraft",
                error = state.minecraftError?.resolve(),
                onVerify = viewModel::verifyMinecraft,
                onDismiss = viewModel::dismissMinecraft,
                onCopyAddress = {
                    context.getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText(BuildConfig.MC_AUTH_ADDRESS, BuildConfig.MC_AUTH_ADDRESS))
                    viewModel.addressCopied()
                },
            )
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
