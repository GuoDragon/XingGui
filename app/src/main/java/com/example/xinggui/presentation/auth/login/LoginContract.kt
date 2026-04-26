package com.example.xinggui.presentation.auth.login

interface LoginContract {
    interface View {
        fun showSubmitting(submitting: Boolean)
        fun showError(message: String)
        fun navigateToRoleSelect()
        fun navigateToRegister()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        suspend fun onLoginClicked(account: String, password: String)
        fun onRegisterClicked()
    }
}
