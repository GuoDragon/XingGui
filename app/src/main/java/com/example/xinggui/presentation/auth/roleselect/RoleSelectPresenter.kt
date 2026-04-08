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
        view?.showSelectedRole(repository.getSessionState().role)
    }

    override fun onContinue(role: UserRole) {
        repository.updateRole(role)
        view?.navigateToMain()
    }
}
