package com.example.xinggui.presentation.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.formatProfileDateForDisplay
import com.example.xinggui.data.model.normalizeProfileDate
import com.example.xinggui.data.model.parseProfileDate
import com.example.xinggui.presentation.common.AvatarPreset
import com.example.xinggui.presentation.common.AvatarPresets
import com.example.xinggui.ui.theme.IosBlue
import com.example.xinggui.ui.theme.IosSeparator
import com.example.xinggui.ui.theme.StarTextPrimary
import com.example.xinggui.ui.theme.StarTextSecondary
import java.time.LocalDate

@Composable
fun AccountProfileEditDialog(
    state: MainShellUiState,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (displayName: String, email: String?, avatarKey: String) -> Unit
) {
    var displayName by remember(state.currentUserName) { mutableStateOf(state.currentUserName) }
    var email by remember(state.currentUserEmail) { mutableStateOf(state.currentUserEmail.orEmpty()) }
    var avatarKey by remember(state.currentUserAvatarKey) {
        mutableStateOf(state.currentUserAvatarKey ?: AvatarPresets.defaultUserKey())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ProfileEditDialogShell(
        title = "编辑账号资料",
        saving = saving,
        onDismiss = onDismiss,
        errorMessage = errorMessage,
        confirmText = "保存账号",
        onConfirm = {
            val validation = validateAccountProfile(displayName, email)
            if (validation != null) {
                errorMessage = validation
            } else {
                errorMessage = null
                onSave(displayName.trim(), email.trim().takeIf { it.isNotBlank() }, avatarKey)
            }
        }
    ) {
        ReadOnlyProfileLine(label = "用户名", value = state.username ?: "未填写")
        OutlinedTextField(
            value = displayName,
            onValueChange = {
                displayName = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("显示名") }
        )
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("邮箱") },
            placeholder = { Text("name@example.com") }
        )
        AvatarPresetPicker(
            title = "预设头像",
            presets = AvatarPresets.userPresets,
            selectedKey = avatarKey,
            onSelected = { avatarKey = it }
        )
    }
}

@Composable
fun ChildProfileEditDialog(
    child: ChildProfile,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        birthDate: String,
        interventionStartDate: String,
        avatarKey: String
    ) -> Unit
) {
    var name by remember(child.childId, child.name) { mutableStateOf(child.name) }
    var birthDate by remember(child.childId, child.birthDate) { mutableStateOf(child.birthDate.orEmpty()) }
    var interventionStartDate by remember(child.childId, child.interventionStartDate) {
        mutableStateOf(child.interventionStartDate.orEmpty())
    }
    var avatarKey by remember(child.childId, child.avatarKey) {
        mutableStateOf(child.avatarKey ?: AvatarPresets.defaultChildKey())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ProfileEditDialogShell(
        title = "编辑当前孩子资料",
        saving = saving,
        onDismiss = onDismiss,
        errorMessage = errorMessage,
        confirmText = "保存孩子资料",
        onConfirm = {
            val normalizedBirthDate = normalizeProfileDate(birthDate)
            val normalizedStartDate = normalizeProfileDate(interventionStartDate)
            val validation = validateChildProfile(name, normalizedBirthDate, normalizedStartDate)
            if (validation != null) {
                errorMessage = validation
            } else {
                errorMessage = null
                onSave(name.trim(), normalizedBirthDate, normalizedStartDate, avatarKey)
            }
        }
    ) {
        ReadOnlyProfileLine(label = "儿童 ID", value = child.childId)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("姓名") }
        )
        OutlinedTextField(
            value = birthDate,
            onValueChange = {
                birthDate = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("出生日期") },
            placeholder = { Text("yyyy-MM-dd") },
            supportingText = { ProfileDateSupportingText(birthDate) }
        )
        OutlinedTextField(
            value = interventionStartDate,
            onValueChange = {
                interventionStartDate = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("干预开始日期") },
            placeholder = { Text("yyyy-MM-dd") },
            supportingText = { ProfileDateSupportingText(interventionStartDate) }
        )
        AvatarPresetPicker(
            title = "预设头像",
            presets = AvatarPresets.childPresets,
            selectedKey = avatarKey,
            onSelected = { avatarKey = it }
        )
    }
}

@Composable
private fun ProfileEditDialogShell(
    title: String,
    saving: Boolean,
    onDismiss: () -> Unit,
    errorMessage: String?,
    confirmText: String,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue)
            ) {
                Text(text = if (saving) "保存中..." else confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AvatarPresetPicker(
    title: String,
    presets: List<AvatarPreset>,
    selectedKey: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = StarTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presets.forEach { preset ->
                val selected = preset.key == selectedKey
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelected(preset.key) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) Color(0xFFEAF4FF) else Color.White,
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) IosBlue else IosSeparator
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = preset.drawableRes),
                            contentDescription = preset.label,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) IosBlue else StarTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyProfileLine(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFD)),
        border = BorderStroke(1.dp, IosSeparator)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = StarTextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = StarTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileDateSupportingText(value: String) {
    val label = if (parseProfileDate(value) != null) formatProfileDateForDisplay(value) else null
    if (label != null) {
        Text("显示为 $label")
    } else {
        Text("格式：yyyy-MM-dd")
    }
}

private fun validateAccountProfile(displayName: String, email: String): String? {
    if (displayName.isBlank()) {
        return "请填写显示名"
    }
    if (email.isNotBlank() && !email.contains("@")) {
        return "请填写有效邮箱"
    }
    return null
}

private fun validateChildProfile(
    name: String,
    birthDate: String,
    interventionStartDate: String
): String? {
    if (name.isBlank()) {
        return "请填写儿童姓名"
    }
    val birth = parseProfileDate(birthDate) ?: return "请按 yyyy-MM-dd 填写出生日期"
    val start = parseProfileDate(interventionStartDate) ?: return "请按 yyyy-MM-dd 填写干预开始日期"
    val today = LocalDate.now()
    if (birth.isAfter(today)) {
        return "出生日期不能晚于今天"
    }
    if (start.isAfter(today)) {
        return "干预开始日期不能晚于今天"
    }
    if (start.isBefore(birth)) {
        return "干预开始日期不能早于出生日期"
    }
    return null
}
