package com.example.xinggui.presentation.resources

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.ui.components.EmptyView
import com.example.xinggui.ui.components.ErrorView
import com.example.xinggui.ui.components.LoadingIndicator
import com.example.xinggui.ui.components.ModuleEntryCard
import com.example.xinggui.ui.components.SectionCard

@Composable
fun ResourcesScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    val presenter = remember { ResourcesPresenter(DataRepository) }
    var uiState by remember { mutableStateOf<ResourcesUiState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmpty by remember { mutableStateOf(false) }
    val view = remember {
        object : ResourcesContract.View {
            override fun showLoading() {
                isLoading = true
                showEmpty = false
            }

            override fun showContent(state: ResourcesUiState) {
                uiState = state
                isLoading = false
                showEmpty = false
                errorMessage = null
            }

            override fun showEmpty() {
                uiState = null
                isLoading = false
                showEmpty = true
            }

            override fun showError(message: String) {
                errorMessage = message
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { presenter.attachView(view) }
    LaunchedEffect(selectedChildId, currentRole) { presenter.loadData(currentRole) }
    DisposableEffect(Unit) { onDispose { presenter.detachView() } }

    when {
        errorMessage != null -> ErrorView(message = errorMessage!!, onRetry = { presenter.loadData(currentRole) }, modifier = modifier)
        isLoading -> LoadingIndicator(modifier = modifier)
        showEmpty || uiState == null -> EmptyView(message = "暂无资源演示数据", modifier = modifier)
        else -> {
            val state = uiState!!
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.placeholder_search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                SectionCard(
                    title = "智能推荐",
                    subtitle = if (state.role == UserRole.PARENT) {
                        "家长模式优先推荐家庭跟进材料。"
                    } else {
                        "教师模式优先推荐课堂干预参考。"
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.recommended.forEach { item ->
                            ModuleEntryCard(
                                title = item.title,
                                description = item.summary,
                                icon = Icons.Default.StarBorder,
                                badgeText = if (item.isPaid) stringResource(R.string.label_paid) else item.category
                            )
                        }
                    }
                }
                SectionCard(title = "分类", subtitle = "资讯、案例、政策解读与教具指南") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.categories.chunked(2).forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { item ->
                                    ModuleEntryCard(
                                        title = item.title,
                                        description = "${item.count} 条演示内容",
                                        icon = if (item.isPaid) Icons.Default.Lock else Icons.Default.Search,
                                        modifier = Modifier.weight(1f),
                                        badgeText = if (item.isPaid) stringResource(R.string.label_paid) else null
                                    )
                                }
                            }
                        }
                    }
                }
                SectionCard(title = "全部资源") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.allItems.forEach { item ->
                            Text(
                                text = "• ${item.title} | ${item.category}${if (item.isPaid) " | 付费" else ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}


