package com.example.xinggui.data.model

enum class UserRole(val storageValue: String, val displayName: String) {
    PARENT("PARENT", "家长"),
    TEACHER("TEACHER", "教师");

    companion object {
        fun fromStorageValue(value: String?): UserRole {
            return entries.firstOrNull { it.storageValue == value } ?: PARENT
        }
    }
}
