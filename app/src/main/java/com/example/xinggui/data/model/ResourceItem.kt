package com.example.xinggui.data.model

data class ResourceItem(
    val resourceId: String,
    val title: String,
    val category: String,
    val isPaid: Boolean,
    val summary: String,
    val recommendedReason: String,
    val assetPath: String? = null,
    val sourceUrl: String? = null
)
