package com.karasu256.karasulab.ui.markdown

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object PlatformMarkdownRenderer : MarkdownRenderer {
    @Composable
    override fun Render(markdown: String, modifier: Modifier) {
        Text(text = markdown, modifier = modifier)
    }
}
