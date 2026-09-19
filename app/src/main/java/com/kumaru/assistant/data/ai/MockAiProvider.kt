package com.kumaru.assistant.data.ai

import com.kumaru.assistant.domain.ai.AiProvider
import kotlinx.coroutines.delay

/**
 * Development & mock implementation of [AiProvider].
 * Provides realistic responses for Kumaru V0.1 without requiring an API key.
 *
 * This will be complemented or replaced by the Gemini API integration in stage V0.2.
 */
class MockAiProvider : AiProvider {

    override suspend fun generateResponse(input: String): String {
        // Simulate real network / inference latency
        delay(950)

        val cleanInput = input.trim().lowercase()

        return when {
            cleanInput.isEmpty() -> {
                "I'm listening. Tell me what's on your mind or how I can help."
            }
            cleanInput.contains("who are you") || cleanInput.contains("what is your name") -> {
                "I am Kumaru, your personal Android AI assistant. We are currently running on the V0.1 native foundation."
            }
            cleanInput.contains("enna kumaru") -> {
                "Sollunga! I am active and ready. Push-to-talk is online for V0.1, and wake-word detection is coming in a future update."
            }
            cleanInput.contains("hello") || cleanInput.contains("hi") || cleanInput.contains("hey") -> {
                "Hello! Kumaru V0.1 is running smoothly. How can I assist you today?"
            }
            cleanInput.contains("what can you do") || cleanInput.contains("capabilities") || cleanInput.contains("help") -> {
                "Right now in V0.1, I can process push-to-talk voice interactions and maintain our conversation state. Soon I'll be powered by the Gemini API, phone tools, memory, and automated device actions."
            }
            cleanInput.contains("gemini") -> {
                "Gemini API integration is scheduled for stage V0.2! The architecture is already prepped for it."
            }
            cleanInput.contains("time") -> {
                val now = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                "The current time is $now."
            }
            else -> {
                "Understood: \"$input\". Kumaru V0.1 processed your query successfully. Real generative intelligence will be connected via the Gemini API in the next phase."
            }
        }
    }
}
