package com.karasu256.karasulab.ui.security

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.karasu256.karasulab.auth.CredentialAuthenticator
import com.karasu256.karasulab.ui.component.ConfirmDialog
import com.karasu256.karasulab.ui.component.EmptyText
import com.karasu256.karasulab.ui.component.ErrorRetry
import com.karasu256.karasulab.ui.component.FormDialog
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.PasswordField
import com.karasu256.karasulab.ui.component.PasswordStrengthMeter
import com.karasu256.karasulab.ui.component.SectionHeader
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.component.TextEntryField
import com.karasu256.karasulab.ui.component.Tone
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<RootGraph>
@Composable
fun SecurityScreen(
    navigator: DestinationsNavigator,
    viewModel: SecurityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val credentials = remember(activity) { activity?.let(::CredentialAuthenticator) }
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.resolve()
    val settings = state.settings

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.security_title),
        navigator = navigator,
        loading = state.loading,
        snackbarHostState = snackbarHostState,
    ) {
        if (state.loadFailed) {
            ErrorRetry(message = stringResource(R.string.load_error), onRetry = viewModel::load)
        }
        if (settings != null) {
            SettingsGroup {
                item {
                    SettingsItem(
                        title = stringResource(R.string.security_password),
                        description = if (settings.hasPassword) "••••••••" else stringResource(R.string.security_password_not_set),
                        leading = { IconTile(Icons.Filled.Password, Tone.Primary) },
                        onClick = viewModel::openPasswordDialog,
                        chevron = true,
                    )
                }
            }
            SectionHeader(
                text = stringResource(R.string.passkey_title),
                action = {
                    IconButton(onClick = { credentials?.let(viewModel::addPasskey) }, enabled = !state.registering && credentials != null) {
                        if (state.registering) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.passkey_register))
                        }
                    }
                },
            )
            if (settings.passkeys.isEmpty()) {
                EmptyText(stringResource(R.string.passkey_none))
            } else {
                SettingsGroup {
                    settings.passkeys.forEach { passkey ->
                        item {
                            SettingsItem(
                                title = passkey.name ?: stringResource(R.string.passkey_unnamed),
                                description = passkey.createdOn,
                                leading = { IconTile(Icons.Filled.Fingerprint, Tone.Secondary) },
                                trailing = {
                                    IconButton(
                                        onClick = { viewModel.askDeletePasskey(passkey) },
                                        enabled = state.deletingPasskeyId != passkey.id,
                                    ) {
                                        if (state.deletingPasskeyId == passkey.id) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.passkey_remove))
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
            SectionHeader(text = stringResource(R.string.danger_title))
            SettingsGroup {
                item {
                    SettingsItem(
                        title = stringResource(R.string.danger_delete_account),
                        leading = { IconTile(Icons.Filled.DeleteForever, Tone.Error) },
                        onClick = viewModel::openDeleteDialog,
                        destructive = true,
                    )
                }
            }
        }

        state.passwordDialog?.let { dialog ->
            val hasPassword = settings?.hasPassword == true
            val title = stringResource(if (hasPassword) R.string.security_change_password else R.string.security_set_password)
            FormDialog(
                title = title,
                confirmLabel = title,
                onConfirm = viewModel::submitPassword,
                onDismiss = viewModel::closePasswordDialog,
                busy = dialog.saving,
                confirmEnabled = dialog.newPassword.isNotEmpty() && (!hasPassword || dialog.currentPassword.isNotEmpty()),
                error = dialog.error?.resolve(),
            ) {
                if (hasPassword) {
                    PasswordField(
                        value = dialog.currentPassword,
                        onValueChange = viewModel::onCurrentPasswordChange,
                        label = stringResource(R.string.security_current_password),
                        enabled = !dialog.saving,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                PasswordField(
                    value = dialog.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = stringResource(R.string.security_new_password),
                    enabled = !dialog.saving,
                    newPassword = true,
                    onImeAction = viewModel::submitPassword,
                )
                PasswordStrengthMeter(password = dialog.newPassword, modifier = Modifier.padding(top = 8.dp))
            }
        }

        state.passkeyNameDialog?.let { dialog ->
            FormDialog(
                title = stringResource(R.string.passkey_register),
                confirmLabel = stringResource(R.string.passkey_register),
                onConfirm = viewModel::savePasskey,
                onDismiss = viewModel::dismissPasskeyName,
                busy = dialog.saving,
                confirmEnabled = dialog.name.isNotBlank(),
            ) {
                TextEntryField(
                    value = dialog.name,
                    onValueChange = viewModel::onPasskeyNameChange,
                    label = stringResource(R.string.passkey_name),
                    enabled = !dialog.saving,
                    placeholder = stringResource(R.string.passkey_name_placeholder),
                    onImeAction = viewModel::savePasskey,
                )
            }
        }

        state.passkeyToDelete?.let { passkey ->
            ConfirmDialog(
                title = stringResource(R.string.passkey_remove_title),
                description = stringResource(R.string.passkey_remove_description, passkey.name ?: stringResource(R.string.passkey_unnamed)),
                confirmLabel = stringResource(R.string.passkey_remove),
                onConfirm = viewModel::deletePasskey,
                onDismiss = viewModel::cancelDeletePasskey,
            )
        }

        state.deleteDialog?.let { dialog ->
            val hasPassword = settings?.hasPassword == true
            FormDialog(
                title = stringResource(R.string.danger_delete_account),
                confirmLabel = stringResource(R.string.action_delete),
                onConfirm = viewModel::deleteAccount,
                onDismiss = viewModel::closeDeleteDialog,
                busy = dialog.deleting,
                confirmEnabled = !hasPassword || dialog.password.isNotEmpty(),
                error = dialog.error?.resolve(),
            ) {
                if (hasPassword) {
                    PasswordField(
                        value = dialog.password,
                        onValueChange = viewModel::onDeletePasswordChange,
                        label = stringResource(R.string.danger_confirm_password),
                        enabled = !dialog.deleting,
                        onImeAction = viewModel::deleteAccount,
                    )
                } else {
                    Text(stringResource(R.string.danger_reauth_required))
                }
            }
        }
    }
}
