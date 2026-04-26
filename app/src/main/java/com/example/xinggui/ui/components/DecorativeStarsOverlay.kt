package com.example.xinggui.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

const val STAR_DECOR_COUNT = 8

private data class StarDecorStyle(
    val color: Color,
    val minSizeFactor: Float,
    val maxSizeFactor: Float,
    val minAlpha: Float,
    val maxAlpha: Float,
    val insetFraction: Float
)

private val STAR_DECOR_STYLE = StarDecorStyle(
    color = Color(0xFFA6C5F0),
    minSizeFactor = 0.003f,
    maxSizeFactor = 0.009f,
    minAlpha = 0.12f,
    maxAlpha = 0.28f,
    insetFraction = 0.04f
)

@Composable
fun DecorativeStarsOverlay(
    modifier: Modifier = Modifier,
    pageKey: String
) {
    // Each page entry gets a fresh random layout; recomposition keeps it stable.
    val sessionSeed = remember(pageKey) { "$pageKey:${Random.nextInt()}" }
    val stars = remember(sessionSeed) {
        buildDecorativeStars(
            seedKey = sessionSeed,
            starCount = STAR_DECOR_COUNT,
            style = STAR_DECOR_STYLE
        )
    }

    Canvas(modifier = modifier) {
        stars.forEach { star ->
            val center = Offset(
                x = size.width * star.xFraction,
                y = size.height * star.yFraction
            )
            val outerRadius = minOf(size.width, size.height) * star.sizeFactor
            if (outerRadius <= 0f) return@forEach

            drawPath(
                path = buildStarPath(
                    center = center,
                    outerRadius = outerRadius,
                    innerRadius = outerRadius * 0.45f,
                    rotationRadians = star.rotationRadians
                ),
                color = STAR_DECOR_STYLE.color.copy(alpha = star.alpha)
            )
        }
    }
}

private data class DecorativeStarSpec(
    val xFraction: Float,
    val yFraction: Float,
    val sizeFactor: Float,
    val alpha: Float,
    val rotationRadians: Float
)

private fun buildDecorativeStars(
    seedKey: String,
    starCount: Int,
    style: StarDecorStyle
): List<DecorativeStarSpec> {
    val random = Random(seedKey.hashCode())
    val clampedCount = starCount.coerceAtLeast(0)
    val clampedInset = style.insetFraction.coerceIn(0f, 0.49f)
    val rangeScale = 1f - clampedInset * 2f
    val sizeRange = (style.maxSizeFactor - style.minSizeFactor).coerceAtLeast(0f)
    val alphaRange = (style.maxAlpha - style.minAlpha).coerceAtLeast(0f)
    return List(clampedCount) {
        DecorativeStarSpec(
            xFraction = random.nextFloat() * rangeScale + clampedInset,
            yFraction = random.nextFloat() * rangeScale + clampedInset,
            sizeFactor = style.minSizeFactor + random.nextFloat() * sizeRange,
            alpha = style.minAlpha + random.nextFloat() * alphaRange,
            rotationRadians = random.nextFloat() * (PI * 2f).toFloat()
        )
    }
}

private fun buildStarPath(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    rotationRadians: Float
): Path {
    val path = Path()
    val step = (PI / 5.0).toFloat()
    for (index in 0 until 10) {
        val radius = if (index % 2 == 0) outerRadius else innerRadius
        val angle = rotationRadians + step * index
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}
