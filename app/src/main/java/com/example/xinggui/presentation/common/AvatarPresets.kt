package com.example.xinggui.presentation.common

import com.example.xinggui.R

data class AvatarPreset(
    val key: String,
    val drawableRes: Int,
    val label: String
)

object AvatarPresets {
    val userPresets: List<AvatarPreset> = listOf(
        AvatarPreset("user_sun", R.drawable.avatar_user_sun, "暖阳"),
        AvatarPreset("user_leaf", R.drawable.avatar_user_leaf, "绿芽"),
        AvatarPreset("user_berry", R.drawable.avatar_user_berry, "莓果")
    )

    val childPresets: List<AvatarPreset> = listOf(
        AvatarPreset("child_sky", R.drawable.child_profile_avatar, "蓝天"),
        AvatarPreset("child_peach", R.drawable.avatar_child_peach, "蜜桃"),
        AvatarPreset("child_mint", R.drawable.avatar_child_mint, "薄荷")
    )

    fun userDrawableRes(key: String?): Int {
        return userPresets.firstOrNull { it.key == key }?.drawableRes ?: userPresets.first().drawableRes
    }

    fun childDrawableRes(key: String?): Int {
        return childPresets.firstOrNull { it.key == key }?.drawableRes ?: childPresets.first().drawableRes
    }

    fun defaultUserKey(): String = userPresets.first().key

    fun defaultChildKey(): String = childPresets.first().key
}
