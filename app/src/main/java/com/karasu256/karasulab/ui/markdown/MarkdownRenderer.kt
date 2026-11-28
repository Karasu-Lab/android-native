package com.karasu256.karasulab.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface MarkdownRenderer {
    @Composable
    fun Render(markdown: String, modifier: Modifier)
}

@Composable
fun MarkdownBody(markdown: String, modifier: Modifier = Modifier) {
    PlatformMarkdownRenderer.Render(markdown = markdown, modifier = modifier)
}
