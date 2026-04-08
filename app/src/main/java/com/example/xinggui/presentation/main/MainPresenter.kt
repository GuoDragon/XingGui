package com.example.xinggui.presentation.main

import com.example.xinggui.data.repository.AppRepository

class MainPresenter(
    private val repository: AppRepository
) : MainContract.Presenter {
    private var view: MainContract.View? = null

    override fun attachView(view: MainContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadShell() {
        val session = repository.getSessionState()
        val user = repository.getCurrentUser()
        val children = repository.getChildrenForUser(session.currentUserId, session.role)
        val currentChild = repository.getChildById(session.selectedChildId)
        if (user == null) {
            view?.showError("当前角色数据缺失")
            return
        }
        view?.showShell(
            MainShellUiState(
                currentUserName = user.name,
                currentRole = session.role,
                currentChild = currentChild,
                availableChildren = children
            )
        )
    }

    override fun onChildSelected(childId: String) {
        repository.updateSelectedChild(childId)
        loadShell()
    }
}
