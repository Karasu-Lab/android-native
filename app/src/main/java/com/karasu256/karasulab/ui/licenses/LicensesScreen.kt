package com.karasu256.karasulab.ui.licenses

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.ramcosta.composedestinations.generated.destinations.LicenseDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination<RootGraph>
@Composable
fun LicensesScreen(navigator: DestinationsNavigator) {
    val libraries = remember { GeneratedLicenseCatalog.libraries }
    val appLicense = libraries.firstOrNull()
    val dependencies = libraries.drop(1)
    SettingsScaffold(title = stringResource(R.string.licenses_title), navigator = navigator) {
        if (appLicense != null) {
            SectionHeader(stringResource(R.string.licenses_app_license))
            SettingsGroup {
                item {
                    SettingsItem(
                        title = appLicense.name,
                        description = appLicense.licenseName,
                        onClick = {
                            navigator.navigate(LicenseDetailScreenDestination(libraryKey = appLicense.key()))
                        },
                    )
                }
            }
        }
        SectionHeader(stringResource(R.string.licenses_open_source))
        SettingsGroup {
            dependencies.forEach { library ->
                item {
                    SettingsItem(
                        title = library.name,
                        description = listOf(library.version, library.licenseName)
                            .filter { it.isNotBlank() }
                            .joinToString(" / ")
                            .ifBlank { stringResource(R.string.licenses_unknown) },
                        onClick = {
                            navigator.navigate(LicenseDetailScreenDestination(libraryKey = library.key()))
                        },
                    )
                }
            }
        }
    }
}

@Destination<RootGraph>
@Composable
fun LicenseDetailScreen(navigator: DestinationsNavigator, libraryKey: String) {
    val library = remember(libraryKey) {
        GeneratedLicenseCatalog.libraries.firstOrNull { it.key() == libraryKey }
    }
    SettingsScaffold(
        title = library?.name ?: stringResource(R.string.licenses_title),
        navigator = navigator,
    ) {
        if (library != null) {
            SettingsGroup {
                item {
                    SettingsItem(
                        title = library.group,
                        description = library.version,
                    )
                }
                item {
                    SettingsItem(
                        title = library.licenseName.ifBlank { stringResource(R.string.licenses_unknown) },
                        description = library.licenseUrl.takeIf { it.isNotBlank() },
                    )
                }
            }
            MarkdownText(
                markdown = library.licenseText,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}

private fun LibraryLicense.key(): String = "$group:$name"
