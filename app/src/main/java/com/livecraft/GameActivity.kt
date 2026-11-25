package com.livecraft

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

class GameActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        setContentView(TowerGameView(this))
    }

    override fun onDestroy() {
        GameState.onChanged = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        GameState.stopGame()
        super.onBackPressed()
    }
}

private class TowerGameView(context: android.content.Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val giftBitmaps = ConcurrentHashMap<String, Bitmap>()
    private val failedImages = ConcurrentHashMap.newKeySet<String>()
    private var snowTick = 0

    init {
        GameState.onChanged = { postInvalidateOnAnimation() }
        WorldCountries.all.forEach { country ->
            Thread({
                try {
                    val connection = URL(country.giftImageUrl).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.getInputStream().use { stream ->
                        BitmapFactory.decodeStream(stream)?.let { bitmap ->
                            giftBitmaps[country.id] = bitmap
                            postInvalidate()
                        }
                    }
                } catch (_: Exception) {
                    failedImages.add(country.id)
                    postInvalidate()
                }
            }, "gift-image-${country.id}").start()
        }
    }

    private fun dp(value: Float) = value * density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        drawWinterBackground(canvas, w, h)
        drawHeader(canvas, w)
        drawCountries(canvas, w, h)
        snowTick = (snowTick + 1) % 10000
        postInvalidateDelayed(110)
    }

    private fun drawWinterBackground(canvas: Canvas, w: Float, h: Float) {
        paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(5, 17, 35), Color.rgb(12, 49, 66), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // Soft moon glow
        paint.color = 0x223D9BCB
        canvas.drawCircle(w * 0.5f, h * 0.18f, dp(100f), paint)
        paint.color = 0x66D9F2FF
        for (i in 0 until 42) {
            val x = ((i * 97f) % w)
            val y = ((i * 53f + snowTick * (1 + i % 3) * 0.55f) % h)
            val radius = dp(if (i % 4 == 0) 1.8f else 1.0f)
            canvas.drawCircle(x, y, radius, paint)
        }

        // Distant snowy fir trees at the edges
        drawTree(canvas, dp(22f), h - dp(24f), dp(65f), 0xFF164653.toInt())
        drawTree(canvas, w - dp(22f), h - dp(24f), dp(72f), 0xFF164653.toInt())
        drawTree(canvas, dp(70f), h - dp(24f), dp(43f), 0xFF1A5960.toInt())
        drawTree(canvas, w - dp(70f), h - dp(24f), dp(48f), 0xFF1A5960.toInt())

        // Snow-covered floor
        paint.color = 0xFFE8F4F7.toInt()
        canvas.drawRoundRect(RectF(0f, h - dp(35f), w, h + dp(8f)), dp(14f), dp(14f), paint)
        paint.color = 0xFFBBDCE5.toInt()
        canvas.drawRect(0f, h - dp(35f), w, h - dp(31f), paint)

        // Christmas light garland
        val garland = Path().apply {
            moveTo(0f, dp(8f))
            quadTo(w * 0.25f, dp(34f), w * 0.5f, dp(13f))
            quadTo(w * 0.75f, -dp(5f), w, dp(20f))
        }
        paint.color = 0xFF285C4A.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(3f)
        canvas.drawPath(garland, paint)
        paint.style = Paint.Style.FILL
        val bulbs = listOf(0.08f, 0.18f, 0.29f, 0.40f, 0.51f, 0.62f, 0.73f, 0.84f, 0.95f)
        val bulbColors = intArrayOf(0xFFFFD166.toInt(), 0xFFFF6B6B.toInt(), 0xFF8EE3B2.toInt(), 0xFF8BC9FF.toInt())
        bulbs.forEachIndexed { index, fraction ->
            val x = w * fraction
            val y = dp(17f) + dp(8f) * kotlin.math.sin(fraction * Math.PI).toFloat()
            paint.color = bulbColors[index % bulbColors.size]
            canvas.drawCircle(x, y, dp(3.3f), paint)
        }
    }

    private fun drawTree(canvas: Canvas, x: Float, base: Float, height: Float, color: Int) {
        val path = Path().apply {
            moveTo(x, base - height)
            lineTo(x - height * 0.36f, base - height * 0.36f)
            lineTo(x - height * 0.18f, base - height * 0.36f)
            lineTo(x - height * 0.48f, base)
            lineTo(x + height * 0.48f, base)
            lineTo(x + height * 0.18f, base - height * 0.36f)
            lineTo(x + height * 0.36f, base - height * 0.36f)
            close()
        }
        paint.color = color
        canvas.drawPath(path, paint)
        paint.color = 0xFFEAF7FA.toInt()
        canvas.drawCircle(x, base - height + dp(4f), dp(3f), paint)
    }

    private fun drawHeader(canvas: Canvas, w: Float) {
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        paint.textSize = dp(17f)
        canvas.drawText("WORLD TOWER WARS", w / 2f, dp(42f), paint)
        paint.color = 0xFFB8D6E2.toInt()
        paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        paint.textSize = dp(8f)
        canvas.drawText("100 PRESENTES = 1 WIN", w / 2f, dp(55f), paint)
    }

    private fun drawCountries(canvas: Canvas, w: Float, h: Float) {
        val countries = WorldCountries.all
        val scores = GameState.snapshot()
        val columnWidth = w / countries.size
        val towerTop = dp(145f)
        val towerBase = h - dp(39f)
        val towerHeight = (towerBase - towerTop).coerceAtLeast(dp(80f))
        val segmentHeight = towerHeight / 100f

        countries.forEachIndexed { index, country ->
            val centerX = columnWidth * (index + 0.5f)
            val score = scores[country.id] ?: CountryProgress()
            val badgeWidth = min(columnWidth * 0.88f, dp(68f))
            val badge = RectF(centerX - badgeWidth / 2f, dp(65f), centerX + badgeWidth / 2f, dp(83f))
            paint.color = 0xCC102C40.toInt()
            canvas.drawRoundRect(badge, dp(7f), dp(7f), paint)
            paint.color = 0xFFFFD166.toInt()
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            paint.textSize = dp(8f)
            canvas.drawText("WIN ${score.wins}", centerX, dp(77f), paint)

            paint.textSize = dp(21f)
            canvas.drawText(country.flag, centerX, dp(108f), paint)

            val giftSize = min(columnWidth * 0.38f, dp(29f))
            val giftTop = dp(113f)
            val bitmap = giftBitmaps[country.id]
            if (bitmap != null) {
                val ratio = bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1)
                val drawWidth = if (ratio >= 1f) giftSize else giftSize * ratio
                val drawHeight = if (ratio >= 1f) giftSize / ratio else giftSize
                val destination = RectF(centerX - drawWidth / 2f, giftTop, centerX + drawWidth / 2f, giftTop + drawHeight)
                paint.color = Color.WHITE
                canvas.drawBitmap(bitmap, null, destination, paint)
            } else {
                paint.color = 0xFFB9D8E5.toInt()
                paint.textSize = dp(7f)
                canvas.drawText(country.giftName, centerX, giftTop + dp(12f), paint)
            }

            val towerWidth = min(columnWidth * 0.56f, dp(48f))
            val towerRect = RectF(centerX - towerWidth / 2f, towerTop, centerX + towerWidth / 2f, towerBase)
            paint.color = 0x44233F55
            canvas.drawRoundRect(towerRect, dp(5f), dp(5f), paint)
            paint.color = 0x667EA3B4
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(0.8f)
            canvas.drawRoundRect(towerRect, dp(5f), dp(5f), paint)
            paint.style = Paint.Style.FILL

            for (block in 0 until score.points) {
                val blockTop = towerBase - (block + 1) * segmentHeight
                val rect = RectF(
                    centerX - towerWidth / 2f + dp(1.2f),
                    blockTop + dp(0.25f),
                    centerX + towerWidth / 2f - dp(1.2f),
                    blockTop + segmentHeight - dp(0.45f)
                )
                paint.color = country.color
                canvas.drawRoundRect(rect, dp(1.2f), dp(1.2f), paint)
                paint.color = 0x44FFFFFF
                canvas.drawRect(rect.left + dp(1f), rect.top, rect.right - dp(1f), rect.top + dp(0.65f), paint)
            }

            paint.color = 0xFFEAF5F8.toInt()
            paint.textSize = dp(8f)
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            canvas.drawText(country.shortName, centerX, h - dp(20f), paint)
            paint.color = 0xFF24495B.toInt()
            paint.textSize = dp(7f)
            paint.typeface = android.graphics.Typeface.DEFAULT
            canvas.drawText("${score.points}/100", centerX, h - dp(8f), paint)
        }
    }
}
