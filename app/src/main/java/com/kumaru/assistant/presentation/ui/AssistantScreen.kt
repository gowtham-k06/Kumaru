package com.kumaru.assistant.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.components.AssistantHeader
import com.kumaru.assistant.presentation.components.ChatInputField
import com.kumaru.assistant.presentation.components.KumaruBackground
import com.kumaru.assistant.presentation.components.KumaruOrb
import com.kumaru.assistant.presentation.components.TranscriptView
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.StateError
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary
import com.kumaru.assistant.presentation.viewmodel.AssistantViewModel

/**
 * Main Assistant Screen for Kumaru V0.2.3.
 * Assembles the atmospheric background, editorial title, central floating 3D orb,
 * live transcription, refreshable prompt suggestions, and floating bottom interaction capsule.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = viewModel(),
    onOpenHistory: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Runtime permission launcher for microphone audio capture (used for voice)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onTalkButtonClicked(hasRecordPermission = true)
        } else {
            viewModel.onPermissionDenied()
        }
    }

    val onTalkClick = {
        when (uiState.assistantState) {
            AssistantState.LISTENING -> {
                viewModel.onTalkButtonClicked(hasRecordPermission = true)
            }
            AssistantState.IDLE -> {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    viewModel.onTalkButtonClicked(hasRecordPermission = true)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            AssistantState.ERROR -> {
                viewModel.onTalkButtonClicked(hasRecordPermission = true)
            }
            AssistantState.THINKING, AssistantState.SPEAKING -> {
                // Disabled while reasoning or speaking
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        KumaruBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header: Branding, AI v0.2.5 Pill, State Capsule, History & Profile shortcuts
                AssistantHeader(
                    state = uiState.assistantState,
                    onResetConversation = { viewModel.resetConversation() },
                    onOpenHistory = onOpenHistory,
                    onOpenProfile = onOpenProfile,
                    activeIdentity = uiState.activeIdentity,
                    currentMode = uiState.currentMode
                )

                // Editorial Title
                if (uiState.messages.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = AccentPinkPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                if (userProfile.userName.isNotBlank()) {
                                    append("Hello, ${userProfile.userName}\n")
                                } else {
                                    append("AI Enables Smooth Assistant\n")
                                }
                            }
                            withStyle(
                                SpanStyle(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                if (userProfile.userName.isNotBlank()) {
                                    append("How can Kumaru assist you?")
                                } else {
                                    append("& Voice Interaction")
                                }
                            }
                        },
                        fontSize = 18.5.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Central 3D Glossy AI Orb
                KumaruOrb(
                    state = uiState.assistantState,
                    size = if (uiState.messages.isEmpty()) 145.dp else 105.dp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // State / Real-time Live Transcription slot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.assistantState == AssistantState.LISTENING && uiState.currentInput.isNotBlank()) {
                        Text(
                            text = "\"${uiState.currentInput}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = AccentPinkPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    } else {
                        Text(
                            text = when (uiState.assistantState) {
                                AssistantState.IDLE -> if (uiState.errorMessage != null) uiState.errorMessage!! else "Kumaru is ready"
                                AssistantState.LISTENING -> "Listening… Speak now or tap to send"
                                AssistantState.THINKING -> "Thinking with Gemini…"
                                AssistantState.SPEAKING -> "Speaking aloud…"
                                AssistantState.ERROR -> uiState.errorMessage ?: "Something went wrong."
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            color = when (uiState.assistantState) {
                                AssistantState.ERROR -> StateError
                                else -> TextSecondary
                            }
                        )
                    }
                }

                // Scrollable conversation transcript feed or suggestions with refresh
                TranscriptView(
                    messages = uiState.messages,
                    suggestedPrompts = uiState.suggestedPrompts,
                    onSuggestionClick = { suggestion ->
                        viewModel.onSuggestionSelected(suggestion)
                    },
                    onRefreshPrompts = {
                        viewModel.refreshSuggestedPrompts()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Bottom Floating Interaction Capsule: Text Input + Voice Button + Send Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ChatInputField(
                        state = uiState.assistantState,
                        onSendMessage = { message ->
                            viewModel.onSendTextMessage(message)
                        },
                        onVoiceClick = onTalkClick
                    )
                }
            }
        }
    }
}
