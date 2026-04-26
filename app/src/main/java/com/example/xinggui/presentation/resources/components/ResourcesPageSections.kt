package com.example.xinggui.presentation.resources.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xinggui.R
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.resources.ResourceCardUiModel
import com.example.xinggui.presentation.resources.ResourceCategoryNames
import com.example.xinggui.presentation.resources.ResourceCategoryUiModel
import com.example.xinggui.presentation.resources.ResourcesUiState
import com.example.xinggui.ui.components.EmptyView
import com.example.xinggui.ui.theme.IosBlue
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosCardMuted
import com.example.xinggui.ui.theme.IosGroupedBackground
import com.example.xinggui.ui.theme.IosTextPrimary
import com.example.xinggui.ui.theme.IosTextSecondary

private val ResourceBackground = IosGroupedBackground

@Composable
fun ResourcesHomePage(
    state: ResourcesUiState,
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onRecommendedClick: (ResourceCardUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // 根据选中的分类筛选资源
    val filteredItems = if (selectedCategory != null) {
        state.allItems.filter { it.category == selectedCategory }
    } else {
        state.allItems
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ResourceBackground)
    ) {
        // 搜索栏和分类筛选合并到一个卡片
        SearchAndFilterCard(
            keyword = searchKeyword,
            onKeywordChange = onSearchKeywordChange,
            onSearchClick = onSearchClick,
            categories = state.categories,
            selectedCategory = selectedCategory,
            onCategoryClick = { category ->
                selectedCategory = if (selectedCategory == category) null else category
            }
        )

        // 资源列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ResourceBackground),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filteredItems.size) { index ->
                ResourceListItem(
                    item = filteredItems[index],
                    unlocked = filteredItems[index].resourceId in state.unlockedResourceIds,
                    onClick = { onRecommendedClick(filteredItems[index]) }
                )
            }
        }
    }
}

