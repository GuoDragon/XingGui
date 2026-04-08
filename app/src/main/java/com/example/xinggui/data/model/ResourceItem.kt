package com.example.xinggui.data.model

data class ResourceItem(
    val resourceId: String,
    val title: String,
    val category: String,
    val isPaid: Boolean,
    val summary: String,
    val recommendedReason: String
)
