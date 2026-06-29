package com.instructure.horizon.horizonui.foundation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

object HorizonBorder {
    fun level1(color: Color = HorizonColors.LineAndBorder.lineStroke()) = BorderStroke(1.dp, color)
    fun level2(color: Color = HorizonColors.LineAndBorder.lineStroke()) = BorderStroke(2.dp, color)
}

fun Modifier.horizonBorder(
    color: Color,
    backgroundColor: Color,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .padding(start, top, end, bottom)
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val width = (placeable.width - start.roundToPx() - end.roundToPx()).coerceAtLeast(0)
            val height = (placeable.height - top.roundToPx() - bottom.roundToPx()).coerceAtLeast(0)

            layout(width, height) {
                placeable.place(-start.roundToPx(), -top.roundToPx())
            }
        }
        .dropShadow(shape) {
            this.color = color
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
        .background(backgroundColor, shape)
        .clip(shape)
}

fun Modifier.horizonBorderShadow(
    color: Color,
    backgroundColor: Color,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
    blur: Dp = maxOf((start + end) / 2, (top + bottom) / 2),
    cornerRadius: Dp = 0.dp,
    shadowAlpha: Float = 0.1f,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val maxExtent = maxOf((start + end) / 2, (top + bottom) / 2)
    val effectiveBlur = if (blur > maxExtent) maxExtent else blur
    val spreadAmount = maxExtent - effectiveBlur

    return this
        .clip(shape)
        .padding(start, top, end, bottom)
        .dropShadow(shape) {
            this.color = color.copy(alpha = color.alpha * shadowAlpha)
            this.radius = effectiveBlur.toPx()
            this.spread = spreadAmount.toPx()
            this.offset = Offset(
                x = ((end - start) / 2).toPx(),
                y = ((bottom - top) / 2).toPx(),
            )
        }
        .background(backgroundColor, shape)
        .clip(shape)
}

fun Modifier.horizonBorder(
    color: Color,
    backgroundColor: Color,
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return horizonBorder(color, backgroundColor, horizontal, vertical, horizontal, vertical, cornerRadius)
}

fun Modifier.horizonBorder(
    color: Color,
    backgroundColor: Color,
    all: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
): Modifier {
    return horizonBorder(color, backgroundColor, all, all, cornerRadius)
}

fun Modifier.horizonBorderShadow(
    color: Color,
    backgroundColor: Color,
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
    blur: Dp = maxOf(horizontal, vertical),
    cornerRadius: Dp = 0.dp,
    shadowAlpha: Float = 0.1f,
): Modifier {
    return horizonBorderShadow(color, backgroundColor, horizontal, vertical, horizontal, vertical, blur, cornerRadius, shadowAlpha)
}

fun Modifier.horizonBorderShadow(
    color: Color,
    backgroundColor: Color,
    all: Dp = 0.dp,
    blur: Dp = all,
    cornerRadius: Dp = 0.dp,
    shadowAlpha: Float = 0.1f,
): Modifier {
    return horizonBorderShadow(color, backgroundColor, all, all, all, all, blur, cornerRadius, shadowAlpha)
}

@Composable
fun Modifier.horizonTopBarShadow(
    scrollState: ScrollableState,
    backgroundColor: Color,
    color: Color = HorizonColors.Surface.inversePrimary(),
    bottom: Dp = 1.dp,
    cornerRadius: Dp = 0.dp,
    maxAlpha: Float = 0.1f,
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val animatedBottom by animateDpAsState(
        targetValue = if (scrollState.canScrollBackward) bottom else 0.dp,
        label = "horizonTopBarShadowBottom",
    )
    return this
        .zIndex(1f)
        .dropShadow(shape) {
            this.color = color.copy(alpha = color.alpha * maxAlpha)
            this.radius = animatedBottom.toPx()
            this.offset = Offset(0f, animatedBottom.toPx())
        }
        .background(backgroundColor, shape)
}

@Preview(showBackground = true, backgroundColor = 0xFFCCCCCC, widthDp = 360, heightDp = 900)
@Composable
private fun HorizonBorderPreview() {
    val border = Color(0xFF0E68B3)
    val shadow = Color.Black
    val card = Color(0xFFFFFFFF)
    val r16 = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(48.dp).horizonBorder(border, card, bottom = 1.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).horizonBorder(border, card, top = 1.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).horizonBorder(border, card, all = 2.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).horizonBorder(border, card, all = 2.dp, cornerRadius = 16.dp).clip(r16))
        Box(Modifier.fillMaxWidth().height(96.dp).horizonBorder(border, card, 6.dp, 1.dp, 1.dp, 6.dp, 16.dp).clip(r16))
        Box(Modifier.fillMaxWidth().height(48.dp).horizonBorderShadow(shadow, card, bottom = 1.dp))
        Box(Modifier.fillMaxWidth().height(80.dp).horizonBorderShadow(shadow, card, bottom = 8.dp))
        Box(Modifier.fillMaxWidth().height(96.dp).horizonBorderShadow(shadow, card, all = 8.dp, cornerRadius = 16.dp).clip(r16))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFCCCCCC, widthDp = 360, heightDp = 320)
@Composable
private fun HorizonTopBarShadowPreview() {
    val unscrolled = rememberScrollState(initial = 0)
    val scrolled = rememberScrollState(initial = 1)
    val card = Color(0xFFFFFFFF)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .horizonTopBarShadow(scrollState = unscrolled, backgroundColor = card)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .horizonTopBarShadow(scrollState = scrolled, backgroundColor = card)
        )
    }
}
