package com.example.xinggui.data.model

data class SessionState(
    val currentUserId: String,
    val role: UserRole,
    val selectedChildId: String
)
