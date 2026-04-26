package com.example.xinggui.data.model

data class ResourceRuntimeState(
    val unlockedResourceIds: Set<String> = emptySet(),
    val searchHistory: List<String> = emptyList()
)
