package com.livecraft

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Original, lightweight bell melody for the Christmas game theme. */
internal class ChristmasMusic {
    private var track: AudioTrack? = null

    fun start() {
        if (track?.playState == AudioTrack.PLAYSTATE_PLAYING) return
        if (track == null) {
            val sampleRate = 22050
            val melody = doubleArrayOf(
                784.0, 988.0, 1175.0, 988.0, 880.0, 784.0, 659.0, 784.0,
                880.0, 988.0, 880.0, 784.0, 659.0, 587.0, 659.0, 784.0,
                988.0, 1175.0, 1319.0, 1175.0, 988.0, 880.0, 784.0, 659.0,
                784.0, 880.0, 988.0, 784.0, 659.0, 587.0, 523.0, 0.0
            )
            val noteSeconds = 0.24
            val samplesPerNote = (sampleRate * noteSeconds).toInt()
            val pcm = ShortArray(melody.size * samplesPerNote)
            melody.forEachIndexed { index, frequency ->
                for (i in 0 until samplesPerNote) {
                    val t = i.toDouble() / sampleRate
                    val envelope = exp(-5.0 * t / noteSeconds) * (1.0 - exp(-90.0 * t))
                    val wave = if (frequency == 0.0) 0.0 else
                        sin(2.0 * PI * frequency * t) * 0.68 +
                        sin(2.0 * PI * frequency * 2.76 * t) * 0.22 +
                        sin(2.0 * PI * frequency * 4.1 * t) * 0.10
                    pcm[index * samplesPerNote + i] =
                        (wave * envelope * 10500.0).toInt().coerceIn(-32768, 32767).toShort()
                }
            }
            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val audio = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuffer, pcm.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            audio.write(pcm, 0, pcm.size)
            audio.setLoopPoints(0, pcm.size, -1)
            audio.setVolume(0.22f)
            track = audio
        }
        track?.play()
    }

    fun stop() {
        track?.pause()
        track?.flush()
    }

    fun release() {
        track?.release()
        track = null
    }
}
