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
package com.instructure.horizon.features.moduleitemsequence.progress

import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState

data class ProgressScreenUiState(
    val visible: Boolean = false,
    val pages: List<ProgressPageUiState> = emptyList(),
    val currentPosition: Int = 0,
    val selectedModuleItemId: Long = -1,
    val onCloseClick: () -> Unit = {},
    val onPreviousClick: () -> Unit = {},
    val onNextClick: () -> Unit = {},
)

data class ProgressPageUiState(
    val moduleName: String,
    val moduleId: Long,
    val items: List<ProgressPageItem> = emptyList(),
)

sealed class ProgressPageItem {
    data class SubHeader(val name: String) : ProgressPageItem()
    data class ModuleItem(val moduleItemId: Long, val moduleItemCardState: ModuleItemCardState) : ProgressPageItem()
}