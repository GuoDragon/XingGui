package com.example.xinggui.presentation.auth.roleselect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.xinggui.R
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.ui.theme.StarBackground
import com.example.xinggui.ui.theme.StarSurfaceAlt

@Composable
fun RoleSelectScreen(
    onBackClick: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presenter = remember { RoleSelectPresenter(DataRepository) }
    var selectedRole by remember { mutableStateOf(UserRole.PARENT) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val view = remember {
        object : RoleSelectContract.View {
            override fun showSelectedRole(role: UserRole) {
                selectedRole = role
            }

            override fun showError(message: String) {
                errorMessage = message
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.role_select_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.role_select_subtitle), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        RoleOptionCard(
            title = stringResource(R.string.role_parent),
            description = stringResource(R.string.role_parent_desc),
            selected = selectedRole == UserRole.PARENT,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            onClick = { selectedRole = UserRole.PARENT }
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleOptionCard(
            title = stringResource(R.string.role_teacher),
            description = stringResource(R.string.role_teacher_desc),
            selected = selectedRole == UserRole.TEACHER,
            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
            onClick = { selectedRole = UserRole.TEACHER }
        )
        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { presenter.onContinue(selectedRole) }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_continue))
        }
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_back_to_login))
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val background = if (selected) StarSurfaceAlt else MaterialTheme.colorScheme.surface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        tonalElevation = if (selected) 2.dp else 0.dp,
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            icon()
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
