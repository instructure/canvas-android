package com.instructure.horizon.horizonui.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object HorizonElevation {
    val level0 = 0.0.dp
    val level1 = 1.0.dp
    val level2 = 3.0.dp
    val level3 = 6.0.dp
    val level4 = 8.0.dp
    val level5 = 12.0.dp
}

fun Modifier.horizonShadow(elevation: Dp, shape: Shape = RectangleShape, clip: Boolean = elevation > 0.dp): Modifier {
    return this.shadow(
        elevation,
        shape = shape,
        clip = clip,
        ambientColor = HorizonColors.Surface.inversePrimary().copy(alpha = 0.7f),
        spotColor = HorizonColors.Surface.inversePrimary().copy(alpha = 0.7f)
    )
}