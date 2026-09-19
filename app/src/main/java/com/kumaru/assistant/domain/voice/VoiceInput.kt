package com.kumaru.assistant.domain.voice

/**
 * Abstraction for speech recognition and voice capture.
 *
 * For V0.1, push-to-talk is utilized.
 * In future stages, continuous listening and the wake-phrase ("Enna Kumaru")
 * will hook into this abstraction.
 */
interface VoiceInput {
    /**
     * Starts audio capture / speech recognition session.
     *
     * @param onResult Callback invoked when speech transcription is complete.
     * @param onError Callback invoked when an error occurs during capture.
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: (Throwable) -> Unit
    )

    /**
     * Stops the ongoing listening session.
     */
    fun stopListening()

    /**
     * Current listening state.
     */
    val isListening: Boolean
}
