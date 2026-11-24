package com.karasu256.karasulab.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.Account
import com.karasu256.karasulab.ui.component.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(
    accounts: List<Account>,
    removingId: String?,
    onSwitch: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddAccount: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            accounts.forEach { account ->
                ListItem(
                    headlineContent = { Text(text = account.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.clickable(enabled = !account.isActive) { onSwitch(account.id) },
                    supportingContent = { Text(text = account.email, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingContent = { UserAvatar(id = account.id, label = account.displayName, imageUrl = account.image) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (account.isActive) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = stringResource(R.string.account_current),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(onClick = { onRemove(account.id) }, enabled = removingId == null) {
                                if (removingId == account.id) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.account_remove))
                                }
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ListItem(
                headlineContent = { Text(stringResource(R.string.account_add)) },
                modifier = Modifier.clickable(onClick = onAddAccount),
                leadingContent = { Icon(Icons.Filled.Add, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_sign_out)) },
                modifier = Modifier.clickable(onClick = onSignOut),
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
fun AddAccountDialog(onContinue: (switchToNew: Boolean) -> Unit, onDismiss: () -> Unit) {
    var switchToNew by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_add)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { switchToNew = !switchToNew },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(checked = switchToNew, onCheckedChange = null)
                Text(stringResource(R.string.account_switch_after_add))
            }
        },
        confirmButton = {
            TextButton(onClick = { onContinue(switchToNew) }) { Text(stringResource(R.string.account_continue_to_sign_in)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
