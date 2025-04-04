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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.SegmentedControl
import com.instructure.horizon.horizonui.molecules.SegmentedControlIconPosition

@Composable
fun SegmentedControlScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val iconPositions = listOf(
            SegmentedControlIconPosition.NoIcon(),
            SegmentedControlIconPosition.End(iconRes = R.drawable.add),
            SegmentedControlIconPosition.Start(iconRes = R.drawable.add),
            SegmentedControlIconPosition.NoIcon(checkmark = true),
            SegmentedControlIconPosition.End(checkmark = true, iconRes = R.drawable.add),
            SegmentedControlIconPosition.Start(checkmark = true, iconRes = R.drawable.add),
        )

        iconPositions.forEach { iconPos ->
            var firstSelected by remember { mutableIntStateOf(0) }
            var secondSelected by remember { mutableIntStateOf(0) }
            SegmentedControl(
                options = listOf("Item 1", "Item 2", "Item 3"),
                selectedIndex = firstSelected,
                onItemSelected = {
                    firstSelected = it
                },
                iconPosition = iconPos
            )
            SegmentedControl(
                options = listOf("Item 1", "Item 2"),
                selectedIndex = secondSelected,
                onItemSelected = {
                    secondSelected = it
                },
                iconPosition = iconPos,
            )
        }
    }
}