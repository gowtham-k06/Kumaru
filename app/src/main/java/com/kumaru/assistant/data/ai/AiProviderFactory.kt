package com.kumaru.assistant.data.ai

import android.util.Log
import com.kumaru.assistant.BuildConfig
import com.kumaru.assistant.domain.ai.AiProvider
import com.kumaru.assistant.domain.memory.MemoryStore

/**
 * Factory responsible for instantiating the active [AiProvider].
 * Provides a clean abstraction to switch between [GeminiAiProvider] and [MockAiProvider]
 * without requiring changes to [com.kumaru.assistant.presentation.viewmodel.AssistantViewModel].
 */
object AiProviderFactory {

    private const val TAG = "AiProviderFactory"

    /**
     * Creates an [AiProvider] instance.
     *
     * @param memoryStore Session memory store to pass to [GeminiAiProvider] for multi-turn context.
     * @param forceMock If true, forces [MockAiProvider] even when a valid API key is present.
     * @param apiKey The Gemini API key (defaults to [BuildConfig.GEMINI_API_KEY]).
     * @param model The Gemini model identifier (defaults to [BuildConfig.GEMINI_MODEL]).
     */
    fun create(
        memoryStore: MemoryStore? = null,
        forceMock: Boolean = false,
        apiKey: String = BuildConfig.GEMINI_API_KEY,
        model: String = BuildConfig.GEMINI_MODEL,
        userContextProvider: (() -> String)? = null
    ): AiProvider {
        Log.i(TAG, "KUMARU FACTORY MODEL = $model")
        return if (!forceMock && apiKey.isNotBlank()) {
            GeminiAiProvider(
                apiKey = apiKey,
                model = model,
                memoryStore = memoryStore,
                userContextProvider = userContextProvider
            )
        } else {
            MockAiProvider()
        }
    }
}
