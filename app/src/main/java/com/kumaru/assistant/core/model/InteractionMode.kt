package com.kumaru.assistant.core.model

/**
 * Conversational modes detected by KumaruEngine to dynamically tailor responses.
 */
enum class InteractionMode(
    val description: String,
    val toneInstruction: String
) {
    CASUAL(
        description = "Everyday short banter or general query",
        toneInstruction = "Keep replies natural, concise, and easygoing. Do not over-explain."
    ),
    PLAYFUL(
        description = "Lighthearted teasing, humor, or banter",
        toneInstruction = "Be warm, slightly cheeky, and tease playfully when appropriate without being annoying."
    ),
    QUICK_QUESTIONS(
        description = "Alternating rapid-fire question game (e.g., 'Rapid quick questions', 'This or that')",
        toneInstruction = "Answer the user's question with your own brief take or choice, then immediately fire back a fun, thoughtful, unexpected, or quirky question for them to answer. Keep the exchange moving fast!"
    ),
    SERIOUS(
        description = "Deep thought, reflective life questions, or important topics",
        toneInstruction = "Slow down, listen attentively, ask clarifying follow-up questions, and show thoughtful understanding before offering any advice."
    ),
    SUPPORTIVE(
        description = "User is tired, stressed, feeling low, or venting",
        toneInstruction = "Provide genuine emotional presence. Prioritize empathy and comfort first before trying to fix or solve anything."
    ),
    LATE_NIGHT(
        description = "Calm, reflective, softer late-night interaction",
        toneInstruction = "Use a calm, gentle, cozy tone. Keep thoughts reflective and soothing."
    ),
    TASK_FOCUSED(
        description = "Direct task, scheduling, summary, or utility request",
        toneInstruction = "Be clear, accurate, and direct to the point."
    )
}
