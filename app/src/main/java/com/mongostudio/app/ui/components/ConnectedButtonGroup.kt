package com.mongostudio.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.mongostudio.app.ui.theme.*

data class ButtonGroupItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val count: Int? = null
)

/**
 * Material 3 Expressive Connected Button Group (Single Select).
 * Connected pill segments with spring transitions and expressive states.
 */
@Composable
fun ConnectedButtonGroup(
    items: List<ButtonGroupItem>,
    selectedId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = EmeraldPrimary,
    activeContentColor: Color = TextOnPrimary
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(SurfaceContainerHigh)
            .border(1.dp, CardBorderDark, PillShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = item.id == selectedId
            val shape = when {
                items.size == 1 -> PillShape
                index == 0 -> ConnectedGroupStartShape
                index == items.lastIndex -> ConnectedGroupEndShape
                else -> ConnectedGroupCenterShape
            }

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) activeColor else Color.Transparent,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "btn_group_bg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) activeContentColor else TextSecondary,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "btn_group_text"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.0f else 0.98f,
                label = "btn_group_scale"
            )

            val interaction = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(shape)
                    .background(bgColor)
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(color = textColor),
                        onClick = { onItemSelected(item.id) }
                    )
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                    if (item.count != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(
                                    if (isSelected) activeContentColor.copy(alpha = 0.2f)
                                    else SurfaceContainerHighest
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
