package com.karasu256.karasulab.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> StepHost(
    step: T,
    steps: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = step,
        modifier = modifier,
        transitionSpec = {
            val direction = if (steps.indexOf(targetState) >= steps.indexOf(initialState)) 1 else -1
            (slideInHorizontally { it / 4 * direction } + fadeIn()) togetherWith
                (slideOutHorizontally { -it / 4 * direction } + fadeOut())
        },
        label = "step",
    ) { target ->
        content(target)
    }
}
