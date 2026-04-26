package com.example.xinggui.presentation.auth.roleselect

import com.example.xinggui.data.model.UserRole

interface RoleSelectContract {
    interface View {
        fun showRoleOptions(roles: List<UserRole>, selectedRole: UserRole?)
        fun showError(message: String)
        fun showSubmitting(submitting: Boolean)
        fun navigateToMain()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadInitialSelection()
        suspend fun onContinue(role: UserRole)
    }
}
