package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Zen Soundscape Generator:
 * Synthesizes calming acoustic Tibetan Singing Bowl and Harmonic Chime tones
 * using Android's native AudioTrack PCM audio synthesis.
 * 100% offline, zero external audio assets required.
 */
object ZenSoundHelper {

    enum class SoundType(val title: String, val description: String) {
        TIBETAN_BOWL("Tibetan Singing Bowl", "Resonant 216Hz/432Hz tone with gentle acoustic binaural beating"),
        GENTLE_CHIME("Gentle Harmonic Chime", "Clear 528Hz Solfeggio bell decay with warm resonance"),
        MEDITATION_BELL("Zen Temple Bell", "Deep 144Hz grounding chime with slow atmospheric fade")
    }

    /**
     * Plays sound by name string (from stored preferences)
     */
    fun playSound(context: android.content.Context? = null, soundName: String = "TIBETAN_BOWL") {
        val type = when (soundName.uppercase()) {
            "ZEN_CHIME", "GENTLE_CHIME" -> SoundType.GENTLE_CHIME
            "TEMPLE_BELL", "MEDITATION_BELL" -> SoundType.MEDITATION_BELL
            else -> SoundType.TIBETAN_BOWL
        }
        playSound(type)
    }

    /**
     * Plays the selected soundscape asynchronously in IO dispatcher.
     */
    fun playSound(soundType: SoundType = SoundType.TIBETAN_BOWL) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (soundType) {
                    SoundType.TIBETAN_BOWL -> generateTibetanBowl()
                    SoundType.GENTLE_CHIME -> generateGentleChime()
                    SoundType.MEDITATION_BELL -> generateZenBell()
                }
            } catch (_: Exception) {
                // Audio synthesis failsafe
            }
        }
    }

    /**
     * Synthesizes a resonant Tibetan Singing Bowl with fundamental 432 Hz,
     * a 216 Hz sub-octave, a 1296 Hz third harmonic, and a 2.5 Hz amplitude modulation (acoustic wobble).
     */
    private fun generateTibetanBowl() {
        val sampleRate = 44100
        val durationSeconds = 3.5f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)

        val f1 = 432.0 // Fundamental
        val f2 = 216.0 // Warm sub-octave
        val f3 = 1296.0 // Shimmering harmonic
        val f4 = 864.0 // Octave harmonic
        val beatRate = 2.4 // Gentle acoustic beating (vibrato)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate

            // Envelope: instantaneous smooth onset, long natural exponential decay
            val envelope = if (t < 0.05) {
                (t / 0.05)
            } else {
                exp(-1.1 * (t - 0.05))
            }

            // Acoustic amplitude modulation (pulsing hum of singing bowl)
            val tremolo = 0.85 + 0.15 * sin(2.0 * PI * beatRate * t)

            // Combine harmonics
            val signal = (
                0.50 * sin(2.0 * PI * f1 * t) +
                0.28 * sin(2.0 * PI * f2 * t) +
                0.14 * sin(2.0 * PI * f4 * t) +
                0.08 * sin(2.0 * PI * f3 * t)
            ) * envelope * tremolo

            samples[i] = (signal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        playAudioTrack(samples, sampleRate)
    }

    /**
     * Synthesizes a bright, uplifting 528 Hz Solfeggio bell chime with warm decay.
     */
    private fun generateGentleChime() {
        val sampleRate = 44100
        val durationSeconds = 2.8f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)

        val f1 = 528.0 // Solfeggio "Transformation & Miracles"
        val f2 = 1056.0 // Harmonic octave
        val f3 = 1584.0 // Upper sparkle

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate

            // Fast strike, exponential ringing decay
            val envelope = if (t < 0.015) {
                (t / 0.015)
            } else {
                exp(-1.6 * (t - 0.015))
            }

            val signal = (
                0.65 * sin(2.0 * PI * f1 * t) +
                0.25 * sin(2.0 * PI * f2 * t) +
                0.10 * sin(2.0 * PI * f3 * t)
            ) * envelope

            samples[i] = (signal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        playAudioTrack(samples, sampleRate)
    }

    /**
     * Synthesizes a deep, grounding meditation temple bell at 144 Hz.
     */
    private fun generateZenBell() {
        val sampleRate = 44100
        val durationSeconds = 3.8f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)

        val f1 = 144.0
        val f2 = 288.0
        val f3 = 576.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate

            val envelope = if (t < 0.03) {
                (t / 0.03)
            } else {
                exp(-0.85 * (t - 0.03))
            }

            val signal = (
                0.60 * sin(2.0 * PI * f1 * t) +
                0.30 * sin(2.0 * PI * f2 * t) +
                0.10 * sin(2.0 * PI * f3 * t)
            ) * envelope

            samples[i] = (signal.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        playAudioTrack(samples, sampleRate)
    }

    private fun playAudioTrack(samples: ShortArray, sampleRate: Int) {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(samples.size * 2)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            Thread.sleep((samples.size * 1000L) / sampleRate + 200L)
        } catch (_: Exception) {
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
