package com.kumaru.assistant.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumaru.assistant.core.model.InteractionMode
import com.kumaru.assistant.core.model.UserIdentity
import com.kumaru.assistant.core.state.AssistantState
import com.kumaru.assistant.presentation.theme.AccentPinkPrimary
import com.kumaru.assistant.presentation.theme.GlassBorderLight
import com.kumaru.assistant.presentation.theme.GlassSurfaceWhite
import com.kumaru.assistant.presentation.theme.StateError
import com.kumaru.assistant.presentation.theme.StateIdle
import com.kumaru.assistant.presentation.theme.StateListening
import com.kumaru.assistant.presentation.theme.StateSpeaking
import com.kumaru.assistant.presentation.theme.StateThinking
import com.kumaru.assistant.presentation.theme.TextPrimary
import com.kumaru.assistant.presentation.theme.TextSecondary

/**
 * Editorial header for Kumaru V0.2.5.
 * Displays clean modern branding, live glass status pill, active user identity,
 * conversational mode indicator, History, Profile, and New Chat actions.
 */
@Composable
fun AssistantHeader(
    state: AssistantState,
    onResetConversation: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    activeIdentity: UserIdentity = UserIdentity.GOWTHAM,
    currentMode: InteractionMode = InteractionMode.CASUAL,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> StateIdle
            AssistantState.LISTENING -> StateListening
            AssistantState.THINKING -> StateThinking
            AssistantState.SPEAKING -> StateSpeaking
            AssistantState.ERROR -> StateError
        },
        label = "HeaderStatusColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Kumaru Brand & Identity + Profile shortcut
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onOpenProfile() }
        ) {
            // Stylized C / Kumaru emblem badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape, ambientColor = Color(0x15000000))
                    .clip(CircleShape)
                    .background(GlassSurfaceWhite)
                    .border(1.dp, GlassBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                KumaruMiniOrb(size = 20.dp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KUMARU",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1FE05697))
                            .border(0.5.dp, Color(0x3DE05697), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "AI V0.2.6",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentPinkPrimary,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
                Text(
                    text = "${activeIdentity.displayName} • ${currentMode.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }

        // Right Action Bar: History, Status Pill, New Chat, Profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // History Icon Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x10000000))
                    .clip(CircleShape)
                    .background(GlassSurfaceWhite)
                    .border(1.dp, GlassBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Conversation History",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Glass Status Pill
            Box(
                modifier = Modifier
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0x10000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassSurfaceWhite)
                    .border(1.dp, GlassBorderLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.5.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = state.label.uppercase(),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = TextPrimary
                    )
                }
            }

            // New Chat / Reset
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x10000000))
                    .clip(CircleShape)
                    .background(GlassSurfaceWhite)
                    .border(1.dp, GlassBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onResetConversation,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "New Conversation",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Profile Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape, ambientColor = Color(0x10000000))
                    .clip(CircleShape)
                    .background(GlassSurfaceWhite)
                    .border(1.dp, GlassBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = "User Profile",
                        tint = AccentPinkPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
