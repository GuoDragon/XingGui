package com.example.xinggui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.ui.theme.StarBlue
import com.example.xinggui.ui.theme.StarCyan

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val background = when (role) {
        UserRole.PARENT -> StarBlue.copy(alpha = 0.12f)
        UserRole.TEACHER -> StarCyan.copy(alpha = 0.14f)
    }
    val foreground = when (role) {
        UserRole.PARENT -> StarBlue
        UserRole.TEACHER -> StarCyan
    }
    Text(
        text = role.displayName,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = foreground,
        style = MaterialTheme.typography.labelLarge
    )
}
