package com.karasu256.karasulab.ui.licenses

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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

private const val MIT_TEXT = """MIT License

Copyright (c) 2026 Karasu Lab

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE."""

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
    val context = LocalContext.current
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
                        description = library.licenseUrl,
                        onClick = if (library.licenseUrl.isNotBlank()) {
                            { context.startActivity(Intent(Intent.ACTION_VIEW, library.licenseUrl.toUri())) }
                        } else {
                            null
                        },
                    )
                }
            }
            if (library.licenseName.contains("MIT", ignoreCase = true)) {
                Text(
                    text = MIT_TEXT,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            } else if (library.licenseUrl.isNotBlank()) {
                Text(
                    text = stringResource(R.string.licenses_open_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
    }
}

private fun LibraryLicense.key(): String = "$group:$name"
