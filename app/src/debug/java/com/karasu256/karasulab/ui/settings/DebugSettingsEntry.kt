package com.karasu256.karasulab.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.karasu256.karasulab.R
import com.karasu256.karasulab.ui.component.IconTile
import com.karasu256.karasulab.ui.component.SettingsItem
import com.ramcosta.composedestinations.generated.destinations.DebugScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun DebugSettingsEntry(navigator: DestinationsNavigator) {
    SettingsItem(
        title = stringResource(R.string.settings_debug),
        description = stringResource(R.string.settings_debug_description),
        leading = { IconTile(Icons.Filled.BugReport) },
        onClick = { navigator.navigate(DebugScreenDestination) },
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}
