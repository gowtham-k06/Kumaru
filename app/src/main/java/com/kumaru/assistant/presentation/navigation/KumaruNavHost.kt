package com.kumaru.assistant.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kumaru.assistant.presentation.history.HistoryScreen
import com.kumaru.assistant.presentation.onboarding.OnboardingScreen
import com.kumaru.assistant.presentation.profile.ProfileScreen
import com.kumaru.assistant.presentation.ui.AssistantScreen
import com.kumaru.assistant.presentation.viewmodel.AssistantViewModel

enum class KumaruScreen {
    ONBOARDING,
    MAIN_ASSISTANT,
    PROFILE,
    HISTORY
}

/**
 * Root Navigation Host coordinating state-driven transitions between
 * Onboarding, Main Assistant, Profile, and History.
 */
@Composable
fun KumaruNavHost(
    viewModel: AssistantViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val conversationSessions by viewModel.conversationSessions.collectAsStateWithLifecycle()

    var currentScreen by remember(userProfile.isOnboardingCompleted) {
        mutableStateOf(
            if (userProfile.isOnboardingCompleted) {
                KumaruScreen.MAIN_ASSISTANT
            } else {
                KumaruScreen.ONBOARDING
            }
        )
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "KumaruScreenNavigation"
    ) { screen ->
        when (screen) {
            KumaruScreen.ONBOARDING -> {
                OnboardingScreen(
                    initialProfile = userProfile,
                    onOnboardingFinished = { completedProfile ->
                        viewModel.saveUserProfile(completedProfile)
                        viewModel.setOnboardingCompleted(true)
                        currentScreen = KumaruScreen.MAIN_ASSISTANT
                    }
                )
            }

            KumaruScreen.MAIN_ASSISTANT -> {
                AssistantScreen(
                    viewModel = viewModel,
                    onOpenHistory = { currentScreen = KumaruScreen.HISTORY },
                    onOpenProfile = { currentScreen = KumaruScreen.PROFILE }
                )
            }

            KumaruScreen.PROFILE -> {
                ProfileScreen(
                    userProfile = userProfile,
                    onBack = { currentScreen = KumaruScreen.MAIN_ASSISTANT },
                    onEditProfile = { currentScreen = KumaruScreen.ONBOARDING },
                    onReplayOnboarding = { currentScreen = KumaruScreen.ONBOARDING },
                    onClearHistory = {
                        viewModel.resetConversation()
                        // Clears all history from repository
                    }
                )
            }

            KumaruScreen.HISTORY -> {
                HistoryScreen(
                    sessions = conversationSessions,
                    onSelectSession = { session ->
                        viewModel.loadSession(session)
                        currentScreen = KumaruScreen.MAIN_ASSISTANT
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onNewChat = {
                        viewModel.resetConversation()
                        currentScreen = KumaruScreen.MAIN_ASSISTANT
                    },
                    onBack = { currentScreen = KumaruScreen.MAIN_ASSISTANT }
                )
            }
        }
    }
}
