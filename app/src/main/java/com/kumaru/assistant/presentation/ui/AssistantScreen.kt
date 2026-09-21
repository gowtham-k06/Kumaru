package com.kumaru.assistant.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.components.AssistantHeader
import com.kumaru.assistant.presentation.components.ChatInputField
import com.kumaru.assistant.presentation.components.GlowingOrb
import com.kumaru.assistant.presentation.components.TalkButton
import com.kumaru.assistant.presentation.components.TranscriptView
import com.kumaru.assistant.presentation.theme.DarkObsidian
import com.kumaru.assistant.presentation.theme.ErrorCrimson
import com.kumaru.assistant.presentation.theme.NeonCyan
import com.kumaru.assistant.presentation.theme.TextSecondary
import com.kumaru.assistant.presentation.theme.VoidBlack
import com.kumaru.assistant.presentation.viewmodel.AssistantViewModel

/**
 * Root screen for Kumaru V0.2.1 Real Voice + Text Assistant.
 * Assembles the state-reactive Glowing Orb, continuous speech input pipeline,
 * glassmorphic text input field, conversation transcript, and tactile Talk button.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Runtime permission launcher for microphone audio capture (used ONLY for voice)
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
        containerColor = VoidBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DarkObsidian,
                            VoidBlack
                        ),
                        radius = 1600f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top branding header & live state
                AssistantHeader(
                    state = uiState.assistantState,
                    onResetConversation = { viewModel.resetConversation() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Central interactive AI orb with dynamic state animations
                GlowingOrb(
                    state = uiState.assistantState,
                    size = 145.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Stable status & transcription slot (fixed height bounds prevents layout vibration)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.assistantState == AssistantState.LISTENING && uiState.currentInput.isNotBlank()) {
                        Text(
                            text = "\"${uiState.currentInput}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = NeonCyan,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    } else {
                        Text(
                            text = when (uiState.assistantState) {
                                AssistantState.IDLE -> if (uiState.errorMessage != null) uiState.errorMessage!! else "Kumaru is ready"
                                AssistantState.LISTENING -> "Listening… Speak or tap to submit"
                                AssistantState.THINKING -> "Thinking with Gemini…"
                                AssistantState.SPEAKING -> "Speaking aloud…"
                                AssistantState.ERROR -> uiState.errorMessage ?: "Something went wrong."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            color = when {
                                uiState.assistantState == AssistantState.ERROR -> ErrorCrimson
                                uiState.assistantState == AssistantState.IDLE && uiState.errorMessage != null -> TextSecondary
                                else -> TextSecondary
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scrollable conversation transcript feed
                TranscriptView(
                    messages = uiState.messages,
                    onSuggestionClick = { suggestion ->
                        viewModel.onSuggestionSelected(suggestion)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Text input area (No mic permission required)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ChatInputField(
                        state = uiState.assistantState,
                        onSendMessage = { message ->
                            viewModel.onSendTextMessage(message)
                        }
                    )
                }

                // Bottom Push-to-Talk action section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TalkButton(
                        state = uiState.assistantState,
                        onClick = onTalkClick
                    )
                }
            }
        }
    }
}
