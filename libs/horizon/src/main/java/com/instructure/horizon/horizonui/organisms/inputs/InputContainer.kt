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
package com.instructure.horizon.horizonui.organisms.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.organisms.inputs.sizes.InputSize

@Composable
fun InputContainer(
    isFocused: Boolean,
    isError: Boolean,
    isDisabled: Boolean,
    size: InputSize,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .clip(HorizonCornerRadius.level1_5)
            .then(
                if (isFocused)
                    Modifier
                        .border(
                            HorizonBorder.level2(if (isError) HorizonColors.Surface.error() else HorizonColors.Surface.institution()),
                            HorizonCornerRadius.level1_5
                        )
                        .background(HorizonColors.Surface.cardPrimary())
                else
                    Modifier
            )
            .alpha(if (isDisabled) 0.5f else 1f)
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(HorizonCornerRadius.level1_5)
                .background(HorizonColors.Surface.cardPrimary())
                .border(
                    HorizonBorder.level1(if (isError) HorizonColors.Surface.error() else HorizonColors.LineAndBorder.containerStroke()),
                    HorizonCornerRadius.level1_5
                )
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        vertical = size.verticalPadding,
                        horizontal = size.horizontalPadding
                    )
            ){
                content()
            }
        }
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
        size = object : InputSize {
            override val verticalPadding: Dp
                get() = 8.dp
            override val horizontalPadding: Dp
                get() = 12.dp
        },
    ) {
        Text("Placeholder")
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
        size = object : InputSize {
            override val verticalPadding: Dp
                get() = 8.dp
            override val horizontalPadding: Dp
                get() = 12.dp
        },
    ) {
        Text("Placeholder")
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
        size = object : InputSize {
            override val verticalPadding: Dp
                get() = 8.dp
            override val horizontalPadding: Dp
                get() = 12.dp
        },
    ) {
        Text("Placeholder")
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
        size = object : InputSize {
            override val verticalPadding: Dp
                get() = 8.dp
            override val horizontalPadding: Dp
                get() = 12.dp
        },
    ) {
        Text("Placeholder")
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
        size = object : InputSize {
            override val verticalPadding: Dp
                get() = 8.dp
            override val horizontalPadding: Dp
                get() = 12.dp
        },
    ) {
        Text("Placeholder")
    }
}