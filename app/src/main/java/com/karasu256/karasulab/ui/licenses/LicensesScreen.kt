package com.karasu256.karasulab.ui.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.GeneratedLicenseCatalog
import com.karasu256.karasulab.ui.settings.SettingsGroup
import com.karasu256.karasulab.ui.settings.SettingsItem
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination<RootGraph>
@Composable
fun LicensesScreen(navigator: DestinationsNavigator) {
    var expandedKey by remember { mutableStateOf<String?>(null) }
    SettingsScaffold(
        title = stringResource(R.string.licenses_title),
        onBack = { navigator.popBackStack() },
    ) {
        Text(
            text = stringResource(R.string.licenses_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SettingsGroup {
            GeneratedLicenseCatalog.libraries.forEach { entry ->
                val key = "${entry.group}:${entry.name}:${entry.version}"
                val expanded = expandedKey == key
                SettingsItem {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedKey = if (expanded) null else key }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = listOf(entry.licenseName, entry.version)
                                        .filter { it.isNotEmpty() }
                                        .joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                            )
                        }
                        if (expanded) {
                            MarkdownText(
                                markdown = entry.licenseText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
