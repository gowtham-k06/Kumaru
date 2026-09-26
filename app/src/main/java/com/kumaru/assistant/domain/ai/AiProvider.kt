package com.kumaru.assistant.domain.ai

/**
 * Abstraction for AI reasoning and generative responses.
 * Designed to cleanly swap between development mocks and external providers (e.g. Google Gemini).
 */
interface AiProvider {
    /**
     * Generates a conversational or action-oriented response for the given user input.
     *
     * @param input The text transcribed from user input or push-to-talk.
     * @return Generated assistant response text.
     */
    suspend fun generateResponse(input: String): String

    /**
     * Generates a response with dynamic, context-specific system instruction.
     *
     * @param input The text transcribed from user input.
     * @param systemContext Compact tailored context constructed by KumaruEngine.
     * @return Generated assistant response text.
     */
    suspend fun generateResponseWithContext(input: String, systemContext: String): String = generateResponse(input)
}
