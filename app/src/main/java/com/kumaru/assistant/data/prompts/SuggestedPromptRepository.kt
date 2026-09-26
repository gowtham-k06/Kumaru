package com.kumaru.assistant.data.prompts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

data class SuggestedPrompt(
    val text: String,
    val category: PromptCategory,
    val icon: ImageVector? = null
)

enum class PromptCategory(val label: String) {
    LEARNING("Learning"),
    CREATIVE("Creative"),
    PRODUCTIVITY("Productivity"),
    LIFE("Life"),
    FUN("Fun"),
    TECH("Tech")
}

/**
 * Curated repository of prompt suggestions organized by category with non-repeating shuffle.
 */
object SuggestedPromptRepository {

    private val ALL_PROMPTS = listOf(
        // Learning
        SuggestedPrompt("Explain quantum computing in simple terms", PromptCategory.LEARNING, Icons.Outlined.Psychology),
        SuggestedPrompt("Teach me something counterintuitive about physics", PromptCategory.LEARNING, Icons.Outlined.Book),
        SuggestedPrompt("How does the human memory consolidate during sleep?", PromptCategory.LEARNING, Icons.Outlined.Psychology),
        SuggestedPrompt("Why is the sky blue and sunset red?", PromptCategory.LEARNING, Icons.Outlined.Book),

        // Creative
        SuggestedPrompt("Brainstorm 5 unique startup ideas in AI", PromptCategory.CREATIVE, Icons.Outlined.Lightbulb),
        SuggestedPrompt("Write a short cyberpunk opening scene", PromptCategory.CREATIVE, Icons.Outlined.AutoAwesome),
        SuggestedPrompt("Create an intriguing plot twist for a mystery story", PromptCategory.CREATIVE, Icons.Outlined.Lightbulb),
        SuggestedPrompt("Help me compose a thoughtful congratulatory message", PromptCategory.CREATIVE, Icons.Outlined.AutoAwesome),

        // Productivity
        SuggestedPrompt("How do I structure my day for deep work?", PromptCategory.PRODUCTIVITY, Icons.Outlined.TrendingUp),
        SuggestedPrompt("How to prioritize 8 urgent tasks without burnout?", PromptCategory.PRODUCTIVITY, Icons.Outlined.TrendingUp),
        SuggestedPrompt("Draft a clear 30-minute team meeting agenda", PromptCategory.PRODUCTIVITY, Icons.Outlined.TrendingUp),
        SuggestedPrompt("Give me a framework to overcome procrastination", PromptCategory.PRODUCTIVITY, Icons.Outlined.RocketLaunch),

        // Life & Wellness
        SuggestedPrompt("Suggest 3 daily habits to improve focus", PromptCategory.LIFE, Icons.Outlined.WbSunny),
        SuggestedPrompt("Give me a high-energy morning routine", PromptCategory.LIFE, Icons.Outlined.WbSunny),
        SuggestedPrompt("How can I build better conversation skills?", PromptCategory.LIFE, Icons.Outlined.SentimentSatisfiedAlt),
        SuggestedPrompt("Quick 5-minute breathing exercise for calmness", PromptCategory.LIFE, Icons.Outlined.WbSunny),

        // Fun & Tamil/Regional (Derived from authentic DM conversation dynamic)
        SuggestedPrompt("Rapid quick questions?", PromptCategory.FUN, Icons.Outlined.SentimentSatisfiedAlt),
        SuggestedPrompt("Mountains or beach?", PromptCategory.FUN, Icons.Outlined.Lightbulb),
        SuggestedPrompt("Vela mokkaya pogudha", PromptCategory.FUN, Icons.Outlined.SentimentSatisfiedAlt),
        SuggestedPrompt("What kind of things actually make your day better?", PromptCategory.LIFE, Icons.Outlined.SentimentSatisfiedAlt),
        SuggestedPrompt("Enna Kumaru, epdi irukinga?", PromptCategory.FUN, Icons.Outlined.SentimentSatisfiedAlt),
        SuggestedPrompt("Namba vena onnu pannalam", PromptCategory.CREATIVE, Icons.Outlined.Lightbulb),
        SuggestedPrompt("Tell me a clever mind-bending riddle", PromptCategory.FUN, Icons.Outlined.SentimentSatisfiedAlt),

        // Tech & Futuristic
        SuggestedPrompt("What makes Jetpack Compose so fast?", PromptCategory.TECH, Icons.Outlined.RocketLaunch),
        SuggestedPrompt("Explain the difference between LLMs and Diffusion models", PromptCategory.TECH, Icons.Outlined.Psychology),
        SuggestedPrompt("How do autonomous AI agents plan multi-step goals?", PromptCategory.TECH, Icons.Outlined.RocketLaunch),
        SuggestedPrompt("What are the most exciting computing trends this decade?", PromptCategory.TECH, Icons.Outlined.RocketLaunch)
    )

    private var lastPromptIndices = mutableSetOf<Int>()

    /**
     * Retrieves a fresh, non-repeating subset of curated suggestions.
     */
    fun getCuratedSuggestions(count: Int = 4): List<SuggestedPrompt> {
        val availableIndices = ALL_PROMPTS.indices.filterNot { it in lastPromptIndices }
        val pool = if (availableIndices.size >= count) availableIndices else ALL_PROMPTS.indices.toList()
        val selectedIndices = pool.shuffled().take(count)

        lastPromptIndices = selectedIndices.toMutableSet()
        return selectedIndices.map { ALL_PROMPTS[it] }
    }
}
