package com.example.xinggui.presentation.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ChildInfoProfileCard
import com.example.xinggui.ui.theme.IosBlueSoft
import com.example.xinggui.ui.theme.IosGroupedBackground
import com.example.xinggui.ui.theme.IosRed
import com.example.xinggui.ui.theme.IosSeparator
import com.example.xinggui.ui.theme.StarBlue
import com.example.xinggui.ui.theme.StarSurface
import com.example.xinggui.ui.theme.StarSurfaceAlt
import com.example.xinggui.ui.theme.StarTextPrimary
import com.example.xinggui.ui.theme.StarTextSecondary

private val ProfileGroupedBackground = IosGroupedBackground
private val ProfileCardBorder = IosSeparator
private val ProfileDividerColor = IosSeparator
private val ProfileAccentTint = IosBlueSoft
private val ProfileDanger = IosRed

@Composable
fun ProfileScreen(
    state: MainShellUiState,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileGroupedBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "我的",
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                style = MaterialTheme.typography.headlineLarge,
                color = StarTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            AccountSummaryCard(state = state)
        }
        item {
            CurrentChildSection(state = state)
        }
        item {
            AccountActionGroup(onLogoutClick = onLogoutClick)
        }
    }
}

@Composable
private fun AccountSummaryCard(state: MainShellUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = StarSurface),
        border = BorderStroke(1.dp, ProfileCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ProfileAccentTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = StarBlue
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.currentUserName,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = StarTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        CurrentRolePill(roleLabel = state.currentRole.displayName)
                    }
                    Text(
                        text = "账号 ${state.username.displayOrPlaceholder()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF7FAFD))
            ) {
                ProfileValueRow(label = "当前身份", value = state.currentRole.displayName, valueColor = StarBlue)
                ProfileDivider()
                ProfileValueRow(label = "可用身份", value = state.availableRoleLabel())
                state.currentUserId?.takeIf { it.isNotBlank() }?.let { userId ->
                    ProfileDivider()
                    ProfileValueRow(label = "用户 ID", value = userId)
                }
            }
        }
    }
}

@Composable
private fun CurrentRolePill(roleLabel: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ProfileAccentTint
    ) {
        Text(
            text = roleLabel,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = StarBlue,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CurrentChildSection(state: MainShellUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "儿童信息",
            trailing = state.linkedChildrenBadge()
        )
        val child = state.currentChild
        if (child == null) {
            EmptyChildInfo()
        } else {
            ChildInfoProfileCard(
                childName = child.name,
                age = child.age,
                interventionDuration = child.interventionDuration
            )
        }
    }
}

@Composable
private fun AccountActionGroup(onLogoutClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "账号操作")
        SettingsGroupCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogoutClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ProfileDanger.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ProfileDanger
                    )
                }
                Text(
                    text = "退出账号 / 切换账号",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ProfileDanger,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFB4BFCC)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = StarTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        if (trailing != null) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, ProfileCardBorder)
            ) {
                Text(
                    text = trailing,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = StarTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StarSurface),
        border = BorderStroke(1.dp, ProfileCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun EmptyChildInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StarSurface),
        border = BorderStroke(1.dp, ProfileCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(StarSurfaceAlt),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = StarBlue
                )
            }
            Text(
                text = "暂无关联儿童信息",
                style = MaterialTheme.typography.bodyMedium,
                color = StarTextSecondary
            )
        }
    }
}

@Composable
private fun ProfileValueRow(
    label: String,
    value: String,
    valueColor: Color = StarTextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = StarTextSecondary
        )
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = 0.6.dp,
        color = ProfileDividerColor
    )
}

private fun MainShellUiState.availableRoleLabel(): String {
    return availableRoles.ifEmpty { listOf(currentRole) }
        .joinToString(" / ") { it.displayName }
}

private fun MainShellUiState.linkedChildrenBadge(): String? {
    return if (currentRole == UserRole.TEACHER && availableChildren.size > 1) {
        "已关联 ${availableChildren.size} 名儿童"
    } else {
        null
    }
}

private fun String?.displayOrPlaceholder(): String = if (isNullOrBlank()) "未填写" else this
