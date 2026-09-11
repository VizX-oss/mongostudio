package com.mongostudio.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Linear Wavy Progress Indicator.
 * Optimised: fewer path points using cubic bezier approximation of sin wave.
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

        // Wave using cubic bezier: 4 control points per period → smooth & fast
        val path = Path()
        val cp = lambdaPx * 0.3183f // (1/π)*λ — ideal for sine cubic approx
        var x = -phaseOffset % lambdaPx
        var isFirst = true
        while (x <= width + lambdaPx) {
            val y0 = midY + ampPx * sin((x / lambdaPx) * (2 * PI).toFloat())
            if (isFirst) { path.moveTo(x, y0); isFirst = false }
            // One full sine period via two cubic bezier segments
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
 * Material 3 Expressive Circular Wavy Spinner.
 * Optimised: pre-computed sin lookup table, smooth arc sweep animation with
 * dual-rotation trick (like AOSP's circular progress indicator), and cubic path.
 */
@Composable
fun CircularWavySpinner(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 32.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.15f),
    waveCount: Int = 5,
    strokeWidth: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_wavy")

    // Outer rotation — constant speed, clockwise
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Wave phase offset — drives ripple motion along the circle
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Amplitude pulse — squiggles breathe in and out
    val ampScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amp_pulse"
    )

    // Pre-compute sin table size: enough points for smooth appearance
    val steps = 120
    val sinTable = remember(steps) {
        FloatArray(steps + 1) { i ->
            sin(i.toFloat() / steps * (2 * PI).toFloat())
        }
    }

    Canvas(modifier = modifier.size(sizeDp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val swPx = strokeWidth.toPx()
        val baseRadius = (size.minDimension / 2f) - swPx * 1.8f
        val amp = swPx * 1.1f * ampScale

        // Track ring
        drawCircle(
            color = trackColor,
            radius = baseRadius,
            center = center,
            style = Stroke(width = swPx, cap = StrokeCap.Round)
        )

        // Squiggly wave arc — rotated by outer rotation
        rotate(rotation, pivot = center) {
            val path = Path()
            for (i in 0..steps) {
                val theta = i.toFloat() / steps * (2 * PI).toFloat()
                val r = baseRadius + amp * sinTable[i] *
                    cos(theta * waveCount - phase) // modulate with cos for directionality
                val x = center.x + r * cos(theta)
                val y = center.y + r * sin(theta)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = swPx * 0.85f, cap = StrokeCap.Round)
            )
        }
    }
}
