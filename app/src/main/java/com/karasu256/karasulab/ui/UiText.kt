package com.karasu256.karasulab.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.karasu256.karasulab.R
import java.io.IOException

data class UiText(@param:StringRes val id: Int, val args: List<Any> = emptyList()) {
    @Composable
    fun resolve(): String = stringResource(id, *args.toTypedArray())
}

fun Throwable.toUiText(fallback: Int = R.string.error_unknown): UiText =
    UiText(if (this is IOException) R.string.error_network else fallback)
