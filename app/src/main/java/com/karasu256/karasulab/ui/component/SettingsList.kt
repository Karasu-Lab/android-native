package com.karasu256.karasulab.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Stable
class SettingsGroupScope {
    internal val entries = mutableListOf<@Composable () -> Unit>()

    fun item(content: @Composable () -> Unit) {
        entries += content
    }
}

@Composable
fun SettingsGroup(modifier: Modifier = Modifier, content: SettingsGroupScope.() -> Unit) {
    val entries = SettingsGroupScope().apply(content).entries
    Column(modifier = modifier.fillMaxWidth()) {
        entries.forEach { entry -> entry() }
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        action?.invoke()
    }
}

enum class Tone { Neutral, Primary, Error }

@Composable
fun IconTile(icon: ImageVector, tone: Tone = Tone.Neutral) {
    val colors = MaterialTheme.colorScheme
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = when (tone) {
            Tone.Neutral -> colors.onSurfaceVariant
            Tone.Primary -> colors.primary
            Tone.Error -> colors.error
        },
    )
}

@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    chevron: Boolean = false,
) {
    val clickable = if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier
    val titleColor = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified
    ListItem(
        headlineContent = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = titleColor) },
        modifier = modifier.then(clickable),
        supportingContent = description?.let { { Text(text = it, maxLines = 2, overflow = TextOverflow.Ellipsis) } },
        leadingContent = leading,
        trailingContent = trailing ?: if (chevron) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
