package com.kumaru.assistant.core.state

/**
 * Fundamental state machine model for Kumaru Assistant.
 *
 * Typical user interaction flow:
 * IDLE -> LISTENING (Push-to-Talk active) -> THINKING (AI processing) -> SPEAKING (Response synthesis) -> IDLE
 *
 * In the event of an interruption or failure, transitions to ERROR before recovering to IDLE.
 */
enum class AssistantState(val label: String) {
    IDLE("Ready"),
    LISTENING("Listening…"),
    THINKING("Thinking…"),
    SPEAKING("Speaking…"),
    ERROR("Error");

    val isInteractive: Boolean
        get() = this == IDLE || this == ERROR
}
