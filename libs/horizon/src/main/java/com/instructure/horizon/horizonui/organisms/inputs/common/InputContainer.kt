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
package com.instructure.horizon.horizonui.organisms.inputs.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius

@Composable
fun InputContainer(
    isFocused: Boolean,
    isError: Boolean,
    isDisabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .drawBehind {
                if (isFocused) {
                    val strokeColor =
                        if (isError) HorizonColors.Surface.error() else HorizonColors.Surface.institution()
                    val stroke = HorizonBorder.level2(strokeColor)
                    val radius = 12.dp
                    val borderPadding = 2.dp

                    drawRoundRect(
                        color = strokeColor, // your "selected" outer border
                        size = Size(
                            size.width + stroke.width.toPx() + borderPadding.toPx() * 2,
                            size.height + stroke.width.toPx() + borderPadding.toPx() * 2
                        ),
                        topLeft = Offset(-stroke.width.toPx() / 2 - borderPadding.toPx(), -stroke.width.toPx() / 2 - borderPadding.toPx()),
                        cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
                        style = Stroke(width = stroke.width.toPx())
                    )
                }
            }
            .clip(HorizonCornerRadius.level1_5)
            .background(HorizonColors.Surface.cardPrimary())
            .border(
                HorizonBorder.level1(if (isError) HorizonColors.Surface.error() else HorizonColors.LineAndBorder.containerStroke()),
                HorizonCornerRadius.level1_5
            )
            .alpha(if (isDisabled) 0.5f else 1f)
    ) {
        content()
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputContainerPreview() {
    ContextKeeper.appContext = LocalContext.current
    InputContainer(
        isFocused = false,
        isError = false,
        isDisabled = false,
    ) {
        Text(
            "Placeholder",
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputContainerFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    InputContainer(
        isFocused = true,
        isError = false,
        isDisabled = false,
    ) {
        Text(
            "Placeholder",
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputContainerErrorFocusedPreview() {
    ContextKeeper.appContext = LocalContext.current
    InputContainer(
        isFocused = true,
        isError = true,
        isDisabled = false,
    ) {
        Text(
            "Placeholder",
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputContainerErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    InputContainer(
        isFocused = false,
        isError = true,
        isDisabled = false,
        modifier = Modifier.graphicsLayer { clip = false }
    ) {
        Text(
            "Placeholder",
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
fun InputContainerDisabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    InputContainer(
        isFocused = false,
        isError = false,
        isDisabled = true,
    ) {
        Text(
            "Placeholder",
            modifier = Modifier.padding(8.dp)
        )
    }
}