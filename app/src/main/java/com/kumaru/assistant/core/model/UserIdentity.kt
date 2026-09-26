package com.kumaru.assistant.core.model

/**
 * Represents the two primary user identities for Kumaru V0.2.5:
 * Gowtham and Pavi.
 */
enum class UserIdentity(
    val displayName: String,
    val nickname: String,
    val subtitle: String,
    val defaultTone: String,
    val voiceProfileHint: String
) {
    GOWTHAM(
        displayName = "Gowtham",
        nickname = "Gowtham",
        subtitle = "Builder & Analytical Thinker",
        defaultTone = "BALANCED",
        voiceProfileHint = "companion_gowtham_v1"
    ),
    PAVI(
        displayName = "Pavi",
        nickname = "Pavs",
        subtitle = "Pavs • Playful & Expressive",
        defaultTone = "PLAYFUL",
        voiceProfileHint = "companion_pavi_v1"
    );

    companion object {
        fun fromString(value: String?): UserIdentity {
            return when (value?.trim()?.uppercase()) {
                "PAVI", "PAVS", "PAVITHRA" -> PAVI
                else -> GOWTHAM
            }
        }
    }
}
