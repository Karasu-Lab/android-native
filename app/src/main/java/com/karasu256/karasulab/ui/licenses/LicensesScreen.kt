package com.karasu256.karasulab.ui.licenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.GeneratedLicenseCatalog
import com.karasu256.karasulab.data.LibraryLicense
import com.karasu256.karasulab.ui.component.SectionHeader
import com.karasu256.karasulab.ui.component.SettingsGroup
import com.karasu256.karasulab.ui.component.SettingsItem
import com.karasu256.karasulab.ui.settings.SettingsScaffold
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination<RootGraph>
@Composable
fun LicensesScreen(navigator: DestinationsNavigator) {
    val libraries = remember { GeneratedLicenseCatalog.libraries }
    val appLicense = libraries.firstOrNull()
    val dependencies = libraries.drop(1)
    var expandedKey by remember { mutableStateOf<String?>(null) }

    fun toggle(library: LibraryLicense) {
        val key = library.key()
        expandedKey = if (expandedKey == key) null else key
    }

    SettingsScaffold(title = stringResource(R.string.licenses_title), navigator = navigator) {
        if (appLicense != null) {
            SectionHeader(stringResource(R.string.licenses_app_license))
            SettingsGroup {
                item {
                    LicenseEntry(
                        library = appLicense,
                        expanded = expandedKey == appLicense.key(),
                        onClick = { toggle(appLicense) },
                    )
                }
            }
        }
        SectionHeader(stringResource(R.string.licenses_open_source))
        SettingsGroup {
            dependencies.forEach { library ->
                item {
                    LicenseEntry(
                        library = library,
                        expanded = expandedKey == library.key(),
                        onClick = { toggle(library) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LicenseEntry(
    library: LibraryLicense,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column {
        SettingsItem(
            title = library.name,
            description = listOf(library.version, library.licenseName)
                .filter { it.isNotBlank() }
                .joinToString(" / ")
                .ifBlank { stringResource(R.string.licenses_unknown) },
            trailing = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            },
            onClick = onClick,
        )
        if (expanded) {
            MarkdownText(
                markdown = library.licenseText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private fun LibraryLicense.key(): String = "$group:$name:$version"
