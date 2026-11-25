package com.livecraft

import android.content.Context

data class CountryConfig(
    val id: String,
    val name: String,
    val shortName: String,
    val flag: String,
    val giftName: String,
    val giftImageUrl: String,
    val color: Int
)

data class CountryProgress(val points: Int = 0, val wins: Int = 0)

object WorldCountries {
    val all = listOf(
        CountryConfig("br", "Brasil", "BRA", "🇧🇷", "Rose", "https://beetgames.com/images/gifts/rose.webp", 0xFF25B56A.toInt()),
        CountryConfig("us", "Estados Unidos", "USA", "🇺🇸", "GG", "https://beetgames.com/images/gifts/gg.webp", 0xFF4D83E6.toInt()),
        CountryConfig("jp", "Japão", "JPN", "🇯🇵", "Ice Cream Cone", "https://beetgames.com/images/gifts/ice_cream_cone.webp", 0xFFFF6B91.toInt()),
        CountryConfig("de", "Alemanha", "GER", "🇩🇪", "Cake Slice", "https://beetgames.com/images/gifts/cake_slice.webp", 0xFFF0B23C.toInt()),
        CountryConfig("fr", "França", "FRA", "🇫🇷", "Freestyle", "https://beetgames.com/images/gifts/freestyle.webp", 0xFF8C68D8.toInt()),
        CountryConfig("uk", "Reino Unido", "UK", "🇬🇧", "Glow Stick", "https://beetgames.com/images/gifts/glow_stick.webp", 0xFF43B9D1.toInt()),
        CountryConfig("mx", "México", "MEX", "🇲🇽", "Pop", "https://beetgames.com/images/gifts/pop.webp", 0xFFE36B55.toInt()),
        CountryConfig("ca", "Canadá", "CAN", "🇨🇦", "Starfish", "https://beetgames.com/images/gifts/starfish.webp", 0xFFDB5A55.toInt()),
        CountryConfig("it", "Itália", "ITA", "🇮🇹", "TikTok", "https://beetgames.com/images/gifts/tiktok.webp", 0xFF4AAE82.toInt()),
        CountryConfig("es", "Espanha", "ESP", "🇪🇸", "Wink wink", "https://beetgames.com/images/gifts/wink_wink.webp", 0xFFE0A33B.toInt())
    )

    fun findByGiftName(name: String): CountryConfig? {
        val normalized = normalize(name)
        return all.firstOrNull { normalize(it.giftName) == normalized }
    }

    private fun normalize(value: String) =
        value.lowercase().filter { it.isLetterOrDigit() }
}

object GameState {
    private const val PREFS = "world_tower_wars"
    private const val USER_KEY = "tiktok_username"
    private var preferences: android.content.SharedPreferences? = null
    private val progress = linkedMapOf<String, CountryProgress>()
    @Volatile var isRunning: Boolean = false
        private set
    @Volatile var onChanged: (() -> Unit)? = null

    fun initialize(context: Context) {
        if (preferences != null) return
        preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        synchronized(this) {
            WorldCountries.all.forEach { country ->
                progress[country.id] = CountryProgress(
                    points = preferences!!.getInt("points_${country.id}", 0).coerceIn(0, 99),
                    wins = preferences!!.getInt("wins_${country.id}", 0).coerceAtLeast(0)
                )
            }
        }
    }

    fun saveUsername(username: String) {
        preferences?.edit()?.putString(USER_KEY, username.trim().removePrefix("@"))?.apply()
    }

    fun loadUsername(): String = preferences?.getString(USER_KEY, "").orEmpty()

    fun startGame() {
        isRunning = true
        onChanged?.invoke()
    }

    fun stopGame() {
        isRunning = false
        onChanged?.invoke()
    }

    @Synchronized
    fun snapshot(): Map<String, CountryProgress> = progress.toMap()

    @Synchronized
    fun addGift(giftName: String, giftCount: Int, streakOver: Boolean): Int {
        if (!isRunning || !streakOver) return 0
        val country = WorldCountries.findByGiftName(giftName) ?: return 0
        val old = progress[country.id] ?: CountryProgress()
        val total = old.points + giftCount.coerceAtLeast(1)
        val winsAdded = total / 100
        progress[country.id] = CountryProgress(points = total % 100, wins = old.wins + winsAdded)
        preferences?.edit()
            ?.putInt("points_${country.id}", total % 100)
            ?.putInt("wins_${country.id}", old.wins + winsAdded)
            ?.apply()
        onChanged?.invoke()
        return winsAdded
    }
}
