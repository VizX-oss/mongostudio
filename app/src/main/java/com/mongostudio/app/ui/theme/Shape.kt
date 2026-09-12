package com.mongostudio.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Material 3 Expressive Playful Shapes
val PillShape = CircleShape
val SquircleLarge = RoundedCornerShape(26.dp)
val SquircleMedium = RoundedCornerShape(18.dp)
val SquircleSmall = RoundedCornerShape(8.dp)

val AsymmetricHeroShape = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 8.dp,
    bottomStart = 32.dp,
    bottomEnd = 32.dp
)

val AsymmetricCardShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 10.dp,
    bottomStart = 10.dp,
    bottomEnd = 28.dp
)

val AsymmetricHeaderShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 24.dp,
    bottomEnd = 24.dp
)

val TicketShape = CutCornerShape(topStart = 16.dp, topEnd = 16.dp)

// Split Button Shapes (§7.5)
val SplitButtonLeadingShape = RoundedCornerShape(
    topStart = 24.dp,
    bottomStart = 24.dp,
    topEnd = 6.dp,
    bottomEnd = 6.dp
)

val SplitButtonTrailingShape = RoundedCornerShape(
    topStart = 6.dp,
    bottomStart = 6.dp,
    topEnd = 24.dp,
    bottomEnd = 24.dp
)

// Connected Button Group Shapes (§7.2)
val ConnectedGroupStartShape = RoundedCornerShape(
    topStart = 20.dp,
    bottomStart = 20.dp,
    topEnd = 6.dp,
    bottomEnd = 6.dp
)

val ConnectedGroupCenterShape = RoundedCornerShape(6.dp)

val ConnectedGroupEndShape = RoundedCornerShape(
    topStart = 6.dp,
    bottomStart = 6.dp,
    topEnd = 20.dp,
    bottomEnd = 20.dp
)

/**
 * Shape Morphing implementation using AndroidX Graphics Shapes (§5.6).
 */
class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    private val androidPath = android.graphics.Path()

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        androidPath.reset()
        val matrix = android.graphics.Matrix()
        matrix.setScale(size.width, size.height)
        morph.toPath(progress = progress, path = androidPath)
        androidPath.transform(matrix)
        return Outline.Generic(androidPath.asComposePath())
    }
}

object ExpressiveShapesCatalog {
    fun scallopedPolygon(): RoundedPolygon = RoundedPolygon(
        numVertices = 12,
        rounding = CornerRounding(radius = 0.4f, smoothing = 0.2f)
    )

    fun pillPolygon(): RoundedPolygon = RoundedPolygon(
        numVertices = 4,
        rounding = CornerRounding(radius = 0.5f)
    )

    fun createMorph(start: RoundedPolygon, end: RoundedPolygon): Morph = Morph(start, end)
}
