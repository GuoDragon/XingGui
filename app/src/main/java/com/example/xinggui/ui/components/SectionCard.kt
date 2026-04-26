package com.example.xinggui.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    IosGroupCard(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        trailing = trailing,
        content = content
    )
}
