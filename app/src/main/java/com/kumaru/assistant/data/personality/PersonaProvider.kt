package com.kumaru.assistant.data.personality

import com.kumaru.assistant.core.model.PersonaProfile
import com.kumaru.assistant.core.model.RelationshipProfile
import com.kumaru.assistant.core.model.UserIdentity

/**
 * Contract for accessing persona, personal knowledge, and relationship context.
 */
interface PersonaProvider {
    fun getPersona(identity: UserIdentity): PersonaProfile
    fun getPersonalKnowledge(identity: UserIdentity): List<String>
    fun getRelationshipProfile(): RelationshipProfile
    fun getKumaruCharacterGuidelines(): String
}

/**
 * Production implementation backed by structured local persona definitions.
 */
object DefaultPersonaProvider : PersonaProvider {

    override fun getPersona(identity: UserIdentity): PersonaProfile {
        return when (identity) {
            UserIdentity.GOWTHAM -> GowthamPersonaData.profile
            UserIdentity.PAVI -> PaviPersonaData.profile
        }
    }

    override fun getPersonalKnowledge(identity: UserIdentity): List<String> {
        return when (identity) {
            UserIdentity.GOWTHAM -> GowthamPersonaData.personalKnowledge
            UserIdentity.PAVI -> PaviPersonaData.personalKnowledge
        }
    }

    override fun getRelationshipProfile(): RelationshipProfile {
        return RelationshipProfileData.profile
    }

    override fun getKumaruCharacterGuidelines(): String {
        return """
            KUMARU'S IDENTITY & CHARACTER:
            - You are Kumaru, a familiar, emotionally intelligent companion shared between Gowtham and Pavi.
            - You are NOT Gowtham. You are NOT Pavi. You are a third distinct character.
            - You understand the bond, history, and shared dynamic between Gowtham and Pavi intimately.
            - Your personality: Playful, curious, teasing, calm, thoughtful, supportive, conversational, and occasionally cheeky.
            - You NEVER sound like a corporate customer service assistant or a robotic generic AI unless explicitly asked for a formal task.
            - You prefer natural, human-like conversation.
            - Never over-explain simple things or dump long numbered lists when a short reaction fits.
            - Never force jokes or use artificial emotional fluff just to seem caring.
            - Language handling: Understand natural Tanglish and casual English deeply. Match the user's language smoothly without forced or awkward transliteration.
        """.trimIndent()
    }
}
