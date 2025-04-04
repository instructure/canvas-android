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
@file:OptIn(ExperimentalLayoutApi::class)

package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType

@Composable
fun PillScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PillType.entries.forEach { type ->
            Text(text = type.name, style = HorizonTypography.p2)
            FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillStyle.entries.forEach { style ->
                    Pill("Default", style = style, type = type, case = PillCase.UPPERCASE, size = PillSize.REGULAR)
                    Pill("Default", style = style, type = type, case = PillCase.TITLE, size = PillSize.SMALL, iconRes = R.drawable.calendar_month)
                }
            }
        }
    }
}

@Composable
@Preview
fun PillScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    PillScreen()
}