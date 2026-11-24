package com.karasu256.karasulab.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R

enum class ProviderMethod { Google, Passkey }

@Composable
fun ProviderButtons(
    onGoogle: () -> Unit,
    onPasskey: () -> Unit,
    enabled: Boolean,
    busyMethod: ProviderMethod?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProviderIconButton(
            label = stringResource(R.string.sign_in_with_google),
            onClick = onGoogle,
            enabled = enabled,
            busy = busyMethod == ProviderMethod.Google,
        ) {
            Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(22.dp))
        }
        ProviderIconButton(
            label = stringResource(R.string.sign_in_with_passkey),
            onClick = onPasskey,
            enabled = enabled,
            busy = busyMethod == ProviderMethod.Passkey,
        ) {
            Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun ProviderIconButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    busy: Boolean,
    icon: @Composable () -> Unit,
) {
    OutlinedIconButton(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = label },
    ) {
        if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else icon()
    }
}
