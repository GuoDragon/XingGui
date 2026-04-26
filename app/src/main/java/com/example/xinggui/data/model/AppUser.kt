package com.example.xinggui.data.model

data class AppUser(
    val userId: String,
    val username: String,
    val name: String,
    val roles: List<UserRole>,
    val email: String? = null,
    val childIds: List<String> = emptyList()
)
