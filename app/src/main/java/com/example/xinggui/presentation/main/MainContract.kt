package com.example.xinggui.presentation.main

import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.UserRole

data class MainShellUiState(
    val currentUserName: String,
    val currentRole: UserRole,
    val currentChild: ChildProfile?,
    val availableChildren: List<ChildProfile>
)

interface MainContract {
    interface View {
        fun showShell(state: MainShellUiState)
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadShell()
        fun onChildSelected(childId: String)
    }
}
