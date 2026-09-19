package com.kumaru.assistant.data.voice

import com.kumaru.assistant.domain.voice.VoiceOutput
import kotlinx.coroutines.delay

/**
 * Voice output coordinator for V0.1.
 * Simulates realistic speech cadence and playback state.
 *
 * In a subsequent stage, native Android [android.speech.tts.TextToSpeech]
 * or a neural streaming audio synthesizer will replace this engine.
 */
class SystemVoiceOutput : VoiceOutput {

    private var _isSpeaking: Boolean = false
    override val isSpeaking: Boolean
        get() = _isSpeaking

    private var cancelled: Boolean = false

    override suspend fun speak(text: String) {
        _isSpeaking = true
        cancelled = false

        // Compute simulated vocal duration based on word count (~180 words per minute)
        val wordCount = text.split("\\s+".toRegex()).size
        val durationMs = (wordCount * 280L).coerceIn(1200L, 4000L)

        val interval = 100L
        var elapsed = 0L
        while (elapsed < durationMs && !cancelled) {
            delay(interval)
            elapsed += interval
        }

        _isSpeaking = false
    }

    override fun stop() {
        cancelled = true
        _isSpeaking = false
    }
}
