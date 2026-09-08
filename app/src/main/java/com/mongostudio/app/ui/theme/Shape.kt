package com.mongostudio.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

// Playful Material 3 Expressive Shape Tokens
val PillShape = RoundedCornerShape(percent = 50)
val SquircleLarge = RoundedCornerShape(28.dp)
val SquircleMedium = RoundedCornerShape(20.dp)
val SquircleSmall = RoundedCornerShape(14.dp)

// Playful Asymmetrical Shape (Signature MD3 Expressive)
val AsymmetricCardShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 10.dp,
    bottomStart = 10.dp,
    bottomEnd = 28.dp
)

val AsymmetricHeaderShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 28.dp,
    bottomEnd = 28.dp
)

// Split Button Shapes
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

// Connected Button Group Shapes
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
