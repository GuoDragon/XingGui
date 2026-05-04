package com.example.xinggui.presentation.auth.login

import com.example.xinggui.data.repository.AppRepository

class LoginPresenter(
    private val repository: AppRepository
) : LoginContract.Presenter {
    private var view: LoginContract.View? = null

    override fun attachView(view: LoginContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override suspend fun onLoginClicked(account: String, password: String) {
        when {
            account.isBlank() -> view?.showError("请输入账号或邮箱")
            password.isBlank() -> view?.showError("请输入密码")
            else -> {
                view?.showSubmitting(true)
                runCatching {
                    repository.login(account = account, password = password)
                }.onSuccess {
                    view?.navigateToRoleSelect()
                }.onFailure { error ->
                    view?.showError(error.message ?: "登录失败，请稍后重试")
                }
                view?.showSubmitting(false)
            }
        }
    }

    override fun onRegisterClicked() {
        view?.navigateToRegister()
    }
}
