package com.example.xinggui.presentation.auth.roleselect

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.xinggui.R
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosGroupCard
import com.example.xinggui.ui.components.IosIconBubble
import com.example.xinggui.ui.components.IosPrimaryButton
import com.example.xinggui.ui.components.IosScreenScaffold
import com.example.xinggui.ui.components.IosTextButton
import com.example.xinggui.ui.theme.IosBlue
import com.example.xinggui.ui.theme.IosBlueSoft
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosCardMuted
import com.example.xinggui.ui.theme.IosRed
import com.example.xinggui.ui.theme.IosSeparator
import com.example.xinggui.ui.theme.IosTextPrimary
import com.example.xinggui.ui.theme.IosTextSecondary
import kotlinx.coroutines.launch

@Composable
fun RoleSelectScreen(
    onBackClick: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> RoleSelectContract.Presenter = { RoleSelectPresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    var availableRoles by remember { mutableStateOf(emptyList<UserRole>()) }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val view = remember {
        object : RoleSelectContract.View {
            override fun showRoleOptions(roles: List<UserRole>, selectedRoleValue: UserRole?) {
                availableRoles = roles
                selectedRole = selectedRoleValue
            }

            override fun showError(message: String) {
                errorMessage = message
            }

            override fun showSubmitting(submitting: Boolean) {
                isSubmitting = submitting
            }

            override fun navigateToMain() {
                onNavigateToMain()
            }
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadInitialSelection()
    }
    DisposableEffect(Unit) {
        onDispose { presenter.detachView() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        IosScreenScaffold(
            title = stringResource(R.string.role_select_title),
            subtitle = stringResource(R.string.role_select_subtitle),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 28.dp)
        ) {
            IosGroupCard(title = "选择本次进入的身份") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    availableRoles.forEach { role ->
                        RoleOptionCard(
                            title = if (role == UserRole.PARENT) stringResource(R.string.role_parent) else stringResource(R.string.role_teacher),
                            description = if (role == UserRole.PARENT) stringResource(R.string.role_parent_desc) else stringResource(R.string.role_teacher_desc),
                            selected = selectedRole == role,
                            icon = if (role == UserRole.PARENT) Icons.Default.Home else Icons.Default.Groups,
                            onClick = { selectedRole = role }
                        )
                    }
                    if (availableRoles.isEmpty()) {
                        Text(
                            text = stringResource(R.string.role_select_empty),
                            color = IosRed,
                            style = MaterialTheme.typography.bodySmall
                        )
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
                        text = if (isSubmitting) stringResource(R.string.loading) else stringResource(R.string.action_continue),
                        onClick = {
                            selectedRole?.let { role ->
                                scope.launch { presenter.onContinue(role) }
                            }
                        },
                        enabled = !isSubmitting && selectedRole != null,
                        loading = isSubmitting
                    )
                    IosTextButton(
                        text = stringResource(R.string.action_back_to_login),
                        onClick = onBackClick,
                        enabled = !isSubmitting
                    )
                }
            }
        }

        DecorativeStarsOverlay(
            modifier = Modifier.fillMaxSize(),
            pageKey = "role_select_screen"
        )
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val background = if (selected) IosBlueSoft else IosCardMuted
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) IosBlue.copy(alpha = 0.46f) else IosSeparator
        )
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IosIconBubble(
                icon = icon,
                tint = if (selected) IosBlue else IosTextSecondary,
                background = if (selected) IosCard else IosCard
            )
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = IosTextPrimary)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = IosTextSecondary)
        }
    }
}
