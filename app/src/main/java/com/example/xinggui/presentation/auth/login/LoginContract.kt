package com.example.xinggui.presentation.auth.login

interface LoginContract {
    interface View {
        fun showError(message: String)
        fun navigateToRoleSelect()
        fun navigateToRegister()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onLoginClicked(email: String, password: String)
        fun onRegisterClicked()
    }
}
