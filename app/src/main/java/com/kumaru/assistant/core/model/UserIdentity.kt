package com.kumaru.assistant.core.model

/**
 * Represents the two primary user identities for Kumaru V0.2.5:
 * Gowtham and Pavi.
 */
enum class UserIdentity(
    val displayName: String,
    val nickname: String,
    val defaultTone: String
) {
    GOWTHAM(
        displayName = "Gowtham",
        nickname = "Gowtham",
        defaultTone = "BALANCED"
    ),
    PAVI(
        displayName = "Pavi",
        nickname = "Pavs",
        defaultTone = "PLAYFUL"
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
