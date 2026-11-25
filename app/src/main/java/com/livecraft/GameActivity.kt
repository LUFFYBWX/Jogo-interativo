package com.livecraft

import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.sin

class GameActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        setContentView(TowerGameView(this))
    }

    override fun onBackPressed() {
        GameState.stopGame()
        super.onBackPressed()
    }

    override fun onDestroy() {
        GameState.onChanged = null
        super.onDestroy()
    }
}

private class TowerGameView(context: android.content.Context) : View(context) {
    private val d = resources.displayMetrics.density
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val images = ConcurrentHashMap<String, Bitmap>()
    private var snow = 0

    init {
        GameState.onChanged = { postInvalidateOnAnimation() }
        WorldCountries.all.forEach { country ->
            Thread({
                try {
                    URL(country.giftImageUrl).openStream().use { stream ->
                        BitmapFactory.decodeStream(stream)?.let {
                            images[country.id] = it
                            postInvalidate()
                        }
                    }
                } catch (_: Exception) { }
            }, "gift-${country.id}").start()
        }
    }

    private fun dp(v: Float) = v * d

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        p.shader = LinearGradient(0f, 0f, 0f, h, 0xFF061426.toInt(), 0xFF164455.toInt(), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null

        // Falling snow
        p.color = 0x99EAF8FF.toInt()
        repeat(55) { i ->
            val x = (i * 89f) % w
            val y = (i * 47f + snow * (1 + i % 3) * 0.45f) % h
            c.drawCircle(x, y, dp(if (i % 5 == 0) 1.7f else 0.9f), p)
        }
        snow = (snow + 1) % 10000

        // Snowy fir silhouettes on the sides
        drawTree(c, dp(20f), h - dp(27f), dp(64f))
        drawTree(c, w - dp(20f), h - dp(27f), dp(64f))
        p.color = 0xFFE9F5F8.toInt()
        c.drawRoundRect(0f, h - dp(34f), w, h + dp(5f), dp(12f), dp(12f), p)

        // Garland and colored Christmas bulbs
        val garland = Path().apply {
            moveTo(0f, dp(9f)); quadTo(w * .25f, dp(34f), w * .5f, dp(14f))
            quadTo(w * .75f, -dp(3f), w, dp(20f))
        }
        p.color = 0xFF39765B.toInt(); p.style = Paint.Style.STROKE; p.strokeWidth = dp(3f)
        c.drawPath(garland, p); p.style = Paint.Style.FILL
        val bulbColors = intArrayOf(0xFFFFD166.toInt(), 0xFFFF6B6B.toInt(), 0xFF8EE3B2.toInt(), 0xFF8BC9FF.toInt())
        for (i in 0..8) {
            p.color = bulbColors[i % bulbColors.size]
            val x = w * (i + 1) / 10f
            c.drawCircle(x, dp(17f) + dp(8f) * sin(i / 8f * Math.PI).toFloat(), dp(3f), p)
        }

        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        p.color = Color.WHITE; p.textSize = dp(17f)
        c.drawText("WORLD TOWER WARS", w / 2f, dp(42f), p)
        p.typeface = Typeface.DEFAULT; p.color = 0xFFB8D6E2.toInt(); p.textSize = dp(8f)
        c.drawText("100 PRESENTES = 1 WIN", w / 2f, dp(55f), p)

        drawTowers(c, w, h)
        postInvalidateDelayed(110)
    }

    private fun drawTree(c: Canvas, x: Float, base: Float, height: Float) {
        val path = Path().apply {
            moveTo(x, base - height); lineTo(x - height * .42f, base - height * .32f)
            lineTo(x - height * .18f, base - height * .32f); lineTo(x - height * .5f, base)
            lineTo(x + height * .5f, base); lineTo(x + height * .18f, base - height * .32f)
            lineTo(x + height * .42f, base - height * .32f); close()
        }
        p.color = 0xFF14515A.toInt(); c.drawPath(path, p)
        p.color = Color.WHITE; c.drawCircle(x, base - height + dp(4f), dp(3f), p)
    }

    private fun drawTowers(c: Canvas, w: Float, h: Float) {
        val countries = WorldCountries.all
        val values = GameState.snapshot()
        val col = w / countries.size
        val top = dp(145f)
        val base = h - dp(39f)
        val blockH = (base - top).coerceAtLeast(dp(80f)) / 100f

        countries.forEachIndexed { index, country ->
            val x = col * (index + .5f)
            val score = values[country.id] ?: CountryProgress()

            // WIN badge above each flag
            val badgeW = min(col * .88f, dp(68f))
            p.color = 0xCC102C40.toInt()
            c.drawRoundRect(RectF(x - badgeW / 2f, dp(65f), x + badgeW / 2f, dp(83f)), dp(7f), dp(7f), p)
            p.color = 0xFFFFD166.toInt(); p.typeface = Typeface.DEFAULT_BOLD; p.textSize = dp(8f)
            c.drawText("WIN ${score.wins}", x, dp(77f), p)

            p.textSize = dp(21f); c.drawText(country.flag, x, dp(108f), p)

            // Original gift icon from the gift catalog
            val bitmap = images[country.id]
            if (bitmap != null) {
                val size = min(col * .38f, dp(29f))
                val ratio = bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1)
                val dw = if (ratio >= 1f) size else size * ratio
                val dh = if (ratio >= 1f) size / ratio else size
                c.drawBitmap(bitmap, null, RectF(x - dw / 2f, dp(113f), x + dw / 2f, dp(113f) + dh), p)
            }

            val towerW = min(col * .56f, dp(48f))
            val tower = RectF(x - towerW / 2f, top, x + towerW / 2f, base)
            p.color = 0x44233F55; c.drawRoundRect(tower, dp(5f), dp(5f), p)
            p.color = 0x667EA3B4; p.style = Paint.Style.STROKE; p.strokeWidth = dp(.8f)
            c.drawRoundRect(tower, dp(5f), dp(5f), p); p.style = Paint.Style.FILL

            // One separate rectangular block for each point
            for (block in 0 until score.points) {
                val y = base - (block + 1) * blockH
                val rect = RectF(x - towerW / 2f + dp(1.2f), y + dp(.2f),
                    x + towerW / 2f - dp(1.2f), y + blockH - dp(.35f))
                p.color = country.color; c.drawRect(rect, p)
                p.color = 0x44FFFFFF; c.drawRect(rect.left + dp(1f), rect.top, rect.right - dp(1f), rect.top + dp(.6f), p)
            }

            p.color = 0xFFEAF5F8.toInt(); p.typeface = Typeface.DEFAULT_BOLD; p.textSize = dp(8f)
            c.drawText(country.shortName, x, h - dp(20f), p)
            p.color = 0xFF24495B.toInt(); p.typeface = Typeface.DEFAULT; p.textSize = dp(7f)
            c.drawText("${score.points}/100", x, h - dp(8f), p)
        }
    }
}
