package com.example.xinggui.presentation.auth.roleselect

import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository

class RoleSelectPresenter(
    private val repository: AppRepository
) : RoleSelectContract.Presenter {
    private var view: RoleSelectContract.View? = null

    override fun attachView(view: RoleSelectContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadInitialSelection() {
        val session = repository.getSessionState()
        view?.showRoleOptions(
            roles = session.availableRoles,
            selectedRole = session.activeRole ?: session.availableRoles.firstOrNull()
        )
    }

    override suspend fun onContinue(role: UserRole) {
        view?.showSubmitting(true)
        runCatching {
            repository.updateRole(role)
        }.onSuccess {
            view?.navigateToMain()
        }.onFailure { error ->
            view?.showError(error.message ?: "角色切换失败")
        }
        view?.showSubmitting(false)
    }
}
