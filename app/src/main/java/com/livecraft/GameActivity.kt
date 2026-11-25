package com.livecraft

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.sin

class GameActivity : Activity() {
    private val christmasMusic = ChristmasMusic()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        setContentView(TowerGameView(this))
    }

    override fun onResume() {
        super.onResume()
        christmasMusic.start()
    }

    override fun onPause() {
        christmasMusic.stop()
        super.onPause()
    }

    override fun onBackPressed() {
        GameState.stopGame()
        super.onBackPressed()
    }

    override fun onDestroy() {
        GameState.onChanged = null
        christmasMusic.release()
        super.onDestroy()
    }
}

private class TowerGameView(context: android.content.Context) : View(context) {
    private val d = resources.displayMetrics.density
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val images = ConcurrentHashMap<String, Bitmap>()
    private var snow = 0
    private val vividFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1.9f) })

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

        drawFestiveBorders(c, w, h)
        drawCountryRows(c, w, h)
        postInvalidateDelayed(110)
    }


    private fun drawFestiveBorders(c: Canvas, w: Float, h: Float) {
        // Top and bottom festive bands frame the battle while keeping the rows centered.
        p.color = 0xAA082536.toInt()
        c.drawRoundRect(RectF(dp(8f), dp(88f), w-dp(8f), dp(132f)), dp(16f), dp(16f), p)
        c.drawRoundRect(RectF(dp(8f), h-dp(100f), w-dp(8f), h-dp(14f)), dp(16f), dp(16f), p)
        val colors = intArrayOf(0xFFFF4040.toInt(), 0xFFFFD54A.toInt(), 0xFF31D879.toInt(), 0xFF40C4FF.toInt(), 0xFFFF70D0.toInt())
        p.style = Paint.Style.STROKE
        p.strokeWidth = dp(3f)
        p.color = 0xFF16834B.toInt()
        val topGarland = Path().apply { moveTo(dp(14f), dp(94f)); quadTo(w/2, dp(122f), w-dp(14f), dp(94f)) }
        val bottomGarland = Path().apply { moveTo(dp(14f), h-dp(94f)); quadTo(w/2, h-dp(58f), w-dp(14f), h-dp(94f)) }
        c.drawPath(topGarland, p); c.drawPath(bottomGarland, p)
        p.style = Paint.Style.FILL
        for (i in 0..10) {
            val x = w*(i+1)/12f
            val y1 = dp(104f)+dp(12f)*sin(i/10f*Math.PI).toFloat()
            val y2 = h-dp(84f)-dp(12f)*sin(i/10f*Math.PI).toFloat()
            p.color = colors[i%colors.size]
            c.drawCircle(x,y1,dp(4f),p); c.drawCircle(x,y2,dp(4f),p)
        }
        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        p.textSize = dp(18f)
        p.color = 0xFFFFE6A0.toInt()
        c.drawText("❄  CHRISTMAS BATTLE  ❄", w/2f, dp(116f), p)
        p.textSize = dp(17f)
        c.drawText("🎄  ✦  ❄  ✦  🎁", w/2f, h-dp(53f), p)
    }

    private fun drawCountryRows(c: Canvas, w: Float, h: Float) {
        val countries = WorldCountries.all
        val values = GameState.snapshot()
        val left = dp(12f)
        val right = w - dp(12f)
        val top = dp(142f)
        val bottom = h - dp(112f)
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
            p.colorFilter = vividFilter
            c.drawText(country.flag, left + dp(27f), centerY + dp(9f), p)
            p.colorFilter = null

            val gift = images[country.id]
            if (gift != null) {
                val size = min(dp(25f), rowH * .48f)
                val ratio = gift.width.toFloat() / gift.height.coerceAtLeast(1)
                val dw = if (ratio >= 1f) size else size * ratio
                val dh = if (ratio >= 1f) size / ratio else size
                val gx = left + dp(53f)
                p.colorFilter = vividFilter
                c.drawBitmap(gift, null, RectF(gx - dw / 2f, centerY - dh / 2f, gx + dw / 2f, centerY + dh / 2f), p)
                p.colorFilter = null
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
