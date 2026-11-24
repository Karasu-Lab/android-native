package com.karasu256.karasulab.init

import android.content.res.Resources
import androidx.annotation.StringRes

data class StepInfo(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
) {
    fun getTitle(resources: Resources): String = resources.getString(titleRes)

    fun getDescription(resources: Resources): String = resources.getString(descriptionRes)
}
