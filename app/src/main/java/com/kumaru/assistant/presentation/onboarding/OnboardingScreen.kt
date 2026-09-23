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
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.UserProfile
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.components.GlassCard
import com.kumaru.assistant.presentation.components.KumaruBackground
import com.kumaru.assistant.presentation.components.KumaruOrb
import com.kumaru.assistant.presentation.theme.AccentPinkGradientEnd
import com.kumaru.assistant.presentation.theme.AccentPinkGradientStart
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassBorderPink
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.TextMuted
import com.kumaru.assistant.presentation.theme.TextOnPink
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary

/**
 * Streamlined Onboarding Flow for Kumaru V0.2.4.
 *
 * Flow Structure:
 * - INTRO (3 compact informational screens with visible "Skip" buttons)
 *   - Screen 1: Welcome & Mission ("Your personal AI, built around you.")
 *   - Screen 2: Intro ("Hi. I'm Kumaru.")
 *   - Screen 3: Capabilities ("My job is simple" -> THINK, CREATE, ASSIST)
 * - SETUP (Max 4 focused personalization questions with subtle "1 / 4" progress)
 *   - Q1: Name ("What should I call you?") [Required]
 *   - Q2: Style ("How should I talk to you?") [Casual, Focused, Playful, Balanced]
 *   - Q3: Focus ("What should I help you with?") [Work, Ideas, Learning, Life, Fun, Organization]
 *   - Q4: Context ("Anything I should know?") [Optional, "Skip for now"]
 * - FINAL ("You're all set." / "Kumaru is ready." -> "Enter Kumaru")
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: (UserProfile) -> Unit,
    initialProfile: UserProfile = UserProfile(),
    startInSetup: Boolean = false,
    onCancelEdit: (() -> Unit)? = null
) {
    // Steps: 0..2 = Intro, 3..6 = Setup Q1..Q4, 7 = Final Screen
    var step by remember { mutableIntStateOf(if (startInSetup) 3 else 0) }

    var userName by remember { mutableStateOf(initialProfile.userName) }
    var communicationStyle by remember { mutableStateOf(initialProfile.communicationStyle.ifBlank { "CASUAL" }) }
    var primaryInterests by remember {
        mutableStateOf(
            if (initialProfile.primaryInterests.isNotEmpty()) {
                initialProfile.primaryInterests
            } else {
                setOf("WORK", "IDEAS")
            }
        )
    }
    var personalContext by remember { mutableStateOf(initialProfile.personalContext) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Physical Back button handler
    BackHandler(enabled = true) {
        when {
            startInSetup && step == 3 -> {
                onCancelEdit?.invoke()
            }
            step > 0 -> {
                step--
            }
            startInSetup -> {
                onCancelEdit?.invoke()
            }
        }
    }

    val completeSetup = {
        keyboardController?.hide()
        val completedProfile = UserProfile(
            isOnboardingCompleted = true,
            userName = userName.trim(),
            communicationStyle = communicationStyle,
            primaryInterests = primaryInterests,
            personalContext = personalContext.trim(),
            memoryPreference = initialProfile.memoryPreference.ifBlank { "USEFUL" }
        )
        onOnboardingFinished(completedProfile)
    }

    val nextStep = {
        keyboardController?.hide()
        if (step < 7) {
            step++
        } else {
            completeSetup()
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
            // ----------------------------------------------------
            // TOP BAR: Skip Button (Intro) OR Subtle Progress (Setup)
            // ----------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Back button (on intro step 1..2, and setup steps 3..6)
                if (step in 1..6) {
                    IconButton(
                        onClick = {
                            if (startInSetup && step == 3) {
                                onCancelEdit?.invoke()
                            } else {
                                step--
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                // Center: Subtle title / spacer
                if (step in 3..6) {
                    Text(
                        text = "PERSONALIZATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextMuted
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Right: "Skip" button on Intro (Steps 0, 1, 2) OR Subtle Progress "X / 4" (Steps 3..6)
                if (step in 0..2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x14E05697))
                            .clickable {
                                keyboardController?.hide()
                                step = 3 // IMMEDIATELY jump directly to Setup Question 1
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentPinkPrimary
                        )
                    }
                } else if (step in 3..6) {
                    // Subtle, minimal progress indicator (e.g. 1 / 4)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x0C1E1B2E))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step - 2} / 4",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPinkPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }

            // ----------------------------------------------------
            // MAIN STEP CONTENT
            // ----------------------------------------------------
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
                label = "OnboardingContent"
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
                        // ==========================================
                        // INTRO SCREEN 1: Welcome
                        // ==========================================
                        0 -> {
                            KumaruOrb(state = AssistantState.IDLE, size = 150.dp)
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
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        // ==========================================
                        // INTRO SCREEN 2: Meet Kumaru
                        // ==========================================
                        1 -> {
                            KumaruOrb(state = AssistantState.SPEAKING, size = 120.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Hi. I'm Kumaru.",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "I can help you think, create, learn and get things done.",
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // ==========================================
                        // INTRO SCREEN 3: 3 Compact Capabilities
                        // ==========================================
                        2 -> {
                            Text(
                                text = "My job is simple.",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Designed around how you think and work.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            val pillars = listOf(
                                Triple("THINK", "Understand, decide, and reason through ideas.", Icons.Outlined.Psychology),
                                Triple("CREATE", "Brainstorm, draft, and bring concepts to life.", Icons.Outlined.AutoAwesome),
                                Triple("ASSIST", "Voice or text, always ready with a second brain.", Icons.Outlined.RecordVoiceOver)
                            )

                            pillars.forEach { (title, desc, icon) ->
                                CapabilityCard(title = title, description = desc, icon = icon)
                            }
                        }

                        // ==========================================
                        // SETUP QUESTION 1: Name (Required)
                        // ==========================================
                        3 -> {
                            Text(
                                text = "First things first",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "What should I call you?",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = GlassSurfaceWhite
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                                ) {
                                    if (userName.isEmpty()) {
                                        Text(
                                            text = "Enter your preferred name...",
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
                                            onDone = {
                                                if (userName.isNotBlank()) nextStep()
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // ==========================================
                        // SETUP QUESTION 2: Tone (Casual, Focused, Playful, Balanced)
                        // ==========================================
                        4 -> {
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
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            val styles = listOf(
                                Pair("Casual", "Friendly and natural."),
                                Pair("Focused", "Direct and straight to the point."),
                                Pair("Playful", "Warm with personality and wit."),
                                Pair("Balanced", "Adapts flexibly to the moment.")
                            )

                            styles.forEach { (title, desc) ->
                                val code = title.uppercase()
                                val isSelected = communicationStyle.equals(code, ignoreCase = true)
                                SelectionOptionCard(
                                    title = title,
                                    subtitle = desc,
                                    isSelected = isSelected,
                                    onClick = { communicationStyle = code }
                                )
                            }
                        }

                        // ==========================================
                        // SETUP QUESTION 3: Primary Interests (Multi-select)
                        // ==========================================
                        5 -> {
                            Text(
                                text = "Focus Areas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "What should I help you with?",
                                fontSize = 24.sp,
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
                            Spacer(modifier = Modifier.height(16.dp))

                            val interestOptions = listOf(
                                Pair("Work", "Tasks, projects & productivity"),
                                Pair("Ideas", "Brainstorming & creative sparks"),
                                Pair("Learning", "Understanding new concepts"),
                                Pair("Life", "Everyday planning & routines"),
                                Pair("Fun", "Casual chats, thoughts & humor"),
                                Pair("Organization", "Keeping things neat & structured")
                            )

                            interestOptions.forEach { (title, desc) ->
                                val code = title.uppercase()
                                val isSelected = primaryInterests.contains(code)
                                SelectionOptionCard(
                                    title = title,
                                    subtitle = desc,
                                    isSelected = isSelected,
                                    isMultiSelect = true,
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

                        // ==========================================
                        // SETUP QUESTION 4: Personal Context (Optional)
                        // ==========================================
                        6 -> {
                            Text(
                                text = "Personal Context",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Anything I should know?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Optional note (e.g., student, developer, night owl)",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = GlassSurfaceWhite
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                                ) {
                                    if (personalContext.isEmpty()) {
                                        Text(
                                            text = "e.g. \"I'm a computer science student building Android apps, prefer concise explanations.\"",
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

                        // ==========================================
                        // FINAL SETUP SCREEN: All Set
                        // ==========================================
                        7 -> {
                            KumaruOrb(state = AssistantState.IDLE, size = 130.dp)
                            Spacer(modifier = Modifier.height(22.dp))
                            Text(
                                text = "You're all set.",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Kumaru is ready.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentPinkPrimary
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
                                            text = if (userName.isNotBlank()) userName else "Companion",
                                            fontSize = 17.sp,
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
                                                text = communicationStyle.lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AccentPinkPrimary
                                            )
                                        }
                                    }

                                    if (primaryInterests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            primaryInterests.forEach { interest ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0x0C1E1B2E))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = interest.lowercase().replaceFirstChar { it.uppercase() },
                                                        fontSize = 12.sp,
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
            }

            // ----------------------------------------------------
            // BOTTOM CTA ACTION BUTTON
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secondary "Skip for now" link for Q4
                if (step == 6) {
                    Text(
                        text = "Skip for now",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                keyboardController?.hide()
                                step = 7
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Primary Gradient CTA Button
                val isPrimaryButtonEnabled = when (step) {
                    3 -> userName.isNotBlank()
                    else -> true
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isPrimaryButtonEnabled) 4.dp else 0.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color(0x20000000),
                            spotColor = Color(0x35E05697)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (isPrimaryButtonEnabled) {
                                Brush.horizontalGradient(
                                    listOf(AccentPinkGradientStart, AccentPinkGradientEnd)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE2D9E2), Color(0xFFD4CBD4))
                                )
                            }
                        )
                        .clickable(enabled = isPrimaryButtonEnabled) {
                            nextStep()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (step) {
                                0 -> "Meet Kumaru"
                                2 -> "Set Up Kumaru"
                                7 -> "Enter Kumaru"
                                else -> "Continue"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPrimaryButtonEnabled) TextOnPink else Color(0xFF8E8894)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isPrimaryButtonEnabled) TextOnPink else Color(0xFF8E8894),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT HELPERS
// -----------------------------------------------------------------------------

@Composable
private fun CapabilityCard(
    title: String,
    description: String,
    icon: ImageVector
) {
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
                    text = description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SelectionOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isMultiSelect: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                color = if (isSelected) GlassBorderPink else GlassBorderLight,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) AccentPinkPrimary else TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(if (isMultiSelect) RoundedCornerShape(6.dp) else CircleShape)
                    .background(if (isSelected) AccentPinkPrimary else Color(0x121E1B2E))
                    .border(
                        1.dp,
                        if (isSelected) AccentPinkPrimary else Color(0x281E1B2E),
                        if (isMultiSelect) RoundedCornerShape(6.dp) else CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
