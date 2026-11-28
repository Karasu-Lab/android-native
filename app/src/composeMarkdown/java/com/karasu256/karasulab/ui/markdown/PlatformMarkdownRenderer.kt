package com.karasu256.karasulab.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.jeziellago.compose.markdowntext.MarkdownText

object PlatformMarkdownRenderer : MarkdownRenderer {
    @Composable
    override fun Render(markdown: String, modifier: Modifier) {
        MarkdownText(markdown = markdown, modifier = modifier)
    }
}
