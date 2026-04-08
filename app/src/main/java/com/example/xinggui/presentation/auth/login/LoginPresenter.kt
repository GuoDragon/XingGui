package com.example.xinggui.presentation.auth.login

class LoginPresenter : LoginContract.Presenter {
    private var view: LoginContract.View? = null

    override fun attachView(view: LoginContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onLoginClicked(email: String, password: String) {
        when {
            email.isBlank() -> view?.showError("请输入邮箱")
            password.isBlank() -> view?.showError("请输入密码")
            else -> view?.navigateToRoleSelect()
        }
    }

    override fun onRegisterClicked() {
        view?.navigateToRegister()
    }
}
