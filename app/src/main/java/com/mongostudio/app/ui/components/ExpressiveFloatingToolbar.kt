package com.mongostudio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mongostudio.app.ui.theme.*

data class ToolbarAction(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String,
    val tint: Color = TextPrimary,
    val badgeCount: Int? = null,
    val onClick: () -> Unit
)

/**
 * Material 3 Expressive Horizontal Floating Toolbar.
 * Floating action dock positioned above screen content with playful spring interactions.
 */
@Composable
fun ExpressiveFloatingToolbar(
    actions: List<ToolbarAction>,
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    containerColor: Color = SurfaceContainerHigh.copy(alpha = 0.95f),
    borderColor: Color = CardBorderDark
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 12.dp, shape = PillShape, spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(PillShape)
                .background(containerColor)
                .border(1.dp, borderColor, PillShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

            actions.forEach { action ->
                val interaction = remember { MutableInteractionSource() }
                val isPressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                    label = "toolbar_icon_scale"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interaction,
                            indication = ripple(bounded = true, color = action.tint),
                            onClick = {
                                haptics.performClickFeedback()
                                action.onClick()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = action.tint,
                        modifier = Modifier.size(22.dp)
                    )

                    if (action.badgeCount != null && action.badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RoseAccent)
                        )
                    }
                }
            }

            if (floatingActionButton != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier.padding(start = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    floatingActionButton()
                }
            }
        }
    }
}
