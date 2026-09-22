package com.kumaru.assistant.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kumaru.assistant.core.state.AssistantState

/**
 * Compatible bridge forwarding to the reference-driven 3D glossy [KumaruOrb].
 */
@Composable
fun GlowingOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    KumaruOrb(
        state = state,
        modifier = modifier,
        size = size
    )
}
