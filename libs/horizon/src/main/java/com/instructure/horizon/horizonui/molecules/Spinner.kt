/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.horizonui.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors

enum class SpinnerSize(val size: Dp, val strokeWidth: Dp) {
    LARGE(112.dp, 8.dp),
    MEDIUM(80.dp, 6.dp),
    SMALL(48.dp, 4.dp),
    EXTRA_SMALL(24.dp, 2.dp)
}

@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    size: SpinnerSize = SpinnerSize.SMALL,
    color: Color = HorizonColors.Surface.institution(),
    hasStrokeBackground: Boolean = false,
    progress: Float? = null,
) {
    val strokeBackground = if (hasStrokeBackground) HorizonColors.LineAndBorder.lineDivider() else Color.Transparent
    Box(
        modifier = modifier
            .testTag("LoadingSpinner"),
        contentAlignment = Alignment.Center
    ) {
        if (progress != null) {
            CircularProgressIndicator(
                color = color,
                modifier = Modifier.size(size.size),
                strokeWidth = size.strokeWidth,
                trackColor = strokeBackground,
                progress = {
                    progress
                })
        } else {
            CircularProgressIndicator(
                color = color,
                modifier = Modifier.size(size.size),
                strokeWidth = size.strokeWidth,
                trackColor = strokeBackground
            )
        }
    }
}

@Preview
@Composable
private fun SpinnerPreview() {
    ContextKeeper.appContext = LocalContext.current
    Spinner()
}

@Preview
@Composable
private fun SpinnerWithBackgroundPreview() {
    ContextKeeper.appContext = LocalContext.current
    Spinner(hasStrokeBackground = true)
}

@Preview
@Composable
private fun SpinnerWithBackgroundExtraSmallPreview() {
    ContextKeeper.appContext = LocalContext.current
    Spinner(hasStrokeBackground = true, size = SpinnerSize.EXTRA_SMALL)
}

@Preview
@Composable
private fun SpinnerWithBackgroundMediumPreview() {
    ContextKeeper.appContext = LocalContext.current
    Spinner(hasStrokeBackground = true, size = SpinnerSize.MEDIUM)
}

@Preview
@Composable
private fun SpinnerWithBackgroundLargePreview() {
    ContextKeeper.appContext = LocalContext.current
    Spinner(hasStrokeBackground = true, size = SpinnerSize.LARGE)
}

@Preview
@Composable
private fun SpinnerWithProgressPreview() {
    ContextKeeper.appContext = LocalContext.current
    Spinner(hasStrokeBackground = true, size = SpinnerSize.LARGE, progress = 0.5f)
}