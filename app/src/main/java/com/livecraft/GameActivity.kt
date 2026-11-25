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

        p.shader = LinearGradient(0f, 0f, w, h, 0xFF071827.toInt(), 0xFF174957.toInt(), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null

        // Soft snow and festive lights
        p.color = 0x99EAF8FF.toInt()
        repeat(42) { i ->
            val x = (i * 73f) % w
            val y = (i * 53f + snow * (1 + i % 3) * .35f) % h
            c.drawCircle(x, y, dp(if (i % 5 == 0) 1.5f else .8f), p)
        }
        snow = (snow + 1) % 10000

        val garland = Path().apply {
            moveTo(0f, dp(7f)); quadTo(w * .5f, dp(31f), w, dp(7f))
        }
        p.color = 0xFF39765B.toInt()
        p.style = Paint.Style.STROKE
        p.strokeWidth = dp(2.5f)
        c.drawPath(garland, p)
        p.style = Paint.Style.FILL
        val bulbs = intArrayOf(0xFFFFD166.toInt(), 0xFFFF6B6B.toInt(), 0xFF8EE3B2.toInt(), 0xFF8BC9FF.toInt())
        for (i in 0..8) {
            p.color = bulbs[i % bulbs.size]
            c.drawCircle(w * (i + 1) / 10f, dp(12f) + dp(8f) * sin(i / 8f * Math.PI).toFloat(), dp(3f), p)
        }

        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        p.color = Color.WHITE
        p.textSize = dp(22f)
        c.drawText("WORLD TOWER WARS", w / 2f, dp(57f), p)
        p.typeface = Typeface.DEFAULT_BOLD
        p.color = 0xFFFFD166.toInt()
        p.textSize = dp(11f)
        c.drawText("100 PRESENTES  •  1 WIN", w / 2f, dp(78f), p)

        drawCountryRows(c, w, h)
        postInvalidateDelayed(110)
    }

    private fun drawCountryRows(c: Canvas, w: Float, h: Float) {
        val countries = WorldCountries.all
        val values = GameState.snapshot()
        val left = dp(12f)
        val right = w - dp(12f)
        val top = dp(96f)
        val bottom = h - dp(12f)
        val rowGap = dp(5f)
        val rowH = ((bottom - top) - rowGap * (countries.size - 1)) / countries.size

        countries.forEachIndexed { index, country ->
            val y = top + index * (rowH + rowGap)
            val card = RectF(left, y, right, y + rowH)
            p.color = 0xCC102B3B.toInt()
            c.drawRoundRect(card, dp(11f), dp(11f), p)
            p.color = 0x447FA6B8
            p.style = Paint.Style.STROKE
            p.strokeWidth = dp(1f)
            c.drawRoundRect(card, dp(11f), dp(11f), p)
            p.style = Paint.Style.FILL

            val centerY = y + rowH * .43f
            p.textAlign = Paint.Align.CENTER
            p.typeface = Typeface.DEFAULT
            p.textSize = dp(27f)
            c.drawText(country.flag, left + dp(27f), centerY + dp(9f), p)

            val gift = images[country.id]
            if (gift != null) {
                val size = min(dp(25f), rowH * .48f)
                val ratio = gift.width.toFloat() / gift.height.coerceAtLeast(1)
                val dw = if (ratio >= 1f) size else size * ratio
                val dh = if (ratio >= 1f) size / ratio else size
                val gx = left + dp(53f)
                c.drawBitmap(gift, null, RectF(gx - dw / 2f, centerY - dh / 2f, gx + dw / 2f, centerY + dh / 2f), p)
            }

            val textX = left + dp(75f)
            val progress = values[country.id] ?: CountryProgress()
            p.textAlign = Paint.Align.LEFT
            p.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            p.color = Color.WHITE
            p.textSize = dp(13f)
            c.drawText(country.name, textX, y + rowH * .39f, p)

            p.textAlign = Paint.Align.RIGHT
            p.typeface = Typeface.DEFAULT_BOLD
            p.color = 0xFFFFD166.toInt()
            p.textSize = dp(11f)
            c.drawText("WIN ${progress.wins}", right - dp(12f), y + rowH * .39f, p)

            val barLeft = textX
            val barRight = right - dp(12f)
            val barTop = y + rowH * .57f
            val barBottom = min(y + rowH - dp(7f), barTop + dp(9f))
            p.color = 0xFF294657.toInt()
            c.drawRoundRect(RectF(barLeft, barTop, barRight, barBottom), dp(5f), dp(5f), p)
            if (progress.points > 0) {
                p.color = country.color
                val fillRight = barLeft + (barRight - barLeft) * (progress.points / 100f)
                c.drawRoundRect(RectF(barLeft, barTop, fillRight.coerceAtLeast(barLeft + dp(3f)), barBottom), dp(5f), dp(5f), p)
            }

            p.textAlign = Paint.Align.RIGHT
            p.typeface = Typeface.DEFAULT_BOLD
            p.color = 0xFFD6E7ED.toInt()
            p.textSize = dp(8f)
            c.drawText("${progress.points}/100", barRight, barTop - dp(2f), p)
        }
    }
}
