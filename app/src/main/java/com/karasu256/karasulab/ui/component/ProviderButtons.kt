package com.karasu256.karasulab.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        providers.forEach { provider ->
            val method = when (provider.id) {
                "google" -> ProviderMethod.Google
                "passkey" -> ProviderMethod.Passkey
                else -> null
            }
            ProviderButton(
                provider = provider,
                onClick = {
                    when (provider.id) {
                        "google" -> onGoogle()
                        "passkey" -> onPasskey()
                        else -> onOAuth(provider.id)
                    }
                },
                enabled = enabled,
                busy = busyMethod == method && method != null || busyProviderId == provider.id,
            )
        }
    }
}

@Composable
private fun ProviderButton(
    provider: OAuthProvider,
    onClick: () -> Unit,
    enabled: Boolean,
    busy: Boolean,
) {
    val dark = isSystemInDarkTheme()
    val background = provider.backgroundColor.composeColor(dark)
    val foreground = provider.foregroundColor.composeColor(dark)
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = foreground,
            disabledContainerColor = background,
            disabledContentColor = foreground,
        ),
    ) {
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = foreground)
        } else {
            ProviderGlyph(provider = provider, size = 20.dp)
        }
        Text(text = provider.label, modifier = Modifier.padding(start = 12.dp))
    }
}
