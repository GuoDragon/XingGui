package com.example.xinggui.data.model

data class SessionState(
    val authToken: String? = null,
    val currentUserId: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val avatarKey: String? = null,
    val availableRoles: List<UserRole> = emptyList(),
    val activeRole: UserRole? = null,
    val selectedChildId: String? = null,
    val isAuthenticated: Boolean = false
)
