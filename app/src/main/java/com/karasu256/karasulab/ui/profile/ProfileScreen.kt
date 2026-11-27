package com.karasu256.karasulab.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.MinecraftAvatar
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.TextInputDialog
import com.karasu256.karasulab.ui.component.UserAvatar
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ProfileScreen(
    navigator: DestinationsNavigator,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.resolve()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::uploadIcon)
    }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.profile_title),
        navigator = navigator,
        loading = state.iconBusy,
        snackbarHostState = snackbarHostState,
    ) {
        account?.let { current ->
            SettingsGroup {
                item {
                    SettingsItem(
                        title = stringResource(R.string.profile_icon),
                        description = stringResource(R.string.profile_change_icon),
                        trailing = { UserAvatar(id = current.id, label = current.displayName, imageUrl = current.image, size = 48.dp) },
                        onClick = viewModel::openIconSheet,
                        enabled = !state.iconBusy,
                    )
                }
                item {
                    SettingsItem(
                        title = stringResource(R.string.profile_display_name),
                        description = current.name.orEmpty(),
                        onClick = viewModel::openNameDialog,
                        chevron = true,
                    )
                }
            }
            if (state.nameDialogOpen) {
                TextInputDialog(
                    title = stringResource(R.string.profile_edit_display_name),
                    label = stringResource(R.string.profile_display_name),
                    initialValue = current.name.orEmpty(),
                    confirmLabel = stringResource(R.string.action_save),
                    onConfirm = viewModel::saveName,
                    onDismiss = viewModel::closeNameDialog,
                    busy = state.savingName,
                    error = state.nameError?.resolve(),
                )
            }
            if (state.iconSheetOpen) {
                ModalBottomSheet(onDismissRequest = viewModel::closeIconSheet) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.profile_upload_icon)) },
                            modifier = Modifier.clickable {
                                viewModel.closeIconSheet()
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                        state.providerIcons.forEach { option ->
                            ListItem(
                                headlineContent = { Text(option.accountName ?: option.provider.label) },
                                supportingContent = { Text(stringResource(R.string.profile_use_provider_icon)) },
                                modifier = Modifier.clickable { viewModel.useProviderIcon(option) },
                                leadingContent = {
                                    if (option.provider.id == "minecraft") {
                                        MinecraftAvatar(textureUrl = option.iconUrl)
                                    } else {
                                        UserAvatar(id = option.provider.id, label = option.provider.label, imageUrl = option.iconUrl)
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                        if (current.image != null) {
                            ListItem(
                                headlineContent = { Text(text = stringResource(R.string.profile_delete_icon), color = MaterialTheme.colorScheme.error) },
                                modifier = Modifier.clickable(onClick = viewModel::deleteIcon),
                                leadingContent = {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                }
            }
        }
    }
}
