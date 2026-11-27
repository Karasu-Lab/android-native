package com.karasu256.karasulab.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.karasu256.karasulab.data.OAuthProvider
import com.karasu256.karasulab.data.ProviderColors

fun ProviderColors.composeColor(dark: Boolean): Color {
    val hex = if (dark) this.dark else this.default
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (error: IllegalArgumentException) {
        Color.Unspecified
    }
}

@Composable
fun ProviderGlyph(provider: OAuthProvider, size: Dp, modifier: Modifier = Modifier) {
    val foreground = provider.foregroundColor.composeColor(isSystemInDarkTheme())
    AsyncImage(
        model = provider.iconUrl,
        contentDescription = null,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(foreground),
    )
}

@Composable
fun ProviderIcon(provider: OAuthProvider, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val background = provider.backgroundColor.composeColor(isSystemInDarkTheme())
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        ProviderGlyph(provider = provider, size = size * 0.6f)
    }
}
