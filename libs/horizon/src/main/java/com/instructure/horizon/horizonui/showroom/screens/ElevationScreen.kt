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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun ElevationScreen() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(text = "Level 0", style = HorizonTypography.p2, modifier = Modifier.padding(top = 8.dp))
        Box(
            Modifier
                .size(100.dp, 50.dp)
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)
                .shadow(HorizonElevation.level0, shape = HorizonCornerRadius.level3, clip = true)
        )
        Text(text = "Level 1", style = HorizonTypography.p2, modifier = Modifier.padding(top = 8.dp))
        Box(
            Modifier
                .size(100.dp, 50.dp)
                .shadow(HorizonElevation.level1, shape = HorizonCornerRadius.level3, clip = true)
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)
        )
        Text(text = "Level 2", style = HorizonTypography.p2, modifier = Modifier.padding(top = 8.dp))
        Box(
            Modifier
                .size(100.dp, 50.dp)
                .shadow(HorizonElevation.level2, shape = HorizonCornerRadius.level3, clip = true)
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)

        )
        Text(text = "Level 3", style = HorizonTypography.p2, modifier = Modifier.padding(top = 8.dp))
        Box(
            Modifier
                .size(100.dp, 50.dp)
                .shadow(HorizonElevation.level3, shape = HorizonCornerRadius.level3, clip = true)
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)

        )
        Text(text = "Level 4", style = HorizonTypography.p2, modifier = Modifier.padding(top = 8.dp))
        Box(
            Modifier
                .size(100.dp, 50.dp)
                .shadow(HorizonElevation.level4, shape = HorizonCornerRadius.level3, clip = true)
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)
        )
        Text(text = "Level 5", style = HorizonTypography.p2, modifier = Modifier.padding(top = 8.dp))
        Box(
            Modifier
                .size(100.dp, 50.dp)
                .shadow(HorizonElevation.level5, shape = HorizonCornerRadius.level3, clip = true)
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ElevationScreenPreview() {
    ElevationScreen()
}