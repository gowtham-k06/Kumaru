package com.kumaru.assistant.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.UserProfile
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.components.GlassCard
import com.kumaru.assistant.presentation.components.KumaruBackground
import com.kumaru.assistant.presentation.components.KumaruMiniOrb
import com.kumaru.assistant.presentation.components.KumaruOrb
import com.kumaru.assistant.presentation.theme.AccentPinkGradientEnd
import com.kumaru.assistant.presentation.theme.AccentPinkGradientStart
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassBorderPink
import com.kumaru.assistant.presentation.theme.GlassSurfaceTranslucent
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextOnPink
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary
import com.kumaru.assistant.presentation.theme.TextTertiary

/**
 * Conversational Onboarding Flow for Kumaru V0.2.3.
 * Progressive step-by-step introduction and personalization questionnaire.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: (UserProfile) -> Unit,
    initialProfile: UserProfile = UserProfile()
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 10

    var userName by remember { mutableStateOf(initialProfile.userName) }
    var communicationStyle by remember { mutableStateOf(initialProfile.communicationStyle.ifBlank { "CASUAL" }) }
    var primaryInterests by remember { mutableStateOf(if (initialProfile.primaryInterests.isNotEmpty()) initialProfile.primaryInterests else setOf("IDEAS", "LEARNING")) }
    var personalContext by remember { mutableStateOf(initialProfile.personalContext) }
    var memoryPreference by remember { mutableStateOf(initialProfile.memoryPreference.ifBlank { "USEFUL" }) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle Android physical back press to go to previous step
    BackHandler(enabled = step > 0) {
        step--
    }

    val nextStep = {
        keyboardController?.hide()
        if (step < totalSteps - 1) {
            step++
        } else {
            val completedProfile = UserProfile(
                isOnboardingCompleted = true,
                userName = userName.trim(),
                communicationStyle = communicationStyle,
                primaryInterests = primaryInterests,
                personalContext = personalContext.trim(),
                memoryPreference = memoryPreference
            )
            onOnboardingFinished(completedProfile)
        }
    }

    KumaruBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top Navigation Bar with Progress (Visible on question steps)
            if (step > 0 && step < totalSteps - 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Progress Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { step.toFloat() / (totalSteps - 1).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentPinkPrimary,
                            trackColor = Color(0x1FE05697)
                        )
                    }

                    Text(
                        text = "$step/${totalSteps - 2}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Main Step Content with Smooth Animated Transition
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "OnboardingStepContent"
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (targetStep) {
                        // 0. WELCOME
                        0 -> {
                            KumaruOrb(state = AssistantState.IDLE, size = 160.dp)
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = "KUMARU",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your personal AI, built around you.",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = AccentPinkPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "An assistant that listens, remembers, thinks with you, and helps you get things done.",
                                fontSize = 14.5.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // 1. WHO IS KUMARU?
                        1 -> {
                            KumaruOrb(state = AssistantState.SPEAKING, size = 110.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Hi. I'm Kumaru.",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "I'm here to make the everyday stuff lighter.",
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            val bulletPoints = listOf(
                                "Answer your questions clearly & concisely",
                                "Help brainstorm, plan & think through ideas",
                                "Remember useful context across your sessions",
                                "Talk naturally in fluent English and Tamil"
                            )

                            bulletPoints.forEach { point ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    backgroundColor = GlassSurfaceWhite
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = AccentPinkPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = point,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // 2. KUMARU'S DUTY
                        2 -> {
                            Text(
                                text = "My job is simple.",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Four pillars designed around your productivity.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            val duties = listOf(
                                Triple("THINK", "Help you understand, decide and create.", Icons.Outlined.Psychology),
                                Triple("REMEMBER", "Keep useful things you choose to share.", Icons.Outlined.AutoAwesome),
                                Triple("ORGANIZE", "Help turn thoughts into actions.", Icons.Outlined.TaskAlt),
                                Triple("ASSIST", "Be there when you need a second brain.", Icons.Outlined.RecordVoiceOver)
                            )

                            duties.forEach { (title, desc, icon) ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = GlassSurfaceWhite
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x1AE05697)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = AccentPinkPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = AccentPinkPrimary
                                            )
                                            Text(
                                                text = desc,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. PERSONALIZATION INTRO
                        3 -> {
                            KumaruOrb(state = AssistantState.THINKING, size = 120.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Before we begin...",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tell me a little about you.\nI'll use it to make Kumaru feel more like yours.",
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary
                            )
                        }

                        // 4. Q1 — NAME
                        4 -> {
                            Text(
                                text = "First things first...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "What should I call you?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                                ) {
                                    if (userName.isEmpty()) {
                                        Text(
                                            text = "Enter your name...",
                                            fontSize = 16.sp,
                                            color = TextMuted
                                        )
                                    }
                                    BasicTextField(
                                        value = userName,
                                        onValueChange = { userName = it },
                                        textStyle = TextStyle(
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(AccentPinkPrimary),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Words,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { nextStep() }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 5. Q2 — COMMUNICATION STYLE
                        5 -> {
                            Text(
                                text = "Conversational Tone",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "How should I talk to you?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            val styles = listOf(
                                Pair("CASUAL", "Like a friend."),
                                Pair("FOCUSED", "Straight to the point."),
                                Pair("PLAYFUL", "A little personality is good."),
                                Pair("BALANCED", "Depends on the moment.")
                            )

                            styles.forEach { (code, desc) ->
                                val isSelected = communicationStyle == code
                                OptionSelectionCard(
                                    title = code,
                                    subtitle = desc,
                                    isSelected = isSelected,
                                    onClick = { communicationStyle = code }
                                )
                            }
                        }

                        // 6. Q3 — PRIMARY INTERESTS
                        6 -> {
                            Text(
                                text = "Focus Areas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "What do you want me to help with most?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select all that apply",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            val interests = listOf(
                                Pair("IDEAS", "Brainstorming & creativity"),
                                Pair("WORK", "Tasks, projects & productivity"),
                                Pair("LEARNING", "Understanding new things"),
                                Pair("LIFE", "Planning & everyday decisions"),
                                Pair("CONVERSATION", "Someone to think out loud with"),
                                Pair("ORGANIZATION", "Keeping things in order")
                            )

                            interests.forEach { (code, desc) ->
                                val isSelected = code in primaryInterests
                                OptionSelectionCard(
                                    title = code,
                                    subtitle = desc,
                                    isSelected = isSelected,
                                    onClick = {
                                        primaryInterests = if (isSelected) {
                                            if (primaryInterests.size > 1) primaryInterests - code else primaryInterests
                                        } else {
                                            primaryInterests + code
                                        }
                                    }
                                )
                            }
                        }

                        // 7. Q4 — PERSONAL CONTEXT
                        7 -> {
                            Text(
                                text = "Personal Context",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "What should I understand about you?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                                ) {
                                    if (personalContext.isEmpty()) {
                                        Text(
                                            text = "Anything you want Kumaru to know (e.g., student, developer, night owl)...",
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            color = TextMuted
                                        )
                                    }
                                    BasicTextField(
                                        value = personalContext,
                                        onValueChange = { personalContext = it },
                                        textStyle = TextStyle(
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(AccentPinkPrimary),
                                        minLines = 3,
                                        maxLines = 5,
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 8. Q5 — MEMORY PREFERENCE
                        8 -> {
                            Text(
                                text = "Memory & Privacy",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "How much should I remember?",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            val memoryOptions = listOf(
                                Pair("MINIMAL", "Only what I need for the current conversation."),
                                Pair("USEFUL", "Remember things that help me assist you better."),
                                Pair("PERSONAL", "Remember more about how I work and what I like.")
                            )

                            memoryOptions.forEach { (code, desc) ->
                                val isSelected = memoryPreference == code
                                OptionSelectionCard(
                                    title = code,
                                    subtitle = desc,
                                    isSelected = isSelected,
                                    onClick = { memoryPreference = code }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Privacy",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "All memory is stored locally on your device.",
                                    fontSize = 11.5.sp,
                                    color = TextTertiary
                                )
                            }
                        }

                        // 9. COMPLETION
                        9 -> {
                            KumaruOrb(state = AssistantState.IDLE, size = 130.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Alright. We're ready.",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your Kumaru is set up.",
                                fontSize = 15.sp,
                                color = AccentPinkPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Summary Glass Card
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = GlassSurfaceWhite
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (userName.isNotBlank()) "Master $userName" else "Personal AI",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x1AE05697))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = communicationStyle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AccentPinkPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        primaryInterests.forEach { interest ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0x0C1E1B2E))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = interest.lowercase().replaceFirstChar { it.uppercase() },
                                                    fontSize = 11.5.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom CTA Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color(0x20000000),
                            spotColor = Color(0x35E05697)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentPinkGradientStart, AccentPinkGradientEnd)
                            )
                        )
                        .clickable { nextStep() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (step) {
                                0 -> "Meet Kumaru"
                                3 -> "Let's do it"
                                9 -> "Enter Kumaru"
                                else -> "Continue"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextOnPink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TextOnPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x10000000),
                spotColor = if (isSelected) Color(0x30E05697) else Color(0x08000000)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFFFFF0F6) else GlassSurfaceWhite)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentPinkPrimary else GlassBorderLight,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) AccentPinkPrimary else TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(AccentPinkPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = TextOnPink,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
