package com.mongostudio.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive Number Ticker Animation (§9.2).
 * Smoothly rolls digits vertically on count / metric updates.
 */
@Composable
fun NumberTicker(
    value: String,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        value.forEach { char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> height } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "DigitTicker"
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        style = textStyle,
                        color = color
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    style = textStyle,
                    color = color
                )
            }
        }
    }
}

/**
 * Material 3 Expressive Press-and-Hold Action Trigger Ring (§9.7).
 * Pressing and holding smoothly fills an animated circular arc before triggering the action.
 * Releasing early cancels the action safely.
 */
@Composable
fun PressAndHoldTriggerButton(
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier.size(56.dp),
    icon: ImageVector = Icons.Default.DeleteForever,
    tintColor: Color = MaterialTheme.colorScheme.error,
    backgroundColor: Color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
    contentDescription: String? = "Hold to confirm"
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val job = scope.launch {
                            progress.animateTo(1f, tween(durationMillis = 1000, easing = LinearEasing))
                            onTrigger()
                        }
                        tryAwaitRelease()
                        job.cancel()
                        scope.launch { progress.animateTo(0f, tween(180)) }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize().padding(3.dp)) {
            // Track
            drawCircle(
                color = tintColor.copy(alpha = 0.2f),
                style = Stroke(width = 3.5.dp.toPx())
            )
            // Progress arc
            if (progress.value > 0f) {
                drawArc(
                    color = tintColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.value,
                    useCenter = false,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Material 3 Expressive Day/Night Morph Toggle Switch (§9.8).
 * Rotating spring bounce icon transition between light and dark modes.
 */
@Composable
fun DayNightMorphToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDark) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ToggleRotation"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isDark) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
        label = "ToggleContainer"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = "Toggle theme mode",
            tint = if (isDark) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation)
        )
    }
}

/**
 * Material 3 Expressive Velocity-Aware Auto-Hiding Scrollbar (§8.4).
 * Dynamic vertical scrollbar indicator that highlights during flings and fades out when stationary.
 */
@Composable
fun VelocityAwareScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val isScrolling = listState.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (isScrolling) 0.85f else 0f,
        animationSpec = tween(durationMillis = if (isScrolling) 150 else 600),
        label = "ScrollbarAlpha"
    )

    Canvas(
        modifier = modifier
            .width(5.dp)
            .fillMaxHeight()
    ) {
        val totalItems = listState.layoutInfo.totalItemsCount
        val visibleItems = listState.layoutInfo.visibleItemsInfo.size
        if (totalItems > 0 && visibleItems > 0) {
            val thumbHeight = (size.height * (visibleItems.toFloat() / totalItems)).coerceAtLeast(32.dp.toPx())
            val firstVisibleIndex = listState.firstVisibleItemIndex.toFloat()
            val thumbY = (size.height - thumbHeight) * (firstVisibleIndex / (totalItems - visibleItems).coerceAtLeast(1))

            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(0f, thumbY),
                size = Size(size.width, thumbHeight),
                cornerRadius = CornerRadius(2.5.dp.toPx())
            )
        }
    }
}

/**
 * Material 3 Expressive Accordion Expandable Card (§9.4).
 */
@Composable
fun AccordionCard(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .clickable { isExpanded = !isExpanded },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}
