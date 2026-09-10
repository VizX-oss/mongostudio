package com.mongostudio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.ui.theme.*

/**
 * Material 3 Expressive Split Button.
 * Provides a primary action on the leading segment and a contextual dropdown/options
 * trigger on the trailing segment, adhering to Google's Expressive Button design tokens.
 */
@Composable
fun ExpressiveSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    onTrailingClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailingIcon: ImageVector = Icons.Default.ArrowDropDown,
    containerColor: Color = EmeraldPrimary,
    contentColor: Color = TextOnPrimary,
    enabled: Boolean = true
) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    val primaryInteraction = remember { MutableInteractionSource() }
    val isPrimaryPressed by primaryInteraction.collectIsPressedAsState()
    val primaryScale by animateFloatAsState(
        targetValue = if (isPrimaryPressed) 0.96f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "primary_scale"
    )

    val trailingInteraction = remember { MutableInteractionSource() }
    val isTrailingPressed by trailingInteraction.collectIsPressedAsState()
    val trailingScale by animateFloatAsState(
        targetValue = if (isTrailingPressed) 0.94f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "trailing_scale"
    )

    Row(
        modifier = modifier.height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Primary Action
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight()
                .scale(primaryScale)
                .clip(SplitButtonLeadingShape)
                .background(if (enabled) containerColor else containerColor.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = primaryInteraction,
                    indication = ripple(color = contentColor),
                    enabled = enabled,
                    onClick = {
                        haptics.performClickFeedback()
                        onPrimaryClick()
                    }
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Trailing Secondary / Options Action
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .scale(trailingScale)
                .clip(SplitButtonTrailingShape)
                .background(if (enabled) containerColor.copy(alpha = 0.9f) else containerColor.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = trailingInteraction,
                    indication = ripple(color = contentColor),
                    enabled = enabled,
                    onClick = {
                        haptics.performClickFeedback()
                        onTrailingClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = "Options",
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
