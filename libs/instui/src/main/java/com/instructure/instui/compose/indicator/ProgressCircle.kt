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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIProgressCircle
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI progress circle (donut meter).
 *
 * Draws a track ring and a meter arc representing [progress] (0f..1f), with an
 * optional centered [content] slot (e.g. a score label or an icon).
 *
 * Usage:
 * ```
 * ProgressCircle(progress = 0.85f) {
 *     Text(text = "85")
 * }
 * ```
 */
@Composable
fun ProgressCircle(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = InstUIProgressCircle.smallSize,
    strokeWidth: Dp = InstUIProgressCircle.smallStrokeWidth,
    meterColor: Color = InstUIProgressCircle.meterColorBrand,
    trackColor: Color = InstUIProgressCircle.trackBorderColor,
    content: @Composable () -> Unit = {},
) {
    val safeProgress = progress.coerceIn(0f, 1f).let { if (it.isNaN()) 0f else it }
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = meterColor,
                startAngle = -90f,
                sweepAngle = 360f * safeProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Preview(name = "ProgressCircle — Light", showBackground = true)
@Preview(name = "ProgressCircle — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProgressCirclePreview() {
    InstUITheme {
        Box(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(24.dp),
        ) {
            ProgressCircle(progress = 0.72f) {
                Text(text = "72", style = InstUIText.contentImportant)
            }
        }
    }
}
