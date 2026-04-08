package com.example.xinggui.presentation.auth.register

class RegisterPresenter : RegisterContract.Presenter {
    private var view: RegisterContract.View? = null

    override fun attachView(view: RegisterContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onCreateAccountClicked(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() -> view?.showError("请输入姓名")
            email.isBlank() -> view?.showError("请输入邮箱")
            password.isBlank() -> view?.showError("请输入密码")
            password != confirmPassword -> view?.showError("两次输入的密码不一致")
            else -> view?.navigateToRoleSelect()
        }
    }
}
