package com.livecraft

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var usernameInput: EditText
    private lateinit var connectionStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(7, 19, 35)
        window.navigationBarColor = Color.rgb(7, 19, 35)
        GameState.initialize(this)
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(26), dp(24), dp(24))
            setBackgroundColor(Color.rgb(7, 19, 35))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        scroll.addView(content)

        content.addView(label("WORLD TOWER WARS", 27f, Color.WHITE, true).apply {
            letterSpacing = 0.08f
            gravity = Gravity.CENTER
        }, matchWrap())
        content.addView(label("10 países. Uma disputa. 100 presentes por WIN.", 13f, Color.rgb(172, 194, 211), false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(28))
        }, matchWrap())

        content.addView(sectionTitle("CONEXÃO COM O TIKTOK"))
        usernameInput = EditText(this).apply {
            setSingleLine(true)
            hint = "@seu_usuario"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(125, 148, 167))
            textSize = 16f
            setPadding(dp(16), dp(13), dp(16), dp(13))
            setBackgroundColor(Color.rgb(21, 42, 61))
            setText(GameState.loadUsername().let { if (it.isBlank()) "" else "@$it" })
        }
        content.addView(usernameInput, matchWrap().apply { bottomMargin = dp(12) })

        content.addView(actionButton("SALVAR USUÁRIO", Color.rgb(39, 75, 101)) {
            val username = usernameInput.text.toString().trim().removePrefix("@")
            if (username.isBlank()) {
                usernameInput.error = "Digite o @ do TikTok"
            } else {
                GameState.saveUsername(username)
                Toast.makeText(this, "Usuário salvo!", Toast.LENGTH_SHORT).show()
            }
        }, matchWrap().apply { bottomMargin = dp(20) })

        content.addView(sectionTitle("STATUS DA LIVE"))
        connectionStatus = label("Desconectado", 14f, Color.rgb(255, 205, 112), true).apply {
            setPadding(dp(15), dp(15), dp(15), dp(15))
            setBackgroundColor(Color.rgb(21, 42, 61))
        }
        content.addView(connectionStatus, matchWrap().apply { bottomMargin = dp(12) })

        content.addView(actionButton("CONECTAR À LIVE", Color.rgb(30, 139, 99)) {
            val username = usernameInput.text.toString().trim().removePrefix("@")
            if (username.isBlank()) {
                usernameInput.error = "Digite o @ do TikTok"
                return@actionButton
            }
            GameState.saveUsername(username)
            GameBridgeService.statusListener = { message -> connectionStatus.text = message }
            try {
                TikTokNative.start(username)
                connectionStatus.text = "Solicitação enviada para conectar à live de @$username..."
            } catch (error: UnsatisfiedLinkError) {
                connectionStatus.text = "Detector salvo no repositório, mas a biblioteca nativa ainda não está incluída neste APK."
            } catch (error: Exception) {
                connectionStatus.text = "Não foi possível iniciar a conexão: ${error.message ?: "erro desconhecido"}"
            }
        }, matchWrap().apply { bottomMargin = dp(24) })

        content.addView(sectionTitle("JOGO"))
        content.addView(label("A arena abre em tela cheia e na horizontal. Os presentes reconhecidos somam 1 bloco por envio. A cada 100 blocos, o país ganha 1 WIN e a torre recomeça.", 14f, Color.rgb(172, 194, 211), false).apply {
            setPadding(0, 0, 0, dp(18))
        }, matchWrap())
        content.addView(actionButton("INICIAR JOGO", Color.rgb(197, 47, 62)) {
            GameState.startGame()
            startActivity(Intent(this, GameActivity::class.java))
        }, matchWrap())

        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun sectionTitle(text: String) = label(text, 12f, Color.rgb(113, 165, 193), true).apply {
        letterSpacing = 0.12f
        setPadding(0, dp(4), 0, dp(9))
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun actionButton(text: String, color: Int, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        setBackgroundColor(color)
        setOnClickListener { action() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
