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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.molecules.Tag
import com.instructure.horizon.horizonui.molecules.TagSize
import com.instructure.horizon.horizonui.molecules.TagType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TagType.entries.forEach { type ->
            TagSize.entries.forEach { size ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag("Default", type = type, size = size)
                    Tag("Default", type = type, size = size, dismissible = true)
                    Tag("Default", type = type, size = size, enabled = false)
                }
            }
        }
    }
}

@Composable
@Preview
fun TagScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    TagScreen()
}