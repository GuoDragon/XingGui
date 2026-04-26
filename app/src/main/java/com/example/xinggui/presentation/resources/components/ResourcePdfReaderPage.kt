package com.example.xinggui.presentation.resources.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.xinggui.presentation.resources.AndroidResourcePdfRenderer
import com.example.xinggui.presentation.resources.ResourcePdfRenderer
import com.example.xinggui.presentation.resources.ResourceReaderSession
import com.example.xinggui.ui.theme.IosBlue
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosGroupedBackground

private sealed interface ReaderDocumentState {
    data object Loading : ReaderDocumentState
    data class Ready(val pageCount: Int) : ReaderDocumentState
    data class Error(val message: String) : ReaderDocumentState
}

private sealed interface ReaderPageState {
    data object Loading : ReaderPageState
    data class Ready(val image: ImageBitmap) : ReaderPageState
    data class Error(val message: String) : ReaderPageState
}

@Composable
fun ResourcePdfReaderPage(
    session: ResourceReaderSession?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (session == null) {
        ReaderErrorLayout(
            title = "阅读器不可用",
            message = "当前阅读会话已失效，请从资源详情页重新打开。",
            onBack = onBack,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val renderer = remember(session.assetPath) { AndroidResourcePdfRenderer(context) }
    var documentState by remember(session.assetPath) { mutableStateOf<ReaderDocumentState>(ReaderDocumentState.Loading) }
    val listState = rememberLazyListState()
    val currentPage by remember {
        derivedStateOf {
            val readyState = documentState as? ReaderDocumentState.Ready ?: return@derivedStateOf 0
            (listState.firstVisibleItemIndex + 1).coerceIn(1, readyState.pageCount)
        }
    }

    LaunchedEffect(renderer, session.assetPath) {
        documentState = ReaderDocumentState.Loading
        documentState = renderer.open(session.assetPath).fold(
            onSuccess = { ReaderDocumentState.Ready(it) },
            onFailure = { throwable ->
                ReaderDocumentState.Error(throwable.message ?: "打开文件失败。")
            }
        )
    }
    DisposableEffect(renderer) {
        onDispose { renderer.close() }
    }

    Scaffold(
        topBar = {
            CompactReaderTopBar(title = session.title, onBack = onBack)
        },
        containerColor = IosGroupedBackground,
        modifier = modifier
    ) { contentPadding ->
        when (val state = documentState) {
            ReaderDocumentState.Loading -> ReaderLoadingLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            )

            is ReaderDocumentState.Error -> ReaderErrorLayout(
                title = "无法打开文件",
                message = state.message,
                onBack = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            )

            is ReaderDocumentState.Ready -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Text(
                    text = "第 $currentPage / ${state.pageCount} 页",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    items(count = state.pageCount, key = { index -> "${session.resourceId}_$index" }) { pageIndex ->
                        PdfPageItem(
                            pageIndex = pageIndex,
                            renderer = renderer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactReaderTopBar(
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IosCard)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
        }
    }
}

@Composable
private fun PdfPageItem(
    pageIndex: Int,
    renderer: ResourcePdfRenderer,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "第 ${pageIndex + 1} 页",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val targetWidthPx = constraints.maxWidth.coerceAtLeast(1)
                val pageState by produceState<ReaderPageState>(
                    initialValue = ReaderPageState.Loading,
                    key1 = pageIndex,
                    key2 = targetWidthPx,
                    key3 = renderer
                ) {
                    value = ReaderPageState.Loading
                    value = renderer.renderPage(pageIndex, targetWidthPx).fold(
                        onSuccess = { ReaderPageState.Ready(it) },
                        onFailure = { throwable ->
                            ReaderPageState.Error(throwable.message ?: "渲染失败。")
                        }
                    )
                }

                when (val state = pageState) {
                    ReaderPageState.Loading -> ReaderPageLoading()
                    is ReaderPageState.Error -> ReaderPageError(state.message)
                    is ReaderPageState.Ready -> {
                        val aspectRatio = remember(state.image) {
                            val width = state.image.width.coerceAtLeast(1)
                            val height = state.image.height.coerceAtLeast(1)
                            width.toFloat() / height.toFloat()
                        }
                        Image(
                            bitmap = state.image,
                            contentDescription = "第 ${pageIndex + 1} 页",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderLoadingLayout(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = IosBlue)
            Text(
                text = "正在加载文件...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun ReaderErrorLayout(
    title: String,
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onBack) {
                Text("返回资源页")
            }
        }
    }
}

@Composable
private fun ReaderPageLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = IosBlue)
    }
}

@Composable
private fun ReaderPageError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}
