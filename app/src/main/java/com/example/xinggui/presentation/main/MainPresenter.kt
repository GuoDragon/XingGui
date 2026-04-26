package com.example.xinggui.presentation.main

import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.presentation.common.ScreenRenderState

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

    override suspend fun loadShell() {
        view?.render(ScreenRenderState.Loading)
        val session = repository.getSessionState()
        val role = session.activeRole ?: run {
            view?.render(ScreenRenderState.Error("请先完成角色选择"))
            return
        }
        runCatching {
            val children = repository.getChildrenForActiveRole()
            val selectedChild = session.selectedChildId?.let { selectedId ->
                children.firstOrNull { it.childId == selectedId } ?: repository.getChildById(selectedId)
            } ?: children.firstOrNull()
            if (selectedChild != null && selectedChild.childId != session.selectedChildId) {
                repository.updateSelectedChild(selectedChild.childId)
            }
            MainShellUiState(
                currentUserName = session.displayName?.takeIf { it.isNotBlank() }
                    ?: session.username?.takeIf { it.isNotBlank() }
                    ?: "未命名账号",
                username = session.username,
                currentUserId = session.currentUserId,
                currentRole = role,
                availableRoles = session.availableRoles.ifEmpty { listOf(role) },
                currentChild = selectedChild,
                availableChildren = children
            )
        }.onSuccess { state ->
            view?.render(ScreenRenderState.Content(state))
        }.onFailure { error ->
            view?.render(ScreenRenderState.Error(error.message ?: "加载失败"))
        }
    }

    override suspend fun onChildSelected(childId: String) {
        runCatching {
            repository.updateSelectedChild(childId)
        }.onFailure { error ->
            view?.render(ScreenRenderState.Error(error.message ?: "切换儿童失败"))
        }
        loadShell()
    }

    override suspend fun onLogoutClicked() {
        runCatching {
            repository.logout()
        }.onSuccess {
            view?.navigateToLogin()
        }.onFailure { error ->
            view?.showError(error.message ?: "退出账号失败")
        }
    }
}
