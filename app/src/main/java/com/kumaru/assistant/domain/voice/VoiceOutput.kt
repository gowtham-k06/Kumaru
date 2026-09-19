package com.kumaru.assistant.domain.voice

/**
 * Abstraction for text-to-speech synthesis and voice feedback.
 */
interface VoiceOutput {
    /**
     * Synthesizes and plays the provided text aloud.
     *
     * @param text The text response to speak.
     */
    suspend fun speak(text: String)

    /**
     * Interrupts and halts any currently playing speech.
     */
    fun stop()

    /**
     * True if audio is actively being synthesized or played.
     */
    val isSpeaking: Boolean
}
