package com.example.xinggui.presentation.auth.register

import com.example.xinggui.data.model.CaptchaChallenge
import com.example.xinggui.data.model.UserRole

interface RegisterContract {
    interface View {
        fun showSubmitting(submitting: Boolean)
        fun showError(message: String)
        fun showCaptchaChallenge(challenge: CaptchaChallenge, message: String? = null)
        fun navigateToRoleSelect()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        suspend fun onCreateAccountClicked(
            username: String,
            name: String,
            email: String,
            password: String,
            confirmPassword: String,
            roles: List<UserRole>,
            captchaAnswer: String? = null
        )
        suspend fun onRefreshCaptchaClicked()
    }
}
