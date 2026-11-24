package com.karasu256.karasulab.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProviderButton(
            label = stringResource(R.string.sign_in_with_google),
            onClick = onGoogle,
            enabled = enabled,
            busy = busyMethod == ProviderMethod.Google,
        ) {
            Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
        }
        ProviderButton(
            label = stringResource(R.string.sign_in_with_passkey),
            onClick = onPasskey,
            enabled = enabled,
            busy = busyMethod == ProviderMethod.Passkey,
        ) {
            Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProviderButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    busy: Boolean,
    icon: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else icon()
        Text(text = label, modifier = Modifier.padding(start = 12.dp))
    }
}
