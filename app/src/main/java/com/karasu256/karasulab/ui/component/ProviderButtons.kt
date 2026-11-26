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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.OAuthProvider

enum class ProviderMethod { Google, Passkey }

@Composable
fun ProviderButtons(
    providers: List<OAuthProvider>,
    onGoogle: () -> Unit,
    onPasskey: () -> Unit,
    onOAuth: (String) -> Unit,
    enabled: Boolean,
    busyMethod: ProviderMethod?,
    busyProviderId: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        providers.filter(OAuthProvider::signIn).forEach { provider ->
            val method = when (provider.id) {
                "google" -> ProviderMethod.Google
                "passkey" -> ProviderMethod.Passkey
                else -> null
            }
            ProviderButton(
                label = provider.label,
                onClick = {
                    when (provider.id) {
                        "google" -> onGoogle()
                        "passkey" -> onPasskey()
                        else -> onOAuth(provider.id)
                    }
                },
                enabled = enabled,
                busy = busyMethod == method && method != null || busyProviderId == provider.id,
            ) {
                when (provider.id) {
                    "google" -> Icon(
                        painter = painterResource(R.drawable.ic_google_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp),
                    )
                    "passkey" -> Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    else -> Text(text = provider.label.firstOrNull()?.uppercase() ?: "?", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                }
            }
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
