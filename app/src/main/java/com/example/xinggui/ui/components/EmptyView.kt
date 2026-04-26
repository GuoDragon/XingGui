package com.example.xinggui.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.xinggui.R
import com.example.xinggui.ui.theme.IosBlue

@Composable
fun EmptyView(
    message: String = stringResource(R.string.empty_state),
    modifier: Modifier = Modifier
) {
    IosMessageState(
        message = message,
        icon = Icons.Default.Inbox,
        tint = IosBlue,
        modifier = modifier
    )
}
