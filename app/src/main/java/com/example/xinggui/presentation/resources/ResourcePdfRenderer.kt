package com.example.xinggui.presentation.resources

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

internal interface ResourcePdfRenderer {
    suspend fun open(assetPath: String): Result<Int>
    suspend fun renderPage(pageIndex: Int, targetWidthPx: Int): Result<ImageBitmap>
    fun close()
}

internal class AndroidResourcePdfRenderer(context: Context) : ResourcePdfRenderer {
    private val appContext = context.applicationContext
    private val lock = Any()
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private val bitmapCache = object : LruCache<String, Bitmap>(BITMAP_CACHE_SIZE_KB) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    override suspend fun open(assetPath: String): Result<Int> = withContext(Dispatchers.IO) {
        if (assetPath.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Resource path is empty"))
        }

        synchronized(lock) {
            runCatching {
                closeInternal()
                val cachedFile = copyAssetToCache(assetPath)
                val descriptor = ParcelFileDescriptor.open(cachedFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(descriptor)
                if (renderer.pageCount <= 0) {
                    renderer.close()
                    descriptor.close()
                    throw IllegalStateException("Document has no pages")
                }
                fileDescriptor = descriptor
                pdfRenderer = renderer
                renderer.pageCount
            }
        }
    }

    override suspend fun renderPage(pageIndex: Int, targetWidthPx: Int): Result<ImageBitmap> =
        withContext(Dispatchers.IO) {
            synchronized(lock) {
                runCatching {
                    val renderer = pdfRenderer ?: throw IllegalStateException("Renderer is not initialized")
                    if (pageIndex !in 0 until renderer.pageCount) {
                        throw IndexOutOfBoundsException("Page index is out of range")
                    }

                    val widthPx = targetWidthPx.coerceAtLeast(1)
                    val cacheKey = "$pageIndex:$widthPx"
                    bitmapCache.get(cacheKey)?.let { cached ->
                        return@runCatching cached.asImageBitmap()
                    }

                    renderer.openPage(pageIndex).use { page ->
                        val scale = (widthPx / page.width.toFloat())
                            .takeIf { it.isFinite() && it > 0f } ?: 1f
                        val heightPx = max(1, (page.height * scale).roundToInt())
                        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                        Canvas(bitmap).drawColor(Color.WHITE)
                        val matrix = Matrix().apply { postScale(scale, scale) }
                        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmapCache.put(cacheKey, bitmap)
                        bitmap.asImageBitmap()
                    }
                }
            }
        }

    override fun close() {
        synchronized(lock) { closeInternal() }
    }

    private fun closeInternal() {
        bitmapCache.evictAll()
        runCatching { pdfRenderer?.close() }
        pdfRenderer = null
        runCatching { fileDescriptor?.close() }
        fileDescriptor = null
    }

    private fun copyAssetToCache(assetPath: String): File {
        val cacheDir = File(appContext.cacheDir, CACHE_DIRECTORY).apply { mkdirs() }
        val baseName = assetPath.substringAfterLast('/')
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .ifBlank { "resource.pdf" }
        val hashedPrefix = assetPath.hashCode().toUInt().toString(16)
        val outputFile = File(cacheDir, "${hashedPrefix}_$baseName")
        val tempPrefix = "${hashedPrefix}_".padEnd(3, '_')

        // Refresh on every open so an app update cannot keep showing an older cached PDF.
        val tempFile = File.createTempFile(tempPrefix, ".tmp", cacheDir)
        try {
            appContext.assets.open(assetPath).use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (outputFile.exists()) {
                outputFile.delete()
            }
            if (!tempFile.renameTo(outputFile)) {
                tempFile.inputStream().use { input ->
                    FileOutputStream(outputFile, false).use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile.delete()
            }
        } catch (throwable: Throwable) {
            tempFile.delete()
            throw throwable
        }
        return outputFile
    }

    private companion object {
        const val CACHE_DIRECTORY = "resource_pdf_reader"
        const val BITMAP_CACHE_SIZE_KB = 24 * 1024
    }
}
