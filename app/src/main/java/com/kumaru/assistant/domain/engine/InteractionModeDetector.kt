package com.kumaru.assistant.domain.engine

import com.kumaru.assistant.core.model.ConversationMessage
import com.kumaru.assistant.core.model.InteractionMode
import com.kumaru.assistant.core.model.MessageRole
import java.util.Calendar

/**
 * Lightweight, context-aware detector for conversational interaction modes.
 * Uses message content, conversational history, and time-of-day cues.
 */
class InteractionModeDetector {

    /**
     * Determines the optimal [InteractionMode] for the current turn.
     *
     * @param input Current input from the user.
     * @param history Recent conversation messages for conversational context.
     * @param currentHour Optional hour of day (0..23) for time-based nuances; defaults to local system time.
     */
    fun detectMode(
        input: String,
        history: List<ConversationMessage> = emptyList(),
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): InteractionMode {
        val normalized = input.trim().lowercase()

        // 1. QUICK_QUESTIONS: Explicit game starters or binary rapid question formats
        if (isQuickQuestionPrompt(normalized, history)) {
            return InteractionMode.QUICK_QUESTIONS
        }

        // 2. TASK_FOCUSED: Alarms, timers, weather, device tools, or specific utility requests
        if (isTaskFocused(normalized)) {
            return InteractionMode.TASK_FOCUSED
        }

        // 3. SUPPORTIVE: Expressions of exhaustion, stress, emotional weight, or venting
        if (isSupportiveTrigger(normalized)) {
            return InteractionMode.SUPPORTIVE
        }

        // 4. SERIOUS: Deep questions, life reflections, emotional disclosures
        if (isSeriousTrigger(normalized, history)) {
            return InteractionMode.SERIOUS
        }

        // 5. LATE_NIGHT: Between 11 PM (23:00) and 5 AM (05:00) with reflective or conversational context
        val isLateNightHour = currentHour >= 23 || currentHour < 5
        if (isLateNightHour && (normalized.length > 15 || isReflective(normalized) || history.isNotEmpty())) {
            return InteractionMode.LATE_NIGHT
        }

        // 6. PLAYFUL: Teasing, boredom, banter, laughter cues
        if (isPlayfulTrigger(normalized, history)) {
            return InteractionMode.PLAYFUL
        }

        // 7. Default to CASUAL
        return InteractionMode.CASUAL
    }

    private fun isQuickQuestionPrompt(text: String, history: List<ConversationMessage>): Boolean {
        if (text.contains("rapid") || text.contains("quick question") || text.contains("this or that")) {
            return true
        }

        // Binary choices pattern: "X or Y" (e.g. "mountains or beach", "coffee or chai", "night or day")
        val binaryChoiceRegex = Regex("""^(\w[\w\s]{1,20})\s+or\s+(\w[\w\s]{1,20})\??$""", RegexOption.IGNORE_CASE)
        if (binaryChoiceRegex.matches(text)) {
            return true
        }

        // If the last assistant message ended with a question and we are in an ongoing question game
        val lastAssistantMessage = history.lastOrNull { it.role == MessageRole.KUMARU }?.text?.lowercase() ?: ""
        if (lastAssistantMessage.contains("?") && (
                lastAssistantMessage.contains("your turn") ||
                lastAssistantMessage.contains("or") ||
                lastAssistantMessage.contains("what about you") ||
                lastAssistantMessage.contains("rapid")
            )) {
            // User is answering our previous rapid question and may ask one back
            return true
        }

        return false
    }

    private fun isTaskFocused(text: String): Boolean {
        val taskKeywords = listOf(
            "set an alarm", "set alarm", "alarm for", "wake me up",
            "set a reminder", "create reminder", "remind me",
            "open app", "launch app", "search for", "google for",
            "what's the weather", "current weather", "temperature in"
        )
        return taskKeywords.any { text.contains(it) }
    }

    private fun isSupportiveTrigger(text: String): Boolean {
        val supportiveKeywords = listOf(
            "exhausted", "so tired", "bad day", "rough day", "stressed",
            "vela mokkaya", "drained", "feeling low", "sad", "overwhelmed",
            "cant focus", "can't focus", "headache", "demotivated", "burnout"
        )
        return supportiveKeywords.any { text.contains(it) }
    }

    private fun isSeriousTrigger(text: String, history: List<ConversationMessage>): Boolean {
        val seriousKeywords = listOf(
            "honestly", "to be honest", "struggling with", "scared of",
            "what do you think about life", "future", "career decision",
            "deep question", "meaning of", "regret", "anxious about"
        )
        if (seriousKeywords.any { text.contains(it) }) return true

        // Substantial paragraph length and contemplative tone
        return text.split(" ").size > 30 && !text.contains("haha") && !text.contains("lol")
    }

    private fun isPlayfulTrigger(text: String, history: List<ConversationMessage>): Boolean {
        val playfulKeywords = listOf(
            "bored", "bore adikkuthu", "poda", "cheeky", "haha", "lol",
            "tease", "roast me", "namba vena onnu pannalam", "shut up", "silly",
            "kidding", "joke"
        )
        return playfulKeywords.any { text.contains(it) }
    }

    private fun isReflective(text: String): Boolean {
        val reflectiveKeywords = listOf(
            "quiet", "thinking about", "cant sleep", "can't sleep",
            "night", "peaceful", "still awake", "late"
        )
        return reflectiveKeywords.any { text.contains(it) }
    }
}
