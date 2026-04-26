package com.example.xinggui.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.xinggui.R

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    IosLoadingState(
        message = stringResource(R.string.loading),
        modifier = modifier
    )
}
