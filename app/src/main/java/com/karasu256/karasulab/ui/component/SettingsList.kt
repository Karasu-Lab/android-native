package com.karasu256.karasulab.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    val outer = MaterialTheme.shapes.large
    val inner = CornerSize(4.dp)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        entries.forEachIndexed { index, entry ->
            val first = index == 0
            val last = index == entries.lastIndex
            val shape = outer.copy(
                bottomStart = if (last) outer.bottomStart else inner,
                bottomEnd = if (last) outer.bottomEnd else inner,
                topStart = if (first) outer.topStart else inner,
                topEnd = if (first) outer.topEnd else inner,
            )
            Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainer) {
                entry()
            }
        }
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

enum class Tone { Primary, Secondary, Tertiary, Error, Neutral }

@Composable
fun IconTile(icon: ImageVector, tone: Tone = Tone.Neutral) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        Tone.Primary -> colors.primaryContainer to colors.onPrimaryContainer
        Tone.Secondary -> colors.secondaryContainer to colors.onSecondaryContainer
        Tone.Tertiary -> colors.tertiaryContainer to colors.onTertiaryContainer
        Tone.Error -> colors.errorContainer to colors.onErrorContainer
        Tone.Neutral -> colors.surfaceContainerHighest to colors.onSurfaceVariant
    }
    Surface(shape = MaterialTheme.shapes.medium, color = container, contentColor = content, modifier = Modifier.size(40.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
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
