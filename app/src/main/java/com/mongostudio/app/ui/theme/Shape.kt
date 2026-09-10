package com.mongostudio.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

// Playful Material 3 Expressive Shape Tokens (§4.4)
val PillShape = RoundedCornerShape(percent = 50)
val SquircleLarge = RoundedCornerShape(26.dp)
val SquircleMedium = RoundedCornerShape(18.dp)
val SquircleSmall = RoundedCornerShape(8.dp)

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
    bottomStart = 26.dp,
    bottomEnd = 26.dp
)

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
