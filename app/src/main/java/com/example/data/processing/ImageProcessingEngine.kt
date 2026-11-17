package com.example.data.processing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import com.example.domain.ExportFormat
import com.example.domain.StudioToolId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class ProcessedBitmapResult(
    val bitmap: Bitmap,
    val estimatedBytes: Long,
    val summary: String
)

data class SavedImageResult(
    val filePath: String,
    val fileSizeBytes: Long,
    val width: Int,
    val height: Int
)

class ImageProcessingEngine(private val context: Context) {

    suspend fun loadBitmapFromUri(uri: Uri, maxDimension: Int = 1440): Bitmap? =
        withContext(Dispatchers.IO) {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val sampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, maxDimension)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
        }

    suspend fun loadSampleDrawable(@DrawableRes resId: Int, maxDimension: Int = 1280): Bitmap =
        withContext(Dispatchers.IO) {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, boundsOpts)
            val sampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, maxDimension)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, resId, decodeOpts)
        }

    suspend fun loadBitmapFromFile(path: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists()) return@withContext null
            BitmapFactory.decodeFile(file.absolutePath)
        }

    suspend fun saveBitmapToProjects(
        bitmap: Bitmap,
        fileNamePrefix: String,
        format: ExportFormat = ExportFormat.PNG,
        quality: Int = 92
    ): SavedImageResult = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "projects").apply { mkdirs() }
        val safePrefix = fileNamePrefix.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(24)
        val file = File(dir, "${safePrefix}_${System.currentTimeMillis()}.${format.extension}")
        FileOutputStream(file).use { out ->
            bitmap.compress(toCompressFormat(format), quality.coerceIn(10, 100), out)
        }
        SavedImageResult(
            filePath = file.absolutePath,
            fileSizeBytes = file.length(),
            width = bitmap.width,
            height = bitmap.height
        )
    }

    suspend fun exportToDeviceGallery(
        bitmap: Bitmap,
        displayName: String,
        format: ExportFormat = ExportFormat.PNG,
        quality: Int = 95
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanName = "${displayName.replace(Regex("[^a-zA-Z0-9_-]"), "_")}_${System.currentTimeMillis()}.${format.extension}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, cleanName)
                    put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AIPhotoStudio")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Unable to allocate gallery media URI")
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(toCompressFormat(format), quality.coerceIn(10, 100), out)
                } ?: error("Failed opening gallery output stream")
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                "Saved to Pictures/AIPhotoStudio/$cleanName"
            } else {
                val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val file = File(cacheDir, cleanName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(toCompressFormat(format), quality.coerceIn(10, 100), out)
                }
                "Prepared export at ${file.name}"
            }
        }
    }

    suspend fun createShareIntent(bitmap: Bitmap, format: ExportFormat = ExportFormat.PNG): Intent =
        withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(cacheDir, "studio_share_${System.currentTimeMillis()}.${format.extension}")
            FileOutputStream(file).use { out ->
                bitmap.compress(toCompressFormat(format), 95, out)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

    fun measureEncodedBytes(bitmap: Bitmap, format: ExportFormat, quality: Int = 90): Long {
        val baos = ByteArrayOutputStream()
        bitmap.compress(toCompressFormat(format), quality.coerceIn(10, 100), baos)
        return baos.size().toLong()
    }

    suspend fun processAdvancedTool(
        tool: StudioToolId,
        source: Bitmap,
        cropRatio: String = "1:1",
        resizeWidth: Int = source.width,
        resizeHeight: Int = source.height,
        compressQuality: Int = 75,
        filterPreset: String = "Golden Hour",
        effectName: String = "Vignette",
        effectIntensity: Float = 0.65f,
        overlayText: String = "AI PHOTO STUDIO",
        overlayTextColor: Int = Color.rgb(245, 215, 110),
        overlayTextSizeScale: Float = 1.0f,
        overlayPosition: String = "Bottom",
        stickerType: String = "Crown",
        borderWidthPx: Int = 36,
        borderColor: Int = Color.rgb(212, 175, 55),
        exportFormat: ExportFormat = ExportFormat.WEBP,
        watermarkText: String = "© AI Photo Studio",
        watermarkTiled: Boolean = true
    ): ProcessedBitmapResult = withContext(Dispatchers.Default) {
        when (tool) {
            StudioToolId.ADV_CROPPER -> {
                val out = cropToRatio(source, cropRatio)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Cropped to $cropRatio (${out.width}×${out.height}px)")
            }
            StudioToolId.ADV_RESIZER -> {
                val w = resizeWidth.coerceIn(64, 4096)
                val h = resizeHeight.coerceIn(64, 4096)
                val out = Bitmap.createScaledBitmap(source, w, h, true)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Resized to ${w}×${h}px")
            }
            StudioToolId.ADV_COMPRESSOR -> {
                val q = compressQuality.coerceIn(10, 100)
                val beforeBytes = measureEncodedBytes(source, ExportFormat.JPEG, 98)
                val baos = ByteArrayOutputStream()
                source.compress(Bitmap.CompressFormat.JPEG, q, baos)
                val raw = baos.toByteArray()
                val decoded = BitmapFactory.decodeByteArray(raw, 0, raw.size) ?: source.copy(Bitmap.Config.ARGB_8888, false)
                val savedPct = if (beforeBytes > 0) ((beforeBytes - raw.size) * 100f / beforeBytes).toInt().coerceAtLeast(0) else 0
                ProcessedBitmapResult(
                    decoded,
                    raw.size.toLong(),
                    "Compressed at $q% quality: ${beforeBytes / 1024}KB → ${raw.size / 1024}KB ($savedPct% smaller)"
                )
            }
            StudioToolId.ADV_FILTERS -> {
                val out = applyColorFilter(source, filterPreset)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Applied '$filterPreset' studio ColorMatrix filter")
            }
            StudioToolId.ADV_EFFECTS -> {
                val out = applyStudioEffect(source, effectName, effectIntensity)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Applied '$effectName' effect (${(effectIntensity * 100).toInt()}%)")
            }
            StudioToolId.ADV_TEXT -> {
                val out = drawTextOverlay(source, overlayText, overlayTextColor, overlayTextSizeScale, overlayPosition)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Added typography overlay ($overlayPosition)")
            }
            StudioToolId.ADV_STICKERS -> {
                val out = drawStickerOverlay(source, stickerType, overlayPosition)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Stamped '$stickerType' luxury studio sticker")
            }
            StudioToolId.ADV_BORDER -> {
                val out = addLuxuryBorder(source, borderWidthPx.coerceIn(4, 140), borderColor)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, "Framed with ${borderWidthPx}px border (${out.width}×${out.height}px)")
            }
            StudioToolId.ADV_CONVERTER -> {
                val baos = ByteArrayOutputStream()
                source.compress(toCompressFormat(exportFormat), 92, baos)
                val raw = baos.toByteArray()
                val decoded = BitmapFactory.decodeByteArray(raw, 0, raw.size) ?: source.copy(Bitmap.Config.ARGB_8888, false)
                ProcessedBitmapResult(decoded, raw.size.toLong(), "Converted to ${exportFormat.label} (${raw.size / 1024}KB)")
            }
            StudioToolId.ADV_WATERMARK -> {
                val out = drawWatermark(source, watermarkText.ifBlank { "© AI Photo Studio" }, watermarkTiled)
                val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
                ProcessedBitmapResult(out, bytes, if (watermarkTiled) "Applied tiled studio watermark" else "Applied corner watermark")
            }
            else -> {
                val bytes = measureEncodedBytes(source, ExportFormat.PNG, 95)
                ProcessedBitmapResult(source, bytes, "Ready")
            }
        }
    }

    suspend fun renderCreativeStudioTemplate(
        tool: StudioToolId,
        primarySource: Bitmap,
        collageExtras: List<Bitmap>,
        headline: String,
        subtitle: String,
        accentColor: Int = Color.rgb(212, 175, 55)
    ): ProcessedBitmapResult = withContext(Dispatchers.Default) {
        val out = when (tool) {
            StudioToolId.STUDIO_IG_DP -> renderInstagramDp(primarySource, accentColor)
            StudioToolId.STUDIO_YT_THUMB -> renderYouTubeThumbnail(primarySource, headline, subtitle, accentColor)
            StudioToolId.STUDIO_IG_POST -> renderInstagramPost(primarySource, headline, subtitle, accentColor)
            StudioToolId.STUDIO_FESTIVAL -> renderPosterTemplate(primarySource, headline, subtitle, accentColor, "FESTIVAL EDITION")
            StudioToolId.STUDIO_WEDDING -> renderWeddingTemplate(primarySource, headline, subtitle, accentColor)
            StudioToolId.STUDIO_BIRTHDAY -> renderPosterTemplate(primarySource, headline, subtitle, accentColor, "HAPPY BIRTHDAY")
            StudioToolId.STUDIO_LOGO -> renderLogoTemplate(headline, subtitle, accentColor)
            StudioToolId.STUDIO_COLLAGE -> renderCollage(listOf(primarySource) + collageExtras, accentColor)
            StudioToolId.STUDIO_FRAME -> renderGalleryFrame(primarySource, headline, accentColor)
            StudioToolId.STUDIO_PASSPORT -> renderPassportSheet(primarySource)
            else -> primarySource
        }
        val bytes = measureEncodedBytes(out, ExportFormat.PNG, 95)
        ProcessedBitmapResult(out, bytes, "Rendered ${tool.title} (${out.width}×${out.height}px)")
    }

    private fun cropToRatio(source: Bitmap, ratio: String): Bitmap {
        val (rw, rh) = when (ratio) {
            "1:1" -> 1f to 1f
            "4:5" -> 4f to 5f
            "16:9" -> 16f to 9f
            "3:4" -> 3f to 4f
            "9:16" -> 9f to 16f
            else -> return source.copy(Bitmap.Config.ARGB_8888, false)
        }
        val targetAspect = rw / rh
        val srcAspect = source.width.toFloat() / source.height.toFloat()
        val (cropW, cropH) = if (srcAspect > targetAspect) {
            val h = source.height
            (h * targetAspect).toInt().coerceAtLeast(1) to h
        } else {
            val w = source.width
            w to (w / targetAspect).toInt().coerceAtLeast(1)
        }
        val x = ((source.width - cropW) / 2).coerceAtLeast(0)
        val y = ((source.height - cropH) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(source, x, y, cropW, cropH)
    }

    private fun applyColorFilter(source: Bitmap, preset: String): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val cm = ColorMatrix()
        when (preset) {
            "Noir" -> cm.setSaturation(0f)
            "Golden Hour" -> {
                cm.set(
                    floatArrayOf(
                        1.18f, 0.05f, 0f, 0f, 16f,
                        0.03f, 1.08f, 0f, 0f, 8f,
                        0f, 0f, 0.82f, 0f, -8f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "Vintage" -> {
                cm.set(
                    floatArrayOf(
                        0.95f, 0.25f, 0.12f, 0f, 10f,
                        0.12f, 0.88f, 0.10f, 0f, 6f,
                        0.08f, 0.12f, 0.68f, 0f, 12f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "Vivid" -> cm.setSaturation(1.55f)
            "Cool Midnight" -> {
                cm.set(
                    floatArrayOf(
                        0.85f, 0f, 0.05f, 0f, -6f,
                        0f, 0.96f, 0.08f, 0f, 0f,
                        0.05f, 0.10f, 1.25f, 0f, 18f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "Sepia" -> {
                cm.set(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "Matte" -> {
                cm.set(
                    floatArrayOf(
                        0.90f, 0f, 0f, 0f, 22f,
                        0f, 0.90f, 0f, 0f, 20f,
                        0f, 0f, 0.88f, 0f, 18f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(cm) }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return out
    }

    private fun applyStudioEffect(source: Bitmap, effect: String, intensity: Float): Bitmap {
        val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val s = intensity.coerceIn(0.1f, 1f)
        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (effect) {
            "High Contrast" -> {
                val c = 1f + s * 0.9f
                val t = (-0.5f * c + 0.5f) * 255f
                val cm = ColorMatrix(
                    floatArrayOf(
                        c, 0f, 0f, 0f, t,
                        0f, c, 0f, 0f, t,
                        0f, 0f, c, 0f, t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                basePaint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, basePaint)
            }
            "Gold Duotone" -> {
                val cm = ColorMatrix().apply { setSaturation(0f) }
                basePaint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, basePaint)
                val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb((s * 115).toInt(), 212, 175, 55)
                }
                canvas.drawRect(0f, 0f, source.width.toFloat(), source.height.toFloat(), tintPaint)
            }
            "Warm Glow" -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1f + 0.25f * s, 0f, 0f, 0f, 18f * s,
                        0f, 1f + 0.14f * s, 0f, 0f, 10f * s,
                        0f, 0f, 1f - 0.12f * s, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                basePaint.colorFilter = ColorMatrixColorFilter(cm)
                canvas.drawBitmap(source, 0f, 0f, basePaint)
            }
            else -> { // Vignette
                canvas.drawBitmap(source, 0f, 0f, null)
                val cx = source.width / 2f
                val cy = source.height / 2f
                val radius = maxOf(source.width, source.height) * 0.72f
                val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        cx, cy, radius,
                        intArrayOf(Color.TRANSPARENT, Color.argb((s * 210).toInt(), 8, 8, 8)),
                        floatArrayOf(0.42f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, source.width.toFloat(), source.height.toFloat(), vignettePaint)
            }
        }
        return out
    }

    private fun drawTextOverlay(
        source: Bitmap,
        text: String,
        colorInt: Int,
        scale: Float,
        position: String
    ): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val size = (out.width * 0.068f * scale.coerceIn(0.6f, 2f)).coerceIn(24f, 160f)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 3f, Color.argb(200, 0, 0, 0))
        }
        val y = when (position) {
            "Top" -> out.height * 0.14f + size * 0.4f
            "Center" -> out.height * 0.52f
            else -> out.height * 0.88f
        }
        val content = text.ifBlank { "AI PHOTO STUDIO" }
        canvas.drawText(content, out.width / 2f, y, textPaint)
        return out
    }

    private fun drawStickerOverlay(source: Bitmap, sticker: String, position: String): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val badgeRadius = (minOf(out.width, out.height) * 0.12f).coerceIn(48f, 140f)
        val cx = when (position) {
            "Top" -> out.width - badgeRadius * 1.45f
            "Center" -> out.width / 2f
            else -> badgeRadius * 1.45f
        }
        val cy = when (position) {
            "Top" -> badgeRadius * 1.45f
            "Center" -> out.height / 2f
            else -> out.height - badgeRadius * 1.45f
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(225, 12, 12, 12) }
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.rgb(212, 175, 55)
            strokeWidth = 6f
        }
        canvas.drawCircle(cx, cy, badgeRadius, bgPaint)
        canvas.drawCircle(cx, cy, badgeRadius, ringPaint)

        val symbol = when (sticker) {
            "Crown" -> "♛"
            "Sparkle" -> "✦"
            "Star" -> "★"
            "Heart" -> "♥"
            "Verified" -> "✓"
            else -> "VIP"
        }
        val symPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 110)
            textSize = if (symbol == "VIP") badgeRadius * 0.56f else badgeRadius * 0.92f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(symbol, cx, cy + symPaint.textSize * 0.34f, symPaint)
        return out
    }

    private fun addLuxuryBorder(source: Bitmap, borderPx: Int, colorInt: Int): Bitmap {
        val w = source.width + borderPx * 2
        val h = source.height + borderPx * 2
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(colorInt)
        canvas.drawBitmap(source, borderPx.toFloat(), borderPx.toFloat(), null)
        return out
    }

    private fun drawWatermark(source: Bitmap, text: String, tiled: Boolean): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 245, 215, 110)
            textSize = (out.width * 0.042f).coerceIn(20f, 64f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        if (tiled) {
            canvas.save()
            canvas.rotate(-28f, out.width / 2f, out.height / 2f)
            val stepX = (out.width * 0.48f).coerceAtLeast(220f)
            val stepY = (out.height * 0.25f).coerceAtLeast(150f)
            var y = -out.height * 0.4f
            while (y < out.height * 1.4f) {
                var x = -out.width * 0.4f
                while (x < out.width * 1.4f) {
                    canvas.drawText(text, x, y, paint)
                    x += stepX
                }
                y += stepY
            }
            canvas.restore()
        } else {
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.argb(190, 245, 215, 110)
            canvas.drawText(text, out.width - 28f, out.height - 28f, paint)
        }
        return out
    }

    private fun renderInstagramDp(source: Bitmap, accentColor: Int): Bitmap {
        val size = 1080
        val sq = cropToRatio(source, "1:1")
        val scaled = Bitmap.createScaledBitmap(sq, size, size, true)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(8, 8, 8))

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val pad = 64f
        val rect = RectF(pad, pad, size - pad, size - pad)
        val layerId = canvas.saveLayer(rect, null)
        canvas.drawOval(rect, circlePaint)
        circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, null, rect, circlePaint)
        canvas.restoreToCount(layerId)

        val outerRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 22f
            color = accentColor
        }
        val innerRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.rgb(245, 215, 110)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 44f, outerRing)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 66f, innerRing)
        return out
    }

    private fun renderYouTubeThumbnail(
        source: Bitmap,
        headline: String,
        subtitle: String,
        accentColor: Int
    ): Bitmap {
        val w = 1280
        val h = 720
        val cropped = cropToRatio(source, "16:9")
        val scaled = Bitmap.createScaledBitmap(cropped, w, h, true)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawBitmap(scaled, 0f, 0f, null)

        val scrim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w * 0.72f, 0f,
                intArrayOf(Color.argb(230, 8, 8, 8), Color.argb(40, 8, 8, 8)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrim)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(RectF(64f, 64f, 340f, 126f), 14f, 14f, badgePaint)
        val badgeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(8, 8, 8)
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("STUDIO EDITION", 88f, 105f, badgeText)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(12f, 0f, 4f, Color.BLACK)
        }
        val titleText = headline.ifBlank { "LUXURY EDIT" }.take(26)
        canvas.drawText(titleText, 64f, h * 0.56f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 110)
            textSize = 38f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(subtitle.ifBlank { "Created in AI Photo Studio" }.take(38), 64f, h * 0.67f, subPaint)
        return out
    }

    private fun renderInstagramPost(
        source: Bitmap,
        headline: String,
        subtitle: String,
        accentColor: Int
    ): Bitmap {
        val size = 1080
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(12, 12, 12))

        val sq = cropToRatio(source, "1:1")
        val photoRect = RectF(56f, 56f, size - 56f, size - 210f)
        canvas.drawBitmap(sq, null, photoRect, null)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = accentColor
            strokeWidth = 5f
        }
        canvas.drawRect(photoRect, border)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(headline.ifBlank { "LUXURY PORTRAIT" }.take(28), size / 2f, size - 122f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 110)
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle.ifBlank { "AI PHOTO STUDIO" }.take(40), size / 2f, size - 72f, subPaint)
        return out
    }

    private fun renderPosterTemplate(
        source: Bitmap,
        headline: String,
        subtitle: String,
        accentColor: Int,
        ribbonLabel: String
    ): Bitmap {
        val w = 1080
        val h = 1350
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(8, 8, 8))

        val cropped = cropToRatio(source, "4:5")
        canvas.drawBitmap(cropped, null, RectF(44f, 44f, w - 44f, h - 44f), null)

        val bottomGrad = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.45f, 0f, h.toFloat(),
                intArrayOf(Color.TRANSPARENT, Color.argb(242, 8, 8, 8)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(44f, h * 0.45f, w - 44f, h - 44f, bottomGrad)

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f
            color = accentColor
        }
        canvas.drawRect(34f, 34f, w - 34f, h - 34f, border)

        val ribbonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        canvas.drawRoundRect(RectF(w / 2f - 220f, 88f, w / 2f + 220f, 152f), 32f, 32f, ribbonPaint)
        val ribbonText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(8, 8, 8)
            textSize = 30f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(ribbonLabel, w / 2f, 130f, ribbonText)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 68f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(headline.ifBlank { "GOLDEN CELEBRATION" }.take(24), w / 2f, h - 190f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 110)
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle.ifBlank { "Warm wishes & timeless memories" }.take(38), w / 2f, h - 115f, subPaint)
        return out
    }

    private fun renderWeddingTemplate(
        source: Bitmap,
        headline: String,
        subtitle: String,
        accentColor: Int
    ): Bitmap {
        val w = 1080
        val h = 1350
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(248, 244, 236)) // Luxury warm ivory mat

        val cropped = cropToRatio(source, "4:5")
        val photoRect = RectF(90f, 90f, w - 90f, h - 280f)
        canvas.drawBitmap(cropped, null, photoRect, null)

        val goldFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = accentColor
        }
        canvas.drawRect(photoRect, goldFrame)
        canvas.drawRect(36f, 36f, w - 36f, h - 36f, goldFrame)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(26, 22, 16)
            textSize = 54f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(headline.ifBlank { "Aarav & Meera" }.take(26), w / 2f, h - 165f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(158, 123, 30)
            textSize = 30f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle.ifBlank { "Forever & Always • 2026" }.take(36), w / 2f, h - 102f, subPaint)
        return out
    }

    private fun renderLogoTemplate(brandName: String, tagline: String, accentColor: Int): Bitmap {
        val size = 1024
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(8, 8, 8))

        val cx = size / 2f
        val cy = size * 0.42f
        val crestPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 14f
            color = accentColor
        }
        canvas.drawCircle(cx, cy, 210f, crestPaint)

        val diamond = Path().apply {
            moveTo(cx, cy - 170f)
            lineTo(cx + 170f, cy)
            lineTo(cx, cy + 170f)
            lineTo(cx - 170f, cy)
            close()
        }
        val innerCrest = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.rgb(245, 215, 110)
        }
        canvas.drawPath(diamond, innerCrest)

        val cleanName = brandName.ifBlank { "AURA LUXE" }
        val initials = cleanName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "AL" }
        val initPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 140f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(initials, cx, cy + 50f, initPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 62f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.14f
        }
        canvas.drawText(cleanName.uppercase().take(18), cx, size * 0.77f, namePaint)

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(185, 185, 185)
            textSize = 28f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
        }
        canvas.drawText(tagline.ifBlank { "CREATIVE STUDIO" }.uppercase().take(28), cx, size * 0.84f, tagPaint)
        return out
    }

    private fun renderCollage(sources: List<Bitmap>, accentColor: Int): Bitmap {
        val size = 1080
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(10, 10, 10))

        val imgs = if (sources.size >= 4) sources.take(4) else (0 until 4).map { sources[it % sources.size] }
        val gap = 20f
        val half = (size - gap * 3) / 2f
        val rects = listOf(
            RectF(gap, gap, gap + half, gap + half),
            RectF(gap * 2 + half, gap, size - gap, gap + half),
            RectF(gap, gap * 2 + half, gap + half, size - gap),
            RectF(gap * 2 + half, gap * 2 + half, size - gap, size - gap)
        )
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = accentColor
        }
        imgs.forEachIndexed { idx, bmp ->
            val sq = cropToRatio(bmp, "1:1")
            canvas.drawBitmap(sq, null, rects[idx], null)
            canvas.drawRect(rects[idx], border)
        }
        return out
    }

    private fun renderGalleryFrame(source: Bitmap, caption: String, accentColor: Int): Bitmap {
        val w = source.width + 240
        val h = source.height + 300
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.rgb(20, 20, 20))

        val outerFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 36f
            color = accentColor
        }
        canvas.drawRect(18f, 18f, w - 18f, h - 18f, outerFrame)

        val matRect = RectF(64f, 64f, w - 64f, h - 64f)
        val matPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(245, 242, 235) }
        canvas.drawRect(matRect, matPaint)

        canvas.drawBitmap(source, 120f, 110f, null)

        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(32, 28, 22)
            textSize = (w * 0.032f).coerceIn(22f, 44f)
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(caption.ifBlank { "Studio Gallery Archive" }.take(34), w / 2f, h - 98f, capPaint)
        return out
    }

    private fun renderPassportSheet(source: Bitmap): Bitmap {
        // 6-up 35x45mm studio print sheet (1200x1800px 4x6 inch paper)
        val sheetW = 1200
        val sheetH = 1600
        val out = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)

        val cropped = cropToRatio(source, "3:4")
        val photoW = 340f
        val photoH = 440f
        val cols = 3
        val rows = 2
        val gapX = 40f
        val gapY = 60f
        val startX = (sheetW - (cols * photoW + (cols - 1) * gapX)) / 2f
        val startY = (sheetH - (rows * photoH + (rows - 1) * gapY)) / 2f

        val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.LTGRAY
        }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = startX + c * (photoW + gapX)
                val top = startY + r * (photoH + gapY)
                val rect = RectF(left, top, left + photoW, top + photoH)
                canvas.drawBitmap(cropped, null, rect, null)
                canvas.drawRect(rect, guidePaint)
            }
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("AI PHOTO STUDIO • 35×45mm PASSPORT PRINT SHEET (6 COPIES)", sheetW / 2f, sheetH - 72f, footerPaint)
        return out
    }

    private fun toCompressFormat(format: ExportFormat): Bitmap.CompressFormat = when (format) {
        ExportFormat.PNG -> Bitmap.CompressFormat.PNG
        ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
    }

    private fun calculateInSampleSize(rawW: Int, rawH: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (rawH > maxDim || rawW > maxDim) {
            var halfH = rawH / 2
            var halfW = rawW / 2
            while ((halfH / inSampleSize) >= maxDim && (halfW / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
