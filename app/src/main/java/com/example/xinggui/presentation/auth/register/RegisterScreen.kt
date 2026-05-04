package com.example.xinggui.presentation.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.xinggui.R
import com.example.xinggui.data.model.CaptchaChallenge
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.presentation.common.PrivacyNoticeDialog
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosGroupCard
import com.example.xinggui.ui.components.IosPrimaryButton
import com.example.xinggui.ui.components.IosScreenScaffold
import com.example.xinggui.ui.components.IosSectionHeader
import com.example.xinggui.ui.components.IosTextButton
import com.example.xinggui.ui.components.IosTextField
import com.example.xinggui.ui.theme.IosRed
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onNavigateToRoleSelect: () -> Unit,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> RegisterContract.Presenter = { RegisterPresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var captchaChallenge by remember { mutableStateOf<CaptchaChallenge?>(null) }
    var captchaAnswer by remember { mutableStateOf("") }
    val selectedRoles = remember { mutableStateListOf<UserRole>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    val view = remember {
        object : RegisterContract.View {
            override fun showSubmitting(submitting: Boolean) {
                isSubmitting = submitting
            }

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun showCaptchaChallenge(challenge: CaptchaChallenge, message: String?) {
                captchaChallenge = challenge
                captchaAnswer = ""
                errorMessage = message ?: "请先完成验证码后继续注册"
            }

            override fun navigateToRoleSelect() {
                onNavigateToRoleSelect()
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
            title = stringResource(R.string.register_title),
            subtitle = stringResource(R.string.register_subtitle),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 28.dp)
        ) {
            IosGroupCard(
                modifier = Modifier.fillMaxWidth(),
                title = "创建可切换身份的账号",
                subtitle = "后续可按家长或教师身份进入"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    IosTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = stringResource(R.string.label_username),
                        enabled = !isSubmitting
                    )
                    IosTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = stringResource(R.string.label_name),
                        enabled = !isSubmitting
                    )
                    IosTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = stringResource(R.string.label_email),
                        enabled = !isSubmitting
                    )
                    IosTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = stringResource(R.string.label_password),
                        enabled = !isSubmitting,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    IosTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = stringResource(R.string.label_confirm_password),
                        enabled = !isSubmitting,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    IosSectionHeader(title = stringResource(R.string.label_register_roles))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleChip(
                            role = UserRole.PARENT,
                            selectedRoles = selectedRoles,
                            enabled = !isSubmitting
                        )
                        RoleChip(
                            role = UserRole.TEACHER,
                            selectedRoles = selectedRoles,
                            enabled = !isSubmitting
                        )
                    }
                    captchaChallenge?.let { challenge ->
                        IosGroupCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "安全验证",
                            subtitle = "请回答：${challenge.question}"
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                IosTextField(
                                    value = captchaAnswer,
                                    onValueChange = {
                                        captchaAnswer = it
                                        errorMessage = null
                                    },
                                    label = "验证码答案",
                                    enabled = !isSubmitting
                                )
                                IosTextButton(
                                    text = "换一道题",
                                    onClick = {
                                        scope.launch { presenter.onRefreshCaptchaClicked() }
                                    },
                                    enabled = !isSubmitting
                                )
                            }
                        }
                    }
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = IosRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    IosPrimaryButton(
                        text = if (isSubmitting) stringResource(R.string.loading) else stringResource(R.string.action_create_account),
                        onClick = {
                            scope.launch {
                                presenter.onCreateAccountClicked(
                                    username = username,
                                    name = name,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    roles = selectedRoles.toList(),
                                    captchaAnswer = captchaAnswer
                                )
                            }
                        },
                        enabled = !isSubmitting,
                        loading = isSubmitting
                    )
                    IosTextButton(
                        text = stringResource(R.string.action_back_to_login),
                        onClick = onBackClick,
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
            pageKey = "register_screen"
        )
        if (showPrivacy) {
            PrivacyNoticeDialog(onDismiss = { showPrivacy = false })
        }
    }
}

@Composable
private fun RoleChip(
    role: UserRole,
    selectedRoles: MutableList<UserRole>,
    enabled: Boolean
) {
    val selected = role in selectedRoles
    FilterChip(
        selected = selected,
        onClick = {
            if (selected) {
                selectedRoles.remove(role)
            } else {
                selectedRoles.add(role)
            }
        },
        enabled = enabled,
        label = { Text(text = role.displayName) }
    )
}
