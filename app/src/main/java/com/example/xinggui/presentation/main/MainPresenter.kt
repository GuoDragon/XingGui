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
                currentUserEmail = session.email,
                currentUserAvatarKey = session.avatarKey,
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

    override suspend fun onAccountProfileSaved(
        displayName: String,
        email: String?,
        avatarKey: String?
    ): Boolean {
        return runCatching {
            require(displayName.isNotBlank()) { "请填写显示名" }
            repository.updateCurrentUserProfile(
                displayName = displayName.trim(),
                email = email?.trim()?.takeIf { it.isNotBlank() },
                avatarKey = avatarKey
            )
        }.onSuccess {
            loadShell()
            view?.showMessage("账号资料已更新")
        }.onFailure { error ->
            view?.showMessage(error.message ?: "账号资料保存失败")
        }.isSuccess
    }

    override suspend fun onChildProfileSaved(
        childId: String,
        name: String,
        birthDate: String?,
        interventionStartDate: String?,
        avatarKey: String?
    ): Boolean {
        return runCatching {
            require(childId.isNotBlank()) { "缺少儿童 ID" }
            require(name.isNotBlank()) { "请填写儿童姓名" }
            repository.updateChildProfile(
                childId = childId,
                name = name.trim(),
                birthDate = birthDate?.trim()?.takeIf { it.isNotBlank() },
                interventionStartDate = interventionStartDate?.trim()?.takeIf { it.isNotBlank() },
                avatarKey = avatarKey
            )
        }.onSuccess {
            loadShell()
            view?.showMessage("儿童资料已更新")
        }.onFailure { error ->
            view?.showMessage(error.message ?: "儿童资料保存失败")
        }.isSuccess
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

    override suspend fun onLogoutAllDevicesClicked() {
        runCatching {
            repository.logoutAllDevices()
        }.onSuccess {
            view?.navigateToLogin()
        }.onFailure { error ->
            view?.showError(error.message ?: "退出全部设备失败")
        }
    }
}
