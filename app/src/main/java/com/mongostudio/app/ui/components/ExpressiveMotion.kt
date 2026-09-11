package com.mongostudio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay

/**
 * Material 3 Expressive motion specifications (§9.3, §9.4).
 * Spatial spring for position/scale/bounds, effects curve for fade/color.
 */
val ExpressiveFastSpatialSpec = spring<Float>(
    dampingRatio = 0.72f,
    stiffness = Spring.StiffnessMediumLow
)

val ExpressiveSpatialSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessLow
)

val ExpressiveSlideSpatialSpec = spring<IntOffset>(
    dampingRatio = 0.75f,
    stiffness = Spring.StiffnessMediumLow
)

val ExpressiveEffectsSpec = tween<Float>(
    durationMillis = 180
)

val ExpressiveColorSpec = tween<Color>(
    durationMillis = 160
)

/**
 * Material 3 Expressive Press-Morph Modifier (§9.1).
 * Smooth spring-physics scale bounce on press.
 * Uses explicit remember pattern instead of deprecated composed{}.
 */
@Composable
fun Modifier.pressMorph(
    enabled: Boolean = true,
    pressedScale: Float = 0.93f,
    onClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = ExpressiveFastSpatialSpec,
        label = "pressMorphScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    enabled = enabled,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
}

/**
 * Material 3 Expressive Staggered Entrance (§9.2).
 * Fades and slides in list items with spring physics.
 * Key stability: uses `key = Unit` so re-parenting doesn't reset.
 */
@Composable
fun StaggerEntrance(
    index: Int,
    staggerMs: Long = 30L,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val delayMs = (index * staggerMs).coerceAtMost(300L) // cap max stagger
        delay(delayMs)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = ExpressiveEffectsSpec) +
            slideInVertically(
                animationSpec = ExpressiveSlideSpatialSpec,
                initialOffsetY = { it / 5 }
            ),
        exit = fadeOut(animationSpec = tween(120)) +
            slideOutVertically(
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessHigh),
                targetOffsetY = { -it / 5 }
            ),
        content = content
    )
}

/**
 * Haptic feedback helpers matching M3 Expressive tactile feel (§9.5).
 */
fun HapticFeedback.performConfirmFeedback() {
    try {
        performHapticFeedback(HapticFeedbackType.LongPress)
    } catch (_: Throwable) {}
}

fun HapticFeedback.performClickFeedback() {
    try {
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    } catch (_: Throwable) {}
}
