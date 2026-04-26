package com.example.xinggui.presentation.auth.register

import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository

class RegisterPresenter(
    private val repository: AppRepository
) : RegisterContract.Presenter {
    private var view: RegisterContract.View? = null

    override fun attachView(view: RegisterContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override suspend fun onCreateAccountClicked(
        username: String,
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        roles: List<UserRole>
    ) {
        when {
            username.isBlank() -> view?.showError("请输入账号")
            name.isBlank() -> view?.showError("请输入姓名")
            email.isBlank() -> view?.showError("请输入邮箱")
            password.isBlank() -> view?.showError("请输入密码")
            password != confirmPassword -> view?.showError("两次输入的密码不一致")
            roles.isEmpty() -> view?.showError("请至少选择一个角色")
            else -> {
                view?.showSubmitting(true)
                runCatching {
                    repository.register(
                        username = username,
                        name = name,
                        email = email,
                        password = password,
                        roles = roles
                    )
                }.onSuccess {
                    view?.navigateToRoleSelect()
                }.onFailure { error ->
                    view?.showError(error.message ?: "注册失败，请稍后重试")
                }
                view?.showSubmitting(false)
            }
        }
    }
}
