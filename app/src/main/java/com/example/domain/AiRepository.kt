package com.example.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Single interface routing all cloud AI photo editing and image generation calls.
 * Because Firebase setup was declined by the user, this is backed by [PlaceholderAiRepository]
 * which returns clearly labeled sample previews without calling Gemini or embedding API keys.
 */
interface AiRepository {
    val isLiveGeminiConnected: Boolean
    val statusNotice: String

    suspend fun processAiPhotoEdit(request: AiEditRequest): AiProcessingResult
    suspend fun generateAiImages(request: AiGenRequest): AiGenerationResult
}

data class AiEditRequest(
    val tool: StudioToolId,
    val sourceBitmap: Bitmap,
    val intensity: Float,
    val promptNote: String = ""
)

data class AiGenRequest(
    val tool: StudioToolId,
    val prompt: String,
    val negativePrompt: String,
    val style: String,
    val aspectRatio: String,
    val quality: String,
    val outputCount: Int
)

data class AiProcessingResult(
    val previewBitmap: Bitmap,
    val summary: String,
    val isSamplePreview: Boolean,
    val notice: String
)

data class AiGenerationResult(
    val bitmaps: List<Bitmap>,
    val summary: String,
    val isSamplePreview: Boolean,
    val notice: String
)

class PlaceholderAiRepository : AiRepository {
    override val isLiveGeminiConnected: Boolean = false

    override val statusNotice: String =
        "Firebase AI Logic Required: Firebase setup was declined, so AI tools currently return " +
            "clearly labeled sample previews. Enable Firebase to connect to Gemini."

    override suspend fun processAiPhotoEdit(request: AiEditRequest): AiProcessingResult =
        withContext(Dispatchers.Default) {
            delay(550)
            val src = request.sourceBitmap
            val width = src.width.coerceAtLeast(320)
            val height = src.height.coerceAtLeast(320)
            val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val cm = ColorMatrix()
            val strength = request.intensity.coerceIn(0.1f, 1f)
            when (request.tool) {
                StudioToolId.AI_ENHANCER, StudioToolId.AI_COLOR_CORRECTION -> {
                    cm.setSaturation(1f + 0.25f * strength)
                }
                StudioToolId.AI_RETOUCH, StudioToolId.AI_FACE_ENHANCER -> {
                    val scale = 1f + 0.08f * strength
                    cm.setScale(scale, scale * 0.99f, scale * 0.96f, 1f)
                }
                StudioToolId.AI_RESTORATION -> {
                    cm.setSaturation(1f + 0.15f * strength)
                }
                else -> Unit
            }
            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(src, null, Rect(0, 0, width, height), paint)

            // Draw clear non-deceptive sample preview banner onto the output
            drawSamplePreviewBadge(
                canvas = canvas,
                width = width,
                height = height,
                badgeTitle = "SAMPLE PREVIEW — ${request.tool.title.uppercase()}",
                badgeSubtitle = "Requires Firebase AI Logic (Gemini)"
            )

            AiProcessingResult(
                previewBitmap = out,
                summary = "Sample preview prepared for ${request.tool.title}. Connect Firebase AI Logic for real Gemini model execution.",
                isSamplePreview = true,
                notice = statusNotice
            )
        }

    override suspend fun generateAiImages(request: AiGenRequest): AiGenerationResult =
        withContext(Dispatchers.Default) {
            delay(650)
            val (w, h) = when (request.aspectRatio) {
                "16:9" -> 1280 to 720
                "9:16" -> 720 to 1280
                "4:3" -> 1024 to 768
                "3:4" -> 768 to 1024
                else -> 960 to 960
            }
            val count = request.outputCount.coerceIn(1, 4)
            val results = (1..count).map { index ->
                renderLabeledSampleGenerationCard(
                    width = w,
                    height = h,
                    toolTitle = request.tool.title,
                    style = request.style,
                    prompt = request.prompt,
                    quality = request.quality,
                    variantIndex = index
                )
            }
            AiGenerationResult(
                bitmaps = results,
                summary = "Generated $count clearly labeled sample preview(s) for \"${request.prompt.take(40)}\".",
                isSamplePreview = true,
                notice = statusNotice
            )
        }

    private fun drawSamplePreviewBadge(
        canvas: Canvas,
        width: Int,
        height: Int,
        badgeTitle: String,
        badgeSubtitle: String
    ) {
        val barHeight = (height * 0.13f).coerceIn(64f, 120f)
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(215, 12, 12, 12)
        }
        canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), barPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 175, 55)
            strokeWidth = 3f
        }
        canvas.drawLine(0f, height - barHeight, width.toFloat(), height - barHeight, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 110)
            textSize = (barHeight * 0.28f).coerceIn(18f, 34f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (barHeight * 0.22f).coerceIn(14f, 26f)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(badgeTitle, width / 2f, height - barHeight * 0.56f, titlePaint)
        canvas.drawText(badgeSubtitle, width / 2f, height - barHeight * 0.22f, subPaint)
    }

    private fun renderLabeledSampleGenerationCard(
        width: Int,
        height: Int,
        toolTitle: String,
        style: String,
        prompt: String,
        quality: String,
        variantIndex: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(Color.rgb(8, 8, 8), Color.rgb(28, 22, 12), Color.rgb(12, 12, 12)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val margin = width * 0.06f
        val frameRect = RectF(margin, margin, width - margin, height - margin)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            color = Color.rgb(212, 175, 55)
            strokeWidth = 4f
        }
        canvas.drawRoundRect(frameRect, 24f, 24f, framePaint)

        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(212, 175, 55)
        }
        val pillWidth = width * 0.68f
        val pillHeight = (height * 0.08f).coerceIn(44f, 72f)
        val pillRect = RectF(
            (width - pillWidth) / 2f,
            height * 0.16f,
            (width + pillWidth) / 2f,
            height * 0.16f + pillHeight
        )
        canvas.drawRoundRect(pillRect, pillHeight / 2f, pillHeight / 2f, pillPaint)

        val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(8, 8, 8)
            textSize = pillHeight * 0.42f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "SAMPLE PREVIEW #$variantIndex",
            width / 2f,
            pillRect.centerY() + pillTextPaint.textSize * 0.35f,
            pillTextPaint
        )

        val headlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (width * 0.045f).coerceIn(22f, 44f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(toolTitle, width / 2f, height * 0.38f, headlinePaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 215, 110)
            textSize = (width * 0.03f).coerceIn(16f, 28f)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Style: $style  •  Quality: $quality", width / 2f, height * 0.46f, metaPaint)

        val promptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(185, 185, 185)
            textSize = (width * 0.028f).coerceIn(14f, 24f)
            textAlign = Paint.Align.CENTER
        }
        val cleanPrompt = prompt.trim().ifBlank { "No prompt entered" }.take(64)
        canvas.drawText("\"$cleanPrompt\"", width / 2f, height * 0.56f, promptPaint)

        val noticePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
            textSize = (width * 0.025f).coerceIn(13f, 22f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Enable Firebase AI Logic to generate real Gemini images",
            width / 2f,
            height * 0.78f,
            noticePaint
        )

        return bitmap
    }
}
