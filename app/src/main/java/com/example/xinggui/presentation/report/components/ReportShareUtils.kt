package com.example.xinggui.presentation.report.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.xinggui.presentation.report.ReportUiState
import com.example.xinggui.ui.theme.StarSurface
import com.example.xinggui.ui.theme.StarTextPrimary
import com.example.xinggui.ui.theme.StarTextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

internal fun shareReport(
    context: Context,
    state: ReportUiState,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val shareText = buildReportShareText(state)
    try {
        val radarBitmap = renderRadarChartBitmap(context, state.dimensions, 360)
        val summaryBitmap = renderReportSummaryBitmap(context, state, 360)
        val radarUri = saveBitmapToCacheAndGetUri(context, radarBitmap, "star_report_radar_${state.childName}.png")
        val summaryUri = saveBitmapToCacheAndGetUri(context, summaryBitmap, "star_report_summary_${state.childName}.png")

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_SUBJECT, "星报告 - ${state.childName}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(radarUri, summaryUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享星报告"))
        scope.launch { snackbarHostState.showSnackbar("已打开系统分享面板") }
    } catch (_: ActivityNotFoundException) {
        scope.launch { snackbarHostState.showSnackbar("当前设备没有可用的分享应用") }
    } catch (_: Exception) {
        scope.launch { snackbarHostState.showSnackbar("分享失败，请稍后重试") }
    }
}

private fun buildReportShareText(state: ReportUiState): String {
    return buildString {
        appendLine("星报告")
        appendLine("儿童：${state.childName}")
        appendLine("年龄：${state.age}岁")
        appendLine("干预时长：${state.interventionDuration}")
        appendLine()
        appendLine("报告概览")
        appendLine(state.overview)
        appendLine()
        appendLine("维度得分")
        state.dimensions.forEach { appendLine("${it.title}：${it.score}") }
        appendLine()
        appendLine("智能分析")
        appendLine(state.aiAnalysis)
        appendLine()
        appendLine("综合评估")
        appendLine(state.overallEvaluation)
        appendLine()
        appendLine("后续建议")
        appendLine(state.nextSuggestions)
        if (state.highlights.isNotEmpty()) {
            appendLine()
            appendLine("重点观察")
            state.highlights.forEach { appendLine("- $it") }
        }
    }
}

private fun renderReportSummaryBitmap(
    context: Context,
    state: ReportUiState,
    widthDp: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    val widthPx = (widthDp * density).toInt().coerceAtLeast(1)
    val paddingPx = (16 * density).toInt()

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = StarTextPrimary.toArgb()
        textSize = 18f * scaledDensity
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = StarTextSecondary.toArgb()
        textSize = 13f * scaledDensity
    }
    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = StarTextPrimary.toArgb()
        textSize = 14f * scaledDensity
    }

    val content = buildString {
        appendLine("儿童：${state.childName} | 年龄：${state.age}岁 | 干预时长：${state.interventionDuration}")
        appendLine()
        appendLine("报告概览")
        appendLine(state.overview)
        appendLine()
        appendLine("智能分析")
        appendLine(state.aiAnalysis)
        appendLine()
        appendLine("综合评估")
        appendLine(state.overallEvaluation)
        appendLine()
        appendLine("后续建议")
        appendLine(state.nextSuggestions)
        if (state.highlights.isNotEmpty()) {
            appendLine()
            appendLine("重点观察")
            state.highlights.forEach { appendLine("- $it") }
        }
    }

    val layoutWidth = (widthPx - paddingPx * 2).coerceAtLeast(1)
    val staticLayout = StaticLayout.Builder
        .obtain(content, 0, content.length, bodyPaint, layoutWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, 1.15f)
        .setIncludePad(false)
        .build()

    val titleHeight = (titlePaint.textSize * 1.2f).toInt()
    val subtitleHeight = (subtitlePaint.textSize * 1.2f).toInt()
    val totalHeight = paddingPx + titleHeight + subtitleHeight + (12 * density).toInt() + staticLayout.height + paddingPx

    val bitmap = Bitmap.createBitmap(widthPx, totalHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(StarSurface.toArgb())
    val top = paddingPx.toFloat()
    canvas.drawText("报告摘要", paddingPx.toFloat(), top + titlePaint.textSize, titlePaint)
    canvas.drawText("（用于一键反馈分享）", paddingPx.toFloat(), top + titleHeight + subtitlePaint.textSize, subtitlePaint)

    val contentTop = (paddingPx + titleHeight + subtitleHeight + (12 * density).toInt()).toFloat()
    canvas.save()
    canvas.translate(paddingPx.toFloat(), contentTop)
    staticLayout.draw(canvas)
    canvas.restore()
    return bitmap
}

private fun saveBitmapToCacheAndGetUri(
    context: Context,
    bitmap: Bitmap,
    fileName: String
): Uri {
    val imagesDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val safeName = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    val outFile = File(imagesDir, safeName)
    FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
}
