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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun CornerRadiusScreen() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(text = "Level 0 - 0 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level0).padding(8.dp))

        Text(text = "Level 1 - 8 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level1).padding(8.dp))

        Text(text = "Level 1.5 - 12 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level1_5).padding(8.dp))

        Text(text = "Level 2 - 16 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level2).padding(8.dp))

        Text(text = "Level 3 - 16 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level3).padding(8.dp))

        Text(text = "Level 3 Top - 16 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level3Top).padding(8.dp))

        Text(text = "Level 3 Bottom - 16 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level3Bottom).padding(8.dp))

        Text(text = "Level 3.5 - 24 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level3_5).padding(8.dp))

        Text(text = "Level 4 - 32 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level4).padding(8.dp))

        Text(text = "Level 5 - 32 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level5).padding(8.dp))

        Text(text = "Level 6 - 100 px", style = HorizonTypography.p2)
        Box(Modifier.size(100.dp, 50.dp).background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level6).padding(8.dp))
    }
}

@Composable
@Preview(showBackground = true)
fun CornerRadiusScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    CornerRadiusScreen()
}