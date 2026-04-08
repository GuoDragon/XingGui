package com.example.xinggui.presentation.auth.register

interface RegisterContract {
    interface View {
        fun showError(message: String)
        fun navigateToRoleSelect()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onCreateAccountClicked(name: String, email: String, password: String, confirmPassword: String)
    }
}
