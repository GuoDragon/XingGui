package com.example.xinggui.presentation.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.xinggui.R
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.presentation.common.PrivacyNoticeDialog
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosGroupCard
import com.example.xinggui.ui.components.IosPrimaryButton
import com.example.xinggui.ui.components.IosScreenScaffold
import com.example.xinggui.ui.components.IosTextButton
import com.example.xinggui.ui.components.IosTextField
import com.example.xinggui.ui.theme.IosRed
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToRoleSelect: () -> Unit,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> LoginContract.Presenter = { LoginPresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    val view = remember {
        object : LoginContract.View {
            override fun showSubmitting(submitting: Boolean) {
                isSubmitting = submitting
            }

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun navigateToRoleSelect() {
                onNavigateToRoleSelect()
            }

            override fun navigateToRegister() {
                onNavigateToRegister()
            }
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
    }
    DisposableEffect(Unit) {
        onDispose { presenter.detachView() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        IosScreenScaffold(
            title = stringResource(R.string.login_title),
            subtitle = stringResource(R.string.login_subtitle),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 28.dp)
        ) {
            IosGroupCard(
                modifier = Modifier.fillMaxWidth(),
                title = "欢迎回来",
                subtitle = "使用账号或邮箱继续登录"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IosTextField(
                        value = account,
                        onValueChange = {
                            account = it
                            errorMessage = null
                        },
                        label = stringResource(R.string.label_account),
                        enabled = !isSubmitting
                    )
                    IosTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = stringResource(R.string.label_password),
                        enabled = !isSubmitting,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    errorMessage?.let {
                        val backendUnavailable = it.contains("无法连接本地服务")
                        Text(
                            text = it,
                            color = IosRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        if (backendUnavailable) {
                            Text(
                                text = "排查：确认电脑端已运行 启动后端.bat，后端地址与 app_config.json 一致，然后点重试。",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IosTextButton(
                                text = "重试连接",
                                onClick = {
                                    scope.launch {
                                        presenter.onLoginClicked(account, password)
                                    }
                                },
                                enabled = !isSubmitting
                            )
                        }
                    }
                    IosPrimaryButton(
                        text = if (isSubmitting) stringResource(R.string.loading) else stringResource(R.string.action_login),
                        onClick = {
                            scope.launch {
                                presenter.onLoginClicked(account, password)
                            }
                        },
                        enabled = !isSubmitting,
                        loading = isSubmitting
                    )
                    IosTextButton(
                        text = stringResource(R.string.action_register),
                        onClick = presenter::onRegisterClicked,
                        enabled = !isSubmitting
                    )
                    IosTextButton(
                        text = "隐私与儿童数据说明",
                        onClick = { showPrivacy = true },
                        enabled = !isSubmitting
                    )
                }
            }
        }

        DecorativeStarsOverlay(
            modifier = Modifier.fillMaxSize(),
            pageKey = "login_screen"
        )
        if (showPrivacy) {
            PrivacyNoticeDialog(onDismiss = { showPrivacy = false })
        }
    }
}
