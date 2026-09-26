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
import com.kumaru.assistant.presentation.memory.MemoryScreen
import com.kumaru.assistant.presentation.onboarding.OnboardingScreen
import com.kumaru.assistant.presentation.profile.ProfileScreen
import com.kumaru.assistant.presentation.ui.AssistantScreen
import com.kumaru.assistant.presentation.viewmodel.AssistantViewModel

enum class KumaruScreen {
    ONBOARDING,
    MAIN_ASSISTANT,
    PROFILE,
    HISTORY,
    EDIT_SETUP,
    MEMORIES
}

/**
 * Root Navigation Host coordinating state-driven transitions between
 * Onboarding, Main Assistant, Profile, History, Edit Setup, and Memory Vault.
 */
@Composable
fun KumaruNavHost(
    viewModel: AssistantViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val conversationSessions by viewModel.conversationSessions.collectAsStateWithLifecycle()
    val allMemories by viewModel.allMemories.collectAsStateWithLifecycle()

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
                    startInSetup = false,
                    onOnboardingFinished = { completedProfile ->
                        viewModel.saveUserProfile(completedProfile)
                        viewModel.setOnboardingCompleted(true)
                        currentScreen = KumaruScreen.MAIN_ASSISTANT
                    }
                )
            }

            KumaruScreen.EDIT_SETUP -> {
                OnboardingScreen(
                    initialProfile = userProfile,
                    startInSetup = true,
                    onCancelEdit = { currentScreen = KumaruScreen.PROFILE },
                    onOnboardingFinished = { updatedProfile ->
                        viewModel.saveUserProfile(updatedProfile)
                        currentScreen = KumaruScreen.PROFILE
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
                    onEditProfile = { currentScreen = KumaruScreen.EDIT_SETUP },
                    onReplayOnboarding = { currentScreen = KumaruScreen.ONBOARDING },
                    onOpenMemories = { currentScreen = KumaruScreen.MEMORIES },
                    onSwitchIdentity = { identity ->
                        viewModel.switchUserIdentity(identity)
                    },
                    onClearHistory = {
                        viewModel.resetConversation()
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

            KumaruScreen.MEMORIES -> {
                MemoryScreen(
                    memories = allMemories,
                    onBack = { currentScreen = KumaruScreen.PROFILE },
                    onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                    onPinMemory = { id, pinned -> viewModel.pinMemory(id, pinned) },
                    onUpdateMemory = { item -> viewModel.updateMemory(item) },
                    onAddMemory = { item -> viewModel.addMemory(item) }
                )
            }
        }
    }
}
