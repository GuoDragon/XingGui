package com.example.xinggui.presentation.auth.register

interface RegisterContract {
    interface View {
        fun showSubmitting(submitting: Boolean)
        fun showError(message: String)
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
            roles: List<com.example.xinggui.data.model.UserRole>
        )
    }
}
