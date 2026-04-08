package com.example.xinggui.presentation.auth.roleselect

import com.example.xinggui.data.model.UserRole

interface RoleSelectContract {
    interface View {
        fun showSelectedRole(role: UserRole)
        fun showError(message: String)
        fun navigateToMain()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadInitialSelection()
        fun onContinue(role: UserRole)
    }
}
