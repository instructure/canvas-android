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
package com.instructure.horizon.features.skillspace

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.instructure.horizon.horizonui.organisms.inputs.multi_select.MultiSelect
import com.instructure.horizon.horizonui.organisms.inputs.multi_select.MultiSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.multi_select.MultiSelectState
import com.instructure.horizon.horizonui.organisms.inputs.single_select.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.single_select.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.single_select.SingleSelectState

@Composable
fun SkillspaceScreen() {
    Column {
        var isOpen by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf<String?>(null) }
        var isFocused by remember { mutableStateOf(false) }
        SingleSelect(
            state = SingleSelectState(
                label = "Label",
                placeHolderText = "Placeholder",
                isFocused = isFocused,
                isDisabled = false,
                isMenuOpen = isOpen,
                errorText = null,
                size = SingleSelectInputSize.Medium,
                options = listOf("Option 1", "Option 2", "Option 3"),
                selectedOption = selected,
                onOptionSelected = { selected = it },
                onMenuOpenChanged = { isOpen = it },
                onFocusChanged = { isFocused = it },
            )
        )

        var isFocusedMulti by remember { mutableStateOf(false) }
        var isOpenMulti by remember { mutableStateOf(false) }
        var selectedMulti by remember { mutableStateOf<List<String>>(emptyList()) }
        MultiSelect(
            state = MultiSelectState(
                label = "Label",
                placeHolderText = "Placeholder",
                isFocused = isFocusedMulti,
                isDisabled = false,
                isMenuOpen = isOpenMulti,
                errorText = null,
                size = MultiSelectInputSize.Medium,
                options = listOf(
                    "Option 1",
                    "Option 2",
                    "Option 3",
                    "Option 4",
                    "Option 5",
                    "Option 6"
                ),
                selectedOptions = selectedMulti,
                onOptionSelected = { selectedMulti = selectedMulti + it },
                onMenuOpenChanged = { isOpenMulti = it },
                onOptionRemoved = { selectedMulti = selectedMulti - it },
                onFocusChanged = { isFocusedMulti = it },
            )
        )
    }
}