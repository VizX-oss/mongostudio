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
import androidx.compose.foundation.layout.Box
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
 * Material 3 Expressive Parallel Staggered Entrance (§9.2).
 * Runs smooth GPU-accelerated alpha & slide animations in parallel via graphicsLayer
 * WITHOUT blocking scroll momentum, causing layout jumps, or delaying LazyColumn items.
 */
@Composable
fun StaggerEntrance(
    index: Int,
    staggerMs: Long = 20L,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatable = remember { androidx.compose.animation.core.Animatable(if (index < 8) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (index < 8) {
            val delayMs = (index * staggerMs).coerceAtMost(160L)
            delay(delayMs)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val progress = animatable.value
                alpha = progress
                translationY = (1f - progress) * 24f
            }
    ) {
        content()
    }
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
