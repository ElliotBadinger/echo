package eu.mrogalski.saidit.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

fun ComposeView.setContent(content: @Composable () -> Unit) {
    this.setContent(content)
}