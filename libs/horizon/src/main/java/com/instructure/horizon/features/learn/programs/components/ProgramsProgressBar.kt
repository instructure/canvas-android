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
package com.instructure.horizon.features.learn.programs.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle

@Composable
fun ProgramsProgressBar(
    progress: Double,
    modifier: Modifier = Modifier,
    progressBarStyle: ProgressBarStyle = ProgressBarStyle.Light(overrideProgressColor = HorizonColors.Surface.institution())
) {
    Column(modifier = modifier) {
        if (progress > 0.0) {
            Text(
                stringResource(R.string.programsProgressBar_percentComplete, NumberHelper.doubleToPercentage(progress, 0)),
                style = HorizonTypography.p2,
                color = HorizonColors.Surface.institution()
            )
        } else {
            Text(
                stringResource(R.string.programsProgressBar_notStarted),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.title()
            )
        }
        HorizonSpace(SpaceSize.SPACE_8)
        ProgressBarSmall(
            progress,
            style = progressBarStyle,
            showLabels = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgramsProgressBarPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgramsProgressBar(
        progress = 75.0,
    )
}

@Preview(showBackground = true)
@Composable
fun ProgramsProgressBarNotStartedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgramsProgressBar(
        progress = 0.0,
    )
}