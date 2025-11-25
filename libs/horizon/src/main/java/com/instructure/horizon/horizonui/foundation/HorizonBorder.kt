package com.instructure.horizon.horizonui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    start: Dp,
    top: Dp,
    end: Dp,
    bottom: Dp,
    cornerRadius: Dp,
): Modifier {
    return drawBehind {
        drawRoundRect(
            color = color,
            topLeft = Offset(
                x = -start.toPx(),
                y = -top.toPx()
            ),
            size = Size(
                width = size.width + start.toPx() + end.toPx(),
                height = size.height + top.toPx() + bottom.toPx()
            ),
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        )
    }
}

@Composable
fun Modifier.horizonBorderShadow(
    color: Color,
    start: Dp,
    top: Dp,
    end: Dp,
    bottom: Dp,
    cornerRadius: Dp,
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
    horizontal: Dp,
    vertical: Dp,
    cornerRadius: Dp,
): Modifier {
    return horizonBorder(color, horizontal, vertical, horizontal, vertical, cornerRadius)
}

@Composable
fun Modifier.horizonBorder(
    color: Color,
    all: Dp,
    cornerRadius: Dp,
): Modifier {
    return horizonBorder(color, all, all, cornerRadius)
}

@Composable
fun Modifier.horizonBorderShadow(
    color: Color,
    horizontal: Dp,
    vertical: Dp,
    cornerRadius: Dp,
): Modifier {
    return horizonBorderShadow(color, horizontal, vertical, horizontal, vertical, cornerRadius)
}

@Composable
fun Modifier.horizonBorderShadow(
    color: Color,
    all: Dp,
    cornerRadius: Dp,
): Modifier {
    return horizonBorderShadow(color, all, all, cornerRadius)
}

