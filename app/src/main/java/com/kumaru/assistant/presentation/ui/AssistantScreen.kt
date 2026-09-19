package com.kumaru.assistant.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.components.AssistantHeader
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
 * Root screen for Kumaru V0.1.
 * Assembles the ambient canvas orb, sleek transcript feed, and push-to-talk controls.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top branding header & live state
                AssistantHeader(
                    state = uiState.assistantState,
                    onResetConversation = { viewModel.resetConversation() }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Central interactive AI orb
                GlowingOrb(
                    state = uiState.assistantState,
                    size = 180.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status text label
                Text(
                    text = when (uiState.assistantState) {
                        AssistantState.IDLE -> "Kumaru is ready"
                        AssistantState.LISTENING -> "Listening to your voice…"
                        AssistantState.THINKING -> "Reasoning response…"
                        AssistantState.SPEAKING -> "Speaking aloud…"
                        AssistantState.ERROR -> uiState.errorMessage ?: "An error occurred"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = if (uiState.assistantState == AssistantState.ERROR) ErrorCrimson else TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                // Bottom Push-to-Talk action section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TalkButton(
                        state = uiState.assistantState,
                        onClick = { viewModel.onTalkButtonClicked() }
                    )
                }
            }
        }
    }
}
