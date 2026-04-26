package com.example.xinggui.presentation.main

import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ScreenRenderState

data class MainShellUiState(
    val currentUserName: String,
    val username: String?,
    val currentUserId: String?,
    val currentRole: UserRole,
    val availableRoles: List<UserRole>,
    val currentChild: ChildProfile?,
    val availableChildren: List<ChildProfile>
)

interface MainContract {
    interface View {
        fun render(state: ScreenRenderState<MainShellUiState>) {
            when (state) {
                ScreenRenderState.Loading -> Unit
                is ScreenRenderState.Content -> showShell(state.data)
                is ScreenRenderState.Empty -> showError(state.message)
                is ScreenRenderState.Error -> showError(state.message)
            }
        }

        fun showShell(state: MainShellUiState)
        fun showError(message: String)
        fun navigateToLogin()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        suspend fun loadShell()
        suspend fun onChildSelected(childId: String)
        suspend fun onLogoutClicked()
    }
}
