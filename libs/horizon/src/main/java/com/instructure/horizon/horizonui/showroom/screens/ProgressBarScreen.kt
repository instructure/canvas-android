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
package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.molecules.ProgressBarNumberStyle
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle

@Composable
fun ProgressBarScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProgressBar(progress = 50.0, numberStyle = ProgressBarNumberStyle.OUTSIDE)
        ProgressBar(progress = 50.0, numberStyle = ProgressBarNumberStyle.INSIDE)
        ProgressBar(progress = 50.0, numberStyle = ProgressBarNumberStyle.OFF)
        ProgressBarSmall(progress = 50.0, label = "Text", style = ProgressBarStyle.Dark())
        ProgressBarSmall(progress = 50.0, label = "Text", style = ProgressBarStyle.Light())
        ProgressBarSmall(
            progress = 50.0,
            label = "Text",
            style = ProgressBarStyle.Dark(overrideProgressColor = HorizonColors.PrimitivesGreen.green45())
        )
        ProgressBarSmall(
            progress = 50.0,
            label = "Text",
            style = ProgressBarStyle.Light(overrideProgressColor = HorizonColors.PrimitivesRed.red45()))
    }
}

@Composable
@Preview(showBackground = true)
fun ProgressBarScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBarScreen()
}