package com.mongostudio.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mongostudio.app.ui.theme.EmeraldLight
import com.mongostudio.app.ui.theme.EmeraldPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Wavy Progress Indicator.
 * Draws a playful, organic sinusoidal wave that morphs and ripples.
 */
@Composable
fun WavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = EmeraldLight,
    trackColor: Color = color.copy(alpha = 0.2f),
    waveAmplitude: Dp = 4.dp,
    waveLength: Dp = 24.dp,
    strokeWidth: Dp = 3.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
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

        // Draw track
        drawLine(
            color = trackColor,
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        // Draw animated wavy line
        val path = Path()
        var first = true
        var x = 0f
        val step = 3f

        while (x <= width) {
            val y = midY + ampPx * sin((x / lambdaPx) * 2 * PI.toFloat() - phase)
            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }
            x += step
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

/**
 * Circular Wavy Spinner in Material 3 Expressive style.
 * Draws an organic sinusoidal wave revolving in a loop.
 */
@Composable
fun CircularWavySpinner(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 32.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.2f),
    waveCount: Int = 6,
    strokeWidth: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_wavy_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_anim"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.minDimension / 2f) - (strokeWidth.toPx() * 1.5f)
        val amp = strokeWidth.toPx() * 0.75f
        val rotRad = Math.toRadians(rotation.toDouble()).toFloat()

        // Background circular track
        drawCircle(
            color = trackColor,
            radius = baseRadius,
            center = center,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // Animated squiggly wave
        val path = Path()
        val steps = 100
        for (i in 0..steps) {
            val theta = (i.toFloat() / steps) * (2 * PI).toFloat()
            val r = baseRadius + amp * sin(theta * waveCount + rotRad)
            val x = center.x + r * cos(theta)
            val y = center.y + r * sin(theta)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}
