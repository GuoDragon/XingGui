package com.example.xinggui.data.model

data class AppUser(
    val userId: String,
    val name: String,
    val role: UserRole,
    val email: String? = null,
    val childIds: List<String> = emptyList()
)
