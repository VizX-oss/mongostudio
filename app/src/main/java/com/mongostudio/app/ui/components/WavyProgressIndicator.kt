package com.mongostudio.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Linear Wavy Progress Indicator (§7.2).
 * Cubic bezier approximation of sine wave for fluid rendering.
 */
@Composable
fun WavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.18f),
    waveAmplitude: Dp = 4.dp,
    waveLength: Dp = 28.dp,
    strokeWidth: Dp = 3.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(waveAmplitude * 2 + strokeWidth * 2)
    ) {
        val width = size.width
        val midY = size.height / 2f
        val ampPx = waveAmplitude.toPx()
        val lambdaPx = waveLength.toPx().coerceAtLeast(10f)
        val strokePx = strokeWidth.toPx()
        val phaseOffset = phase * lambdaPx

        // Track
        drawLine(
            color = trackColor,
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        // Wave using cubic bezier
        val path = Path()
        val cp = lambdaPx * 0.3183f
        var x = -phaseOffset % lambdaPx
        var isFirst = true
        while (x <= width + lambdaPx) {
            val y0 = midY + ampPx * sin((x / lambdaPx) * (2 * PI).toFloat())
            if (isFirst) { path.moveTo(x, y0); isFirst = false }
            val xMid = x + lambdaPx / 2f
            val xEnd = x + lambdaPx
            path.cubicTo(
                x + cp, midY - ampPx,
                xMid - cp, midY - ampPx,
                xMid, midY
            )
            path.cubicTo(
                xMid + cp, midY + ampPx,
                xEnd - cp, midY + ampPx,
                xEnd, midY + ampPx * sin(((xEnd) / lambdaPx) * (2 * PI).toFloat())
            )
            x += lambdaPx
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

/**
 * Material 3 Expressive Circular Spinner (§7.2).
 * Clean, fluid rotating indeterminate arc with subtle background track and rounded caps.
 */
@Composable
fun CircularWavySpinner(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 36.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.15f),
    waveCount: Int = 5,
    strokeWidth: Dp = 3.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "expressive_circular_spinner")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 45f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spinner_sweep"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val swPx = strokeWidth.toPx()
        val diameter = size.minDimension - swPx
        val topLeft = Offset(swPx / 2f, swPx / 2f)
        val arcSize = Size(diameter, diameter)

        // Subtle circular track
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = swPx, cap = StrokeCap.Round)
        )

        // Smooth dynamic rotating arc with rounded caps
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = swPx, cap = StrokeCap.Round)
        )
    }
}

/**
 * Radar Ripple Wave Loader (§7.4).
 * Concentric expanding radar rings with cascading opacity and scale.
 */
@Composable
fun RadarRippleLoader(
    modifier: Modifier = Modifier.size(64.dp),
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")

    val progress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ripple1"
    )

    val progress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ripple2"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxRadius = size.minDimension / 2f

            listOf(progress1, progress2).forEach { progress ->
                val radius = maxRadius * progress
                val alpha = (1f - progress).coerceIn(0f, 1f)
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}

/**
 * Rotating Geometric Arc Spinner (§7.3).
 */
@Composable
fun RotatingArcSpinner(
    modifier: Modifier = Modifier.size(36.dp),
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpinnerRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Canvas(modifier = modifier) {
        rotate(rotation) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Segmented Progress Bar (§7.5).
 */
@Composable
fun SegmentedProgressBar(
    totalSegments: Int = 5,
    completedSegments: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until totalSegments) {
            val isFilled = i < completedSegments
            val segmentColor = if (isFilled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(segmentColor)
            )
        }
    }
}

/**
 * Sweeping Gradient Progress Bar (§7.5).
 */
@Composable
fun SweepingGradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SweepTransition")
    val offsetAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepOffset"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary
        ),
        start = Offset(offsetAnimation, 0f),
        end = Offset(offsetAnimation + 300f, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(gradientBrush)
        )
    }
}

/**
 * Skeleton Shimmer Loading Modifier (§7.8).
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 200f, translateAnimation - 200f),
            end = Offset(translateAnimation, translateAnimation)
        )
    )
}

/**
 * Elastic Tooltip Slider (§7.6).
 * Interactive slider that pops and scales the thumb on press with a floating value bubble.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElasticTooltipSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    labelFormatter: (Float) -> String = { "${(it * 100).toInt()}%" }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val haptics = LocalHapticFeedback.current

    val thumbScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragged) 1.35f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "ThumbScale"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDragged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isDragged) 6.dp else 2.dp
            ) {
                Text(
                    text = labelFormatter(value),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDragged) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            valueRange = valueRange,
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    modifier = Modifier.scale(thumbScale)
                )
            }
        )
    }
}
