package com.example.xinggui.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.xinggui.R
import com.example.xinggui.data.model.ChildProfile
import com.example.xinggui.data.model.UserRole

@Composable
fun ChildSelectorBar(
    role: UserRole,
    currentChild: ChildProfile?,
    children: List<ChildProfile>,
    onChildSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = stringResource(R.string.label_current_child),
        modifier = modifier,
        subtitle = if (role == UserRole.TEACHER) stringResource(R.string.action_select_child) else null
    ) {
        if (currentChild == null) {
            EmptyView(message = stringResource(R.string.empty_state))
            return@SectionCard
        }

        var expanded by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentChild.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (role == UserRole.TEACHER && children.size > 1) {
                    TextButton(onClick = { expanded = true }) {
                        Text(text = stringResource(R.string.action_select_child))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        children.forEach { child ->
                            DropdownMenuItem(
                                text = { Text(text = "${child.name} · ${child.age}岁") },
                                onClick = {
                                    expanded = false
                                    onChildSelected(child.childId)
                                }
                            )
                        }
                    }
                }
            }
            Text(
                text = "${currentChild.age}岁",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "干预时长 ${currentChild.interventionDuration}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
