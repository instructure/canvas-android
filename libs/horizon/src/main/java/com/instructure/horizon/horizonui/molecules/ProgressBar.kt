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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import kotlin.math.roundToInt

enum class ProgressBarNumberStyle {
    INSIDE,
    OUTSIDE,
    OFF
}

@Composable
fun ProgressBar(progress: Double, modifier: Modifier = Modifier, numberStyle: ProgressBarNumberStyle = ProgressBarNumberStyle.OUTSIDE) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier
                .border(width = 2.dp, shape = HorizonCornerRadius.level6, color = HorizonColors.Surface.institution())
                .height(28.dp)
                .weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level6)
                    .fillMaxWidth((progress.toFloat() / 100f))
                    .height(28.dp)
                    .padding(end = 8.dp)
            ) {
                if (numberStyle == ProgressBarNumberStyle.INSIDE && progress >= 10.0) {
                    ProgressBarNumber(progress = progress, color = HorizonColors.PrimitivesWhite.white10())
                }
            }
        }
        if (numberStyle == ProgressBarNumberStyle.OUTSIDE) {
            HorizonSpace(SpaceSize.SPACE_8)
            ProgressBarNumber(progress = progress, color = HorizonColors.Surface.institution())
        }
    }
}

@Composable
private fun ProgressBarNumber(progress: Double, color: Color) {
    Text(
        text = stringResource(R.string.progressBar_percent, progress.roundToInt()),
        style = HorizonTypography.buttonTextMedium,
        color = color
    )
}

sealed class ProgressBarStyle(val textColor: Color, val progressColor: Color) {
    data class Light(val overrideProgressColor: Color = HorizonColors.Surface.cardPrimary()) :
        ProgressBarStyle(HorizonColors.Text.surfaceColored(), overrideProgressColor)

    data class Dark(val overrideProgressColor: Color = HorizonColors.Surface.inverseSecondary()) :
        ProgressBarStyle(HorizonColors.Text.body(), overrideProgressColor)
}

@Composable
fun ProgressBarSmall(progress: Double, label: String, modifier: Modifier = Modifier, style: ProgressBarStyle = ProgressBarStyle.Dark()) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = HorizonTypography.p2,
                color = style.textColor
            )
            Text(
                text = stringResource(R.string.progressBar_percent, progress.roundToInt()),
                style = HorizonTypography.p2,
                color = style.textColor
            )
        }
        Box(
            modifier
                .background(shape = HorizonCornerRadius.level1, color = HorizonColors.LineAndBorder.lineStroke())
                .fillMaxWidth()
                .height(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(color = style.progressColor, shape = HorizonCornerRadius.level1)
                    .fillMaxWidth((progress.toFloat() / 100f))
                    .height(8.dp)
            )
        }
    }
}

@Composable
@Preview
private fun ProgressBarOutsidePreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBar(progress = 50.0, numberStyle = ProgressBarNumberStyle.OUTSIDE)
}

@Composable
@Preview
private fun ProgressBarInsidePreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBar(progress = 50.0, numberStyle = ProgressBarNumberStyle.INSIDE)
}

@Composable
@Preview
private fun ProgressBarOffPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBar(progress = 50.0, numberStyle = ProgressBarNumberStyle.OFF)
}

@Composable
@Preview(showBackground = true)
private fun ProgressBarSmallDarkPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBarSmall(progress = 50.0, label = "Text", style = ProgressBarStyle.Dark())
}

@Composable
@Preview
private fun ProgressBarSmallLightPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBarSmall(progress = 50.0, label = "Text", style = ProgressBarStyle.Light())
}

@Composable
@Preview(showBackground = true)
private fun ProgressBarSmallDarkCustomColorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBarSmall(
        progress = 50.0,
        label = "Text",
        style = ProgressBarStyle.Dark(overrideProgressColor = HorizonColors.PrimitivesGreen.green45())
    )
}

@Composable
@Preview
private fun ProgressBarSmallLightCustomColorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBarSmall(
        progress = 50.0,
        label = "Text",
        style = ProgressBarStyle.Light(overrideProgressColor = HorizonColors.PrimitivesRed.red45())
    )
}