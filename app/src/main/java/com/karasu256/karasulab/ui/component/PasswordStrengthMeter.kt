package com.karasu256.karasulab.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R

@Composable
fun PasswordStrengthMeter(password: String, modifier: Modifier = Modifier) {
    val score = listOf(
        password.length >= 8,
        password.any { it in 'A'..'Z' },
        password.any { it in 'a'..'z' },
        password.any { it in '0'..'9' },
        password.any { !it.isLetterOrDigit() || it.code > 127 },
    ).count { it }
    val level = when {
        score <= 1 -> 1
        score <= 2 -> 2
        score <= 4 -> 3
        else -> 4
    }
    val colors = MaterialTheme.colorScheme
    val color by animateColorAsState(
        targetValue = when (level) {
            1 -> colors.error
            2 -> colors.tertiary
            3 -> colors.secondary
            else -> colors.primary
        },
        label = "strengthColor",
    )
    val progress by animateFloatAsState(targetValue = if (password.isEmpty()) 0f else level / 4f, label = "strengthProgress")
    val label = when (level) {
        1 -> R.string.strength_weak
        2 -> R.string.strength_fair
        3 -> R.string.strength_good
        else -> R.string.strength_strong
    }
    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = colors.surfaceContainerHighest,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
        Text(
            text = if (password.isEmpty()) " " else stringResource(label),
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
    }
}
