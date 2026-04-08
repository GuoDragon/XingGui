package com.example.xinggui.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.xinggui.R
import com.example.xinggui.ui.theme.StarBackground

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onNavigateToRoleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presenter = remember { RegisterPresenter() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val view = remember {
        object : RegisterContract.View {
            override fun showError(message: String) {
                errorMessage = message
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.register_subtitle), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it; errorMessage = null }, label = { Text(stringResource(R.string.label_name)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it; errorMessage = null }, label = { Text(stringResource(R.string.label_email)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; errorMessage = null }, label = { Text(stringResource(R.string.label_password)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; errorMessage = null }, label = { Text(stringResource(R.string.label_confirm_password)) }, modifier = Modifier.fillMaxWidth())
        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { presenter.onCreateAccountClicked(name, email, password, confirmPassword) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.action_create_account))
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_back_to_login))
        }
    }
}
