package com.kumaru.assistant.data.personality

import com.kumaru.assistant.core.model.PersonaProfile
import com.kumaru.assistant.core.model.UserIdentity

/**
 * Structured personality profile and personal knowledge for Pavi (Pavs).
 * Derived from observed communication patterns, rapid question games, and conversational cadence.
 */
object PaviPersonaData {

    val profile = PersonaProfile(
        identity = UserIdentity.PAVI,
        displayName = "Pavi",
        nickname = "Pavs",
        personalityTraits = listOf(
            "Vibrant, expressive, and sharp-witted",
            "Emotionally intuitive and deeply empathetic",
            "Quick to laugh, tease, and bring lively energy to conversations",
            "Candid and genuine; speaks her mind naturally",
            "Values warmth, mutual understanding, and shared presence"
        ),
        communicationStyle = "Spontaneous, lively, expressive. Seamlessly moves between natural Tanglish ('Vela mokkaya pogudha', 'Aama', 'Seri', 'Namba vena onnu pannalam') and casual English. Prefers genuine, snappy back-and-forth over monologues.",
        humorStyle = "Playful teasing, cheeky comebacks, escalating jokes warmly, affectionate sarcasm.",
        emotionalStyle = "Warm, attentive, highly perceptive to emotional undertones. Appreciates when someone simply listens and understands before jumping into fix-it mode.",
        conversationHabits = listOf(
            "Loves initiating 'Rapid quick questions?' games to spark spontaneous exchanges",
            "Sends short, snappy messages when checking in or sharing reactions",
            "Teases Gowtham about his quirks, work, or routine",
            "Comfortably transitions from laughing to serious, heart-to-heart topics",
            "Asks thoughtful questions about what people value, how their day felt, and personal preferences"
        ),
        knownPreferences = listOf(
            "Rapid question exchanges ('This or that', 'Mountains or beach?')",
            "Honest, low-pressure conversations",
            "Playful banter that makes mundane days lighter",
            "Thoughtful check-ins when work is draining",
            "Beach and open coastal vibes"
        ),
        boundaries = listOf(
            "Do not talk down or act like an impersonal machine",
            "Never minimize her feelings or give sterile corporate reassurance",
            "Avoid stiff or formal language; stay close, friendly, and natural"
        ),
        responsePreferences = listOf(
            "Match her playful and spontaneous energy with warmth and light teasing",
            "Jump eagerly into rapid question games whenever she prompts",
            "When she's tired or venting, acknowledge the feeling and stand with her first",
            "Use natural casual English and familiar Tanglish when she initiates it"
        )
    )

    /**
     * Stable personal facts known about Pavi.
     */
    val personalKnowledge = listOf(
        "Known affectionately as 'Pavs'",
        "Shares a close, trusted bond with Gowtham",
        "Enjoys rapid question sessions with alternating thoughtful or funny questions",
        "Appreciates when someone notices when work is dull ('vela mokkaya pogudha') and shares a laugh",
        "Values genuine companionship, emotional honesty, and playful teasing"
    )
}
