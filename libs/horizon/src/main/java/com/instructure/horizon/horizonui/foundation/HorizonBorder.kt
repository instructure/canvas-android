package com.instructure.horizon.horizonui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object HorizonBorder {
    fun level1(color: Color = HorizonColors.LineAndBorder.lineStroke()) = BorderStroke(1.dp, color)
    fun level2(color: Color = HorizonColors.LineAndBorder.lineStroke()) = BorderStroke(2.dp, color)
}

@Composable
fun Modifier.horizonBorder(
    color: Color,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return this
        .padding(start, top, end, bottom)
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val width = placeable.width - start.roundToPx() - end.roundToPx()
            val height = placeable.height - top.roundToPx() - bottom.roundToPx()

            layout(width, height) {
                placeable.place(-start.roundToPx(), -top.roundToPx())
            }
        }
        .dropShadow(RoundedCornerShape(cornerRadius)) {
            this.color = color.copy(0.1f)
            this.radius = 0f
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val width = placeable.width + start.roundToPx() + end.roundToPx()
            val height = placeable.height + top.roundToPx() + bottom.roundToPx()

            layout(width, height) {
                placeable.place(start.roundToPx(), top.roundToPx())
            }
        }
}

@Composable
fun Modifier.horizonBorderShadow(
    color: Color,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    val maxShadow = maxOf(start, top, end, bottom)

    return this
        .padding(start, top, end, bottom)
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val width = placeable.width - start.roundToPx() - end.roundToPx()
            val height = placeable.height - top.roundToPx() - bottom.roundToPx()

            layout(width, height) {
                placeable.place(-start.roundToPx(), -top.roundToPx())
            }
        }
        .dropShadow(RoundedCornerShape(cornerRadius)) {
            this.color = color.copy(0.1f)
            this.radius = maxShadow.toPx() / 2

            val offsetX = if (start == 0.dp) {
                this.radius = maxShadow.toPx() / 4
                radius
            } else if (end == 0.dp) {
                this.radius = maxShadow.toPx() / 4
                -radius
            } else {
                0f
            }
            val offsetY = if (top == 0.dp) {
                this.radius = maxShadow.toPx() / 4
                radius
            } else if (bottom == 0.dp) {
                this.radius = maxShadow.toPx() / 4
                -radius
            } else {
                0f
            }
            this.offset = Offset(offsetX, offsetY)
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val width = placeable.width + start.roundToPx() + end.roundToPx()
            val height = placeable.height + top.roundToPx() + bottom.roundToPx()

            layout(width, height) {
                placeable.place(start.roundToPx(), top.roundToPx())
            }
        }
}

@Composable
fun Modifier.horizonBorder(
    color: Color,
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return horizonBorder(color, horizontal, vertical, horizontal, vertical, cornerRadius)
}

@Composable
fun Modifier.horizonBorder(
    color: Color,
    all: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return horizonBorder(color, all, all, cornerRadius)
}

@Composable
fun Modifier.horizonBorderShadow(
    color: Color,
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return horizonBorderShadow(color, horizontal, vertical, horizontal, vertical, cornerRadius)
}

@Composable
fun Modifier.horizonBorderShadow(
    color: Color,
    all: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return horizonBorderShadow(color, all, all, cornerRadius)
}

