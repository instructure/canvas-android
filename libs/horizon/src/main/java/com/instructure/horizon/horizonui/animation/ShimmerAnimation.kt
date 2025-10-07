/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.horizonui.animation

import androidx.annotation.FloatRange
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius


@Composable
fun Modifier.shimmerEffect(
    enabled: Boolean,
    iterationDurationMillis: Int = 1000,
    shape: Shape = HorizonCornerRadius.level1,
    backgroundColor: Color = HorizonColors.PrimitivesGrey.grey14().copy(alpha = 0.5f),
    shimmerColor: Color = HorizonColors.PrimitivesGrey.grey12().copy(alpha = 0.5f),
    @FloatRange(from = 0.0, to = 1.0) shimmerRatio: Float = 0.5f,
): Modifier {
    if (!enabled) return this
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition("ShimmerAnimationTransition")

    val startOffsetX by transition.animateFloat(
        initialValue = -size.width.toFloat(),
        targetValue = size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(iterationDurationMillis)
        ),
        label = "ShimmerAnimation"
    )

    val backgroundFirstPart = (1 / shimmerRatio / 2).toInt()
    val backgroundSecondPart = ((1 / shimmerRatio) - backgroundFirstPart).toInt()
    val colors = buildList {
        repeat(backgroundFirstPart) { add(backgroundColor) }
        add(shimmerColor)
        repeat(backgroundSecondPart) { add(backgroundColor) }
    }

    return clip(shape).background(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        )
        .onGloballyPositioned {
            size = it.size
        }
}