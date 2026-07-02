/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.instui.compose.indicator

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.component.InstUISpinner
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI indeterminate spinner.
 *
 * Usage:
 * ```
 * Spinner()
 * Spinner(size = InstUISpinner.spinnerSizeLg)
 * Spinner(color = InstUISpinner.inverseColor)
 * ```
 */
@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    size: Dp = InstUISpinner.spinnerSizeSm,
    strokeWidth: Dp = InstUISpinner.strokeWidthSm,
    color: Color = InstUISpinner.color,
    trackColor: Color = InstUISpinner.trackColor,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
    )
}

@Preview(name = "Spinner — Light", showBackground = true)
@Preview(name = "Spinner — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SpinnerPreview() {
    InstUITheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            Spinner(size = InstUISpinner.spinnerSizeXs, strokeWidth = InstUISpinner.strokeWidthXs)
            Spinner(size = InstUISpinner.spinnerSizeSm, strokeWidth = InstUISpinner.strokeWidthSm)
            Spinner(size = InstUISpinner.spinnerSizeMd, strokeWidth = InstUISpinner.strokeWidthMd)
        }
    }
}
