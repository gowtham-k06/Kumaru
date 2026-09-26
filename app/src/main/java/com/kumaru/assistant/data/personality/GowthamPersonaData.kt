package com.kumaru.assistant.data.personality

import com.kumaru.assistant.core.model.PersonaProfile
import com.kumaru.assistant.core.model.UserIdentity

/**
 * Structured personality profile and personal knowledge for Gowtham.
 * Derived from observed communication style, conversation habits, and preferences.
 */
object GowthamPersonaData {

    val profile = PersonaProfile(
        identity = UserIdentity.GOWTHAM,
        displayName = "Gowtham",
        nickname = "Gowtham",
        personalityTraits = listOf(
            "Reflective and analytical builder",
            "Calm, observant, and deeply curious",
            "Values substance and authenticity over small talk",
            "Night owl who does deep creative and technical work late",
            "Dry, understated sense of humor with playful teasing"
        ),
        communicationStyle = "Direct, thoughtful, concise. Blends natural casual English with relaxed Tanglish. Gets to the core without unnecessary fluff.",
        humorStyle = "Subtle, ironic, witty deadpan banter; loves turning unexpected observations into ongoing jokes.",
        emotionalStyle = "Grounded, introspective, steady. Demonstrates care through genuine attention, deep listening, and quiet presence rather than melodrama.",
        conversationHabits = listOf(
            "Often initiates or engages in rapid-fire question exchanges",
            "Enjoys philosophical questions and 'this or that' comparisons",
            "Shares late-night reflections when winding down",
            "Prefers thoughtful follow-up questions before jumping into problem-solving",
            "Responds well to clever banter and lighthearted teasing"
        ),
        knownPreferences = listOf(
            "Software architecture and AI development",
            "Deep focus sessions and building useful products",
            "Late-night discussions",
            "Mountains over beaches (prefers calm, elevated scenery)",
            "Honest, direct conversations over performative cheerfulness"
        ),
        boundaries = listOf(
            "Never use patronizing or corporate customer-service language",
            "Avoid lecturing or lecturing on obvious common sense",
            "Do not force unsolicited life advice when he is just sharing a thought"
        ),
        responsePreferences = listOf(
            "Keep answers concise, crisp, and conversational",
            "Be ready to engage in rapid question games when initiated",
            "Tease gently when he's overanalyzing",
            "Provide calm, thoughtful presence during late hours"
        )
    )

    /**
     * Stable personal facts known about Gowtham.
     */
    val personalKnowledge = listOf(
        "Software engineer and builder creating the Kumaru platform",
        "Close friend and confidant to Pavi (Pavs)",
        "Tends to work late into the night and get lost in technical challenges",
        "Values genuine depth, intelligence, and humor in people",
        "Prefers natural Tanglish when in casual mood, English for conceptual discussions"
    )
}
