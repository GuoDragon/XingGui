package com.example.xinggui.data.model

data class CaptchaChallenge(
    val captchaId: String,
    val question: String,
    val expiresAt: Long
)
