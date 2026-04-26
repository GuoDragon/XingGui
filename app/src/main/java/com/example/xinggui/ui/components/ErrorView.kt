package com.example.xinggui.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.xinggui.R
import com.example.xinggui.ui.theme.IosRed

@Composable
fun ErrorView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    IosMessageState(
        message = message,
        icon = Icons.Default.WarningAmber,
        tint = IosRed,
        actionText = if (onRetry != null) stringResource(R.string.action_retry) else null,
        onAction = onRetry,
        modifier = modifier
    )
}
