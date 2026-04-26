package com.example.xinggui.presentation.common

sealed interface ScreenRenderState<out T> {
    data object Loading : ScreenRenderState<Nothing>
    data class Content<T>(val data: T) : ScreenRenderState<T>
    data class Empty(val message: String) : ScreenRenderState<Nothing>
    data class Error(val message: String) : ScreenRenderState<Nothing>
}

