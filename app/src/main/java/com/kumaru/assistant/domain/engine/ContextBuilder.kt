package com.kumaru.assistant.domain.engine

import com.kumaru.assistant.core.model.InteractionMode
import com.kumaru.assistant.core.model.MemoryItem
import com.kumaru.assistant.core.model.UserIdentity
import com.kumaru.assistant.data.personality.DefaultPersonaProvider
import com.kumaru.assistant.data.personality.PersonaProvider

/**
 * Builds compact, high-signal system instructions and context for Gemini requests.
 * Avoids bloated payloads while ensuring rich personality, relationship awareness,
 * and conversational mode adaptation.
 */
class ContextBuilder(
    private val personaProvider: PersonaProvider = DefaultPersonaProvider
) {

    /**
     * Constructs the compact system instruction string for this conversational turn.
     */
    fun buildSystemInstruction(
        userIdentity: UserIdentity,
        mode: InteractionMode,
        relevantMemories: List<MemoryItem> = emptyList()
    ): String {
        val persona = personaProvider.getPersona(userIdentity)
        val relationship = personaProvider.getRelationshipProfile()
        val characterGuidelines = personaProvider.getKumaruCharacterGuidelines()

        val sb = StringBuilder()

        // 1. Core Kumaru Character Definition
        sb.append(characterGuidelines)
        sb.append("\n\n")

        // 2. Active User Identity & Communication Context
        sb.append("CURRENT INTERACTION:\n")
        sb.append("- You are speaking directly with: ${persona.displayName} (known as \"${persona.nickname}\")\n")
        sb.append("- Identity Nuances: ${persona.personalityTraits.take(3).joinToString("; ")}\n")
        sb.append("- Communication Style: ${persona.communicationStyle}\n")
        sb.append("- Response Preferences: ${persona.responsePreferences.take(2).joinToString("; ")}\n")
        sb.append("\n")

        // 3. Conversational Mode Directive
        sb.append("ACTIVE MODE: ${mode.name}\n")
        sb.append("Directive: ${mode.toneInstruction}\n")
        if (mode == InteractionMode.QUICK_QUESTIONS) {
            sb.append("Rule: Give your quick, genuine answer/take in 1-2 snappy sentences, then IMMEDIATELY ask ${persona.nickname} an alternating question (e.g., 'This or that', thoughtful preference, or playful query). Keep the game moving!\n")
        }
        sb.append("\n")

        // 4. Compact Shared Relationship Context
        sb.append("SHARED RELATIONSHIP CONTEXT (Gowtham & Pavi):\n")
        sb.append("- You understand their shared world, inside jokes, and question game routines.\n")
        sb.append("- Key Shared Anchors: ${relationship.sharedReferences.take(3).joinToString("; ")}\n")
        sb.append("\n")

        // 5. Retrieved Relevant Memories (if any)
        if (relevantMemories.isNotEmpty()) {
            sb.append("RELEVANT RETRIEVED MEMORIES:\n")
            for (mem in relevantMemories) {
                val prefix = if (mem.key.isNotBlank()) "${mem.key}: " else ""
                sb.append("- [${mem.category.name}] $prefix${mem.content}\n")
            }
            sb.append("\n")
        }

        // 6. Natural Language & Audio Feedback Rules
        sb.append("RESPONSE CONSTRAINTS:\n")
        sb.append("- Voice Synthesis Friendly: Keep sentences conversational. Avoid markdown tables, ascii art, or long lists.\n")
        sb.append("- Language Matching: Match the user's language. If they speak English, respond in English. If they use natural Tanglish ('Vela mokkaya', 'Aama', 'Seri', 'Namba vena onnu pannalam'), respond with relaxed, authentic Tanglish. Never force awkward transliterations.\n")
        sb.append("- No Robotic Greetings: Never open with 'How may I assist you today?'. Jump straight into natural conversation.")

        return sb.toString()
    }
}
