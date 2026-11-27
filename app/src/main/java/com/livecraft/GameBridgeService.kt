package com.livecraft

import android.os.Handler
import android.os.Looper

class GameBridgeService {
    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())
        @Volatile var statusListener: ((String) -> Unit)? = null

        @JvmStatic fun onNativeTikTokStatus(message: String) {
            mainHandler.post { statusListener?.invoke(message) }
        }

        @JvmStatic fun onNativeTikTokDiagnostic(eventType: String) {
            // Diagnostic events are intentionally kept out of the public game screen.
        }

        @JvmStatic fun onNativeTikTokChat(username: String, comment: String) {
            GameState.addVoteComment(comment)
        }

        @JvmStatic fun onNativeTikTokLikes(count: Int) {
            // Likes do not affect this game.
        }

        @JvmStatic fun onNativeTikTokGift(
            name: String,
            imageUrl: String,
            coins: Long,
            combo: Boolean,
            streakOver: Boolean,
            giftCount: Int
        ) {
            GameState.addGift(name, giftCount, streakOver, coins)
        }
    }
}
