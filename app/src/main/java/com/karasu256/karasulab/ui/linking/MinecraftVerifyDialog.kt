package com.karasu256.karasulab.ui.linking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.BuildConfig
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.CodeField
import com.karasu256.karasulab.ui.component.FormDialog

private const val CODE_LENGTH = 6

@Composable
fun MinecraftVerifyDialog(
    verifying: Boolean,
    error: String?,
    onVerify: (String) -> Unit,
    onDismiss: () -> Unit,
    onCopyAddress: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    FormDialog(
        title = stringResource(R.string.minecraft_verify_title),
        confirmLabel = stringResource(R.string.minecraft_verify_action),
        onConfirm = { onVerify(code) },
        onDismiss = onDismiss,
        busy = verifying,
        confirmEnabled = code.length == CODE_LENGTH,
        error = error,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.minecraft_verify_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.minecraft_verify_server_address),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = BuildConfig.MC_AUTH_ADDRESS,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                        )
                        IconButton(onClick = onCopyAddress) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.minecraft_verify_copy_address))
                        }
                    }
                }
            }
            CodeField(
                value = code,
                onValueChange = { code = it },
                label = stringResource(R.string.minecraft_verify_code_label),
                enabled = !verifying,
                onImeAction = { if (code.length == CODE_LENGTH) onVerify(code) },
            )
        }
    }
}
