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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.instructure.horizon.horizonui.organisms.inputs.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.SingleSelectState
import com.instructure.horizon.horizonui.organisms.inputs.sizes.SingleSelectInputSize

@Composable
fun SkillspaceScreen() {
    var isOpen by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    SingleSelect(
        state = SingleSelectState(
            label = "Label",
            placeHolderText = "Placeholder",
            isFocused = false,
            isDisabled = false,
            isMenuOpen = isOpen,
            errorText = null,
            size = SingleSelectInputSize.Medium,
            options = listOf("Option 1", "Option 2", "Option 3"),
            selectedOption = selected,
            onOptionSelected = { selected = it },
            onMenuOpenChanged = { isOpen = it },
        )
    )
}