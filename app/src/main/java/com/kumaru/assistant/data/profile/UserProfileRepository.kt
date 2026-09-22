package com.kumaru.assistant.data.profile

import android.content.Context
import android.content.SharedPreferences
import com.kumaru.assistant.core.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local-first repository for User Profile and Onboarding state.
 * Backed by SharedPreferences for instantaneous, offline persistence.
 */
class UserProfileRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "kumaru_user_profile"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_COMMUNICATION_STYLE = "communication_style"
        private const val KEY_PRIMARY_INTERESTS = "primary_interests"
        private const val KEY_PERSONAL_CONTEXT = "personal_context"
        private const val KEY_MEMORY_PREFERENCE = "memory_preference"

        @Volatile
        private var INSTANCE: UserProfileRepository? = null

        fun getInstance(context: Context): UserProfileRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserProfileRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun loadProfile(): UserProfile {
        val completed = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val style = prefs.getString(KEY_COMMUNICATION_STYLE, "CASUAL") ?: "CASUAL"
        val interests = prefs.getStringSet(KEY_PRIMARY_INTERESTS, emptySet()) ?: emptySet()
        val context = prefs.getString(KEY_PERSONAL_CONTEXT, "") ?: ""
        val memory = prefs.getString(KEY_MEMORY_PREFERENCE, "USEFUL") ?: "USEFUL"

        return UserProfile(
            isOnboardingCompleted = completed,
            userName = name,
            communicationStyle = style,
            primaryInterests = interests,
            personalContext = context,
            memoryPreference = memory
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putBoolean(KEY_ONBOARDING_COMPLETED, profile.isOnboardingCompleted)
            putString(KEY_USER_NAME, profile.userName)
            putString(KEY_COMMUNICATION_STYLE, profile.communicationStyle)
            putStringSet(KEY_PRIMARY_INTERESTS, profile.primaryInterests)
            putString(KEY_PERSONAL_CONTEXT, profile.personalContext)
            putString(KEY_MEMORY_PREFERENCE, profile.memoryPreference)
            apply()
        }
        _userProfile.value = profile
    }

    fun setOnboardingCompleted(completed: Boolean) {
        val current = _userProfile.value
        val updated = current.copy(isOnboardingCompleted = completed)
        saveProfile(updated)
    }

    /**
     * Generates a concise personalization prompt context to enrich Gemini's system instruction.
     */
    fun getPersonalizationPromptContext(): String {
        val profile = _userProfile.value
        if (!profile.isOnboardingCompleted && profile.userName.isBlank()) {
            return ""
        }

        val sb = StringBuilder()
        sb.append("\n\nUser Profile & Personal Preferences:")
        if (profile.userName.isNotBlank()) {
            sb.append("\n- User Name: ${profile.userName}")
        }
        val styleDesc = when (profile.communicationStyle) {
            "CASUAL" -> "Casual, friendly, like a companion"
            "FOCUSED" -> "Concise, direct, straight to the point"
            "PLAYFUL" -> "Warm, slightly playful with wit"
            "BALANCED" -> "Balanced and adaptable to context"
            else -> profile.communicationStyle
        }
        sb.append("\n- Preferred Tone: $styleDesc")

        if (profile.primaryInterests.isNotEmpty()) {
            sb.append("\n- Primary Focus: ${profile.primaryInterests.joinToString(", ")}")
        }
        if (profile.personalContext.isNotBlank()) {
            sb.append("\n- User Context: ${profile.personalContext}")
        }
        return sb.toString()
    }
}