@Composable
fun ResourcesSearchPage(
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    searchHistory: List<String>,
    onHistoryClick: (String) -> Unit,
    searchResults: List<ResourceCardUiModel>,
    unlockedResourceIds: Set<String>,
    onItemClick: (ResourceCardUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ResourceBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ResourceBackHeader(
                title = "资源搜索",
                onBack = onBack
            )
        }
        item {
            ResourceSearchShell(
                keyword = searchKeyword,
                onKeywordChange = onSearchKeywordChange,
                onSearchClick = onSearchClick
            )
        }
        if (searchHistory.isNotEmpty()) {
            item {
                HistoryCard(
                    items = searchHistory,
                    onItemClick = onHistoryClick
                )
            }
        }
        if (searchResults.isEmpty()) {
            item {
                ResourceEmptyCard("暂时没有匹配结果，试试更短的关键词或换一个分类入口。")
            }
        } else {
            items(searchResults) { item ->
                ResourceResultCard(
                    item = item,
                    unlocked = item.resourceId in unlockedResourceIds,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun ResourcesCategoryPage(
    currentCategory: String,
    categoryItems: List<ResourceCardUiModel>,
    unlockedResourceIds: Set<String>,
    onBack: () -> Unit,
    onItemClick: (ResourceCardUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ResourceBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ResourceBackHeader(
                title = currentCategory,
                onBack = onBack
            )
        }
        if (categoryItems.isEmpty()) {
            item {
                ResourceEmptyCard("这个分类下还没有内容。")
            }
        } else {
            items(categoryItems) { item ->
                ResourceResultCard(
                    item = item,
                    unlocked = item.resourceId in unlockedResourceIds,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun ResourcesDetailPage(
    item: ResourceCardUiModel?,
    @Suppress("UNUSED_PARAMETER") currentRole: UserRole,
    unlockedResourceIds: Set<String>,
    onBack: () -> Unit,
    onUnlockClick: () -> Unit,
    onOpenReaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (item == null) {
        EmptyView(message = "未找到资源内容", modifier = modifier.fillMaxSize())
        return
    }
    val locked = item.isPaid && item.resourceId !in unlockedResourceIds
    val canOpenPdf = !locked && !item.assetPath.isNullOrBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ResourceBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ResourceBackHeader(
                title = "资源详情",
                onBack = onBack
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IosCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResourceTag(text = item.category, container = IosCardMuted, content = MaterialTheme.colorScheme.primary)
                        if (item.isPaid) {
                            ResourceTag(
                                text = if (locked) "待解锁" else "已解锁",
                                container = Color(0xFFFFC857).copy(alpha = 0.28f),
                                content = Color(0xFF8C5C00)
                            )
                        }
                    }
                    Text(text = item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(text = item.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!item.sourceUrl.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = IosCardMuted.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = "来源：${item.sourceUrl}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF234C7A)
                            )
                        }
                    }
                    when {
                        locked -> {
                            Button(
                                onClick = onUnlockClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("解锁阅读")
                            }
                        }
                        canOpenPdf -> {
                            Button(
                                onClick = onOpenReaderClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("开始阅读")
                            }
                        }
                        else -> {
                            Text(
                                text = "暂无可阅读内容",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResourcePayDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("解锁付费资源") },
        text = { Text("确认解锁此资源？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ResourceHeaderCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.report_ref_989dded1c7eb6b35cfe52141929984ec),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 48.dp, height = 44.dp)
            )
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 32.sp,
                color = Color(0xFF111111)
            )
        }
    }
}

@Composable
private fun ResourceBackHeader(
    title: String,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResourceSearchShell(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("搜索政策、案例、教具或阅读材料") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Button(
                onClick = onSearchClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Text("搜索")
            }
        }
    }
}

@Composable
private fun FeaturedResourceCard(
    item: ResourceCardUiModel?,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = item != null, onClick = onClick)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "本周主推荐", color = Color.White.copy(alpha = 0.84f), style = MaterialTheme.typography.labelLarge)
            Text(
                text = item?.title ?: "等待资源数据同步",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = item?.summary ?: "后端启动后会在这里展示一张主推荐卡。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (item != null) {
                    ResourceTag(
                        text = item.category,
                        container = Color.White.copy(alpha = 0.22f),
                        content = Color.White
                    )
                    if (item.isPaid) {
                        ResourceTag(
                            text = if (unlocked) "已解锁" else "付费",
                            container = Color.White.copy(alpha = 0.22f),
                            content = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyDigestCard(
    items: List<ResourceCardUiModel>,
    unlockedResourceIds: Set<String>,
    onItemClick: (ResourceCardUiModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "政策 / 资讯快览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (items.isEmpty()) {
                ResourceEmptyCard("暂无政策类资源。")
            } else {
                items.forEach { item ->
                    ResourceResultCard(
                        item = item,
                        unlocked = item.resourceId in unlockedResourceIds,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySectionCard(
    categories: List<ResourceCategoryUiModel>,
    onCategoryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "分类入口", style = MaterialTheme.typography.titleLarge)
            categories.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { category ->
                        CategoryTile(
                            category = category,
                            modifier = Modifier.weight(1f),
                            onClick = { onCategoryClick(category.title) }
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    items: List<String>,
    onItemClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "搜索历史", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { history ->
                    FilterChip(
                        selected = false,
                        onClick = { onItemClick(history) },
                        label = { Text(history) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: ResourceCategoryUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCardMuted.copy(alpha = 0.52f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = category.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "${category.count} 条内容", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (category.isPaid) {
                ResourceTag(
                    text = "含付费内容",
                    container = Color.White,
                    content = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ResourceResultCard(
    item: ResourceCardUiModel,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = IosCard,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(IosCardMuted, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForCategory(item.category), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = item.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResourceTag(text = item.category, container = IosCardMuted, content = MaterialTheme.colorScheme.primary)
                    if (item.isPaid) {
                        ResourceTag(
                            text = if (unlocked) "已解锁" else "付费",
                            container = if (unlocked) Color(0xFFDDF6E8) else Color(0xFFFFECCE),
                            content = if (unlocked) Color(0xFF19724A) else Color(0xFF996400)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceTag(
    text: String,
    container: Color,
    content: Color
) {
    Box(
        modifier = Modifier
            .background(container, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = content)
    }
}

@Composable
private fun ResourceEmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun iconForCategory(category: String): ImageVector {
    return when (category) {
        ResourceCategoryNames.NEWS_POLICY -> Icons.Default.Newspaper
        ResourceCategoryNames.CASE_STUDY -> Icons.Default.AutoStories
        ResourceCategoryNames.POLICY_INTERPRETATION -> Icons.Default.Gavel
        ResourceCategoryNames.TEACHING_GUIDE -> Icons.Default.Toys
        else -> Icons.Default.Description
    }
}

// 新的知乎风格组件

@Composable
private fun SearchAndFilterCard(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    categories: List<ResourceCategoryUiModel>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 搜索栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("搜索资源", color = Color(0xFF999999)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF999999)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = IosCardMuted,
                        unfocusedContainerColor = IosCardMuted,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "搜索",
                    modifier = Modifier.clickable(onClick = onSearchClick),
                    color = IosBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 分隔线
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )

            // 分类筛选
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category.title
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryClick(category.title) },
                        label = { Text(category.title) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = IosCardMuted,
                            labelColor = Color(0xFF333333),
                            selectedContainerColor = IosBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceSearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索资源", color = Color(0xFF999999)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF999999)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = IosCardMuted,
                    unfocusedContainerColor = IosCardMuted,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索",
                modifier = Modifier.clickable(onClick = onSearchClick),
                color = IosBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<ResourceCategoryUiModel>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category.title
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryClick(category.title) },
                    label = { Text(category.title) },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        containerColor = IosCardMuted,
                        labelColor = Color(0xFF333333),
                        selectedContainerColor = IosBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun ResourceListItem(
    item: ResourceCardUiModel,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = item.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 摘要
            Text(
                text = item.summary,
                fontSize = 14.sp,
                color = IosTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 底部信息栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类标签
                Text(
                    text = item.category,
                    fontSize = 12.sp,
                    color = IosBlue,
                    modifier = Modifier
                        .background(
                            color = IosBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // 付费标签
                if (item.isPaid) {
                    Text(
                        text = if (unlocked) "已解锁" else "付费",
                        fontSize = 12.sp,
                        color = if (unlocked) Color(0xFF52C41A) else Color(0xFFFF9800),
                        modifier = Modifier
                            .background(
                                color = if (unlocked) Color(0xFF52C41A).copy(alpha = 0.1f) else Color(0xFFFF9800).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
