package com.karasu256.karasulab.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeveloperOptionsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val settings: Settings = SharedPreferencesSettings.Factory(context).create(SETTINGS_NAME)

    private val _enabled = MutableStateFlow(settings.getBoolean(ENABLED_KEY, false))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        settings.putBoolean(ENABLED_KEY, enabled)
        _enabled.value = enabled
    }

    companion object {
        private const val SETTINGS_NAME = "developer_options"
        private const val ENABLED_KEY = "enabled"
    }
}
