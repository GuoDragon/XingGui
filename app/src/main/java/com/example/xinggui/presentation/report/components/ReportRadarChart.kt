package com.example.xinggui.presentation.report.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xinggui.presentation.report.DimensionScoreUiModel
import com.example.xinggui.ui.theme.StarBlue
import com.example.xinggui.ui.theme.StarGold
import com.example.xinggui.ui.theme.StarSurface
import com.example.xinggui.ui.theme.StarTextPrimary
import com.example.xinggui.ui.theme.StarTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChart(
    dimensions: List<DimensionScoreUiModel>,
    modifier: Modifier = Modifier
) {
    val safeDimensions = if (dimensions.isEmpty()) listOf(DimensionScoreUiModel("维度", 0)) else dimensions
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = minOf(size.width, size.height) * 0.32f
        val outlineColor = StarTextSecondary.copy(alpha = 0.35f)
        val labelTitleColor = StarTextPrimary.toArgb()
        val labelScoreColor = StarTextSecondary.toArgb()

        val outerPath = Path().apply {
            safeDimensions.forEachIndexed { index, _ ->
                val angle = axisAngle(index, safeDimensions.size)
                val point = radialPoint(center, outerRadius, angle)
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(path = outerPath, color = outlineColor, style = Stroke(width = 1.1.dp.toPx()))

        val points = safeDimensions.mapIndexed { index, item ->
            val angle = axisAngle(index, safeDimensions.size)
            val radius = outerRadius * (item.score.coerceIn(0, 100) / 100f)
            radialPoint(center, radius, angle)
        }
        val fillPath = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(path = fillPath, color = StarBlue.copy(alpha = 0.18f))
        drawPath(path = fillPath, color = StarBlue.copy(alpha = 0.95f), style = Stroke(width = 2.4.dp.toPx()))

        points.forEach { point ->
            drawCircle(color = StarGold, radius = 5.2.dp.toPx(), center = point)
            drawCircle(color = StarSurface, radius = 2.5.dp.toPx(), center = point)
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 13.sp.toPx()
            color = labelTitleColor
            isFakeBoldText = true
        }
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 11.sp.toPx()
            color = labelScoreColor
        }
        safeDimensions.forEachIndexed { index, item ->
            val angle = axisAngle(index, safeDimensions.size)
            val titlePoint = radialPoint(center, outerRadius + 25.dp.toPx(), angle)
            val scorePoint = radialPoint(center, outerRadius + 11.dp.toPx(), angle)
            drawContext.canvas.nativeCanvas.drawText(item.title, titlePoint.x, titlePoint.y, titlePaint)
            drawContext.canvas.nativeCanvas.drawText(item.score.toString(), scorePoint.x, scorePoint.y, scorePaint)
        }
    }
}

internal fun renderRadarChartBitmap(
    context: Context,
    dimensions: List<DimensionScoreUiModel>,
    sizeDp: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(StarSurface.toArgb())

    val center = Offset(sizePx / 2f, sizePx / 2f)
    val outerRadius = (sizePx / 2f) * 0.72f
    val safeDimensions = if (dimensions.isEmpty()) listOf(DimensionScoreUiModel("维度", 0)) else dimensions

    val outerOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.1f * density
        color = StarTextSecondary.copy(alpha = 0.35f).toArgb()
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = StarBlue.copy(alpha = 0.18f).toArgb()
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.4f * density
        color = StarBlue.copy(alpha = 0.95f).toArgb()
    }
    val pointOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = StarGold.toArgb()
    }
    val pointInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = StarSurface.toArgb()
    }
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 13f * scaledDensity
        color = StarTextPrimary.toArgb()
        isFakeBoldText = true
    }
    val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 11f * scaledDensity
        color = StarTextSecondary.toArgb()
    }

    val outerPath = android.graphics.Path().apply {
        safeDimensions.forEachIndexed { index, _ ->
            val angle = axisAngle(index, safeDimensions.size)
            val point = radialPoint(center, outerRadius, angle)
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
        close()
    }
    canvas.drawPath(outerPath, outerOutlinePaint)

    val points = safeDimensions.mapIndexed { index, item ->
        val angle = axisAngle(index, safeDimensions.size)
        val radius = outerRadius * (item.score.coerceIn(0, 100) / 100f)
        radialPoint(center, radius, angle)
    }
    val fillPath = android.graphics.Path().apply {
        points.forEachIndexed { index, point ->
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
        close()
    }
    canvas.drawPath(fillPath, fillPaint)
    canvas.drawPath(fillPath, strokePaint)

    points.forEach { point ->
        canvas.drawCircle(point.x, point.y, 5.2f * density, pointOuterPaint)
        canvas.drawCircle(point.x, point.y, 2.5f * density, pointInnerPaint)
    }

    safeDimensions.forEachIndexed { index, item ->
        val angle = axisAngle(index, safeDimensions.size)
        val titlePoint = radialPoint(center, outerRadius + 25f * density, angle)
        val scorePoint = radialPoint(center, outerRadius + 11f * density, angle)
        canvas.drawText(item.title, titlePoint.x, titlePoint.y, titlePaint)
        canvas.drawText(item.score.toString(), scorePoint.x, scorePoint.y, scorePaint)
    }

    return bitmap
}

private fun axisAngle(index: Int, total: Int): Float {
    val startAngle = (-90f).toRadians()
    val step = (360f / total).toRadians()
    return startAngle + step * index
}

private fun radialPoint(center: Offset, radius: Float, angleRadians: Float): Offset {
    return Offset(
        x = center.x + radius * cos(angleRadians),
        y = center.y + radius * sin(angleRadians)
    )
}

private fun Float.toRadians(): Float = (this / 180f * Math.PI).toFloat()
