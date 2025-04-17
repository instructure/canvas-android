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
package com.instructure.horizon.features.moduleitemsequence

import com.instructure.horizon.features.moduleitemsequence.progress.ProgressScreenUiState
import com.instructure.horizon.horizonui.platform.LoadingState

data class ModuleItemSequenceUiState(
    val loadingState: LoadingState = LoadingState(),
    val items: List<ModuleItemUiState> = emptyList(),
    val currentPosition: Int = -1,
    val currentItem: ModuleItemUiState? = null,
    val progressScreenState: ProgressScreenUiState = ProgressScreenUiState(),
    val onPreviousClick: () -> Unit = {},
    val onNextClick: () -> Unit = {},
    val onProgressClick: () -> Unit = {},
)

data class ModuleItemUiState(
    val moduleName: String,
    val moduleItemName: String,
    val moduleItemId: Long,
    val detailTags: List<String> = emptyList(),
    val pillText: String? = null,
    val moduleItemContent: ModuleItemContent? = null,
    val markAsDoneUiState: MarkAsDoneUiState? = null,
    val isLoading: Boolean = false,
)

data class MarkAsDoneUiState(
    val isDone: Boolean = false,
    val isLoading: Boolean = false,
    val onMarkAsDoneClick: () -> Unit = {},
    val onMarkAsNotDoneClick: () -> Unit = {},
)

sealed class ModuleItemContent {
    data class Assignment(val assignmentId: Long) : ModuleItemContent()
    data class Page(val courseId: Long, val pageUrl: String) : ModuleItemContent()
    data class ExternalLink(val url: String) : ModuleItemContent()
    data class File(val url: String) : ModuleItemContent()
    data class ExternalTool(val url: String) : ModuleItemContent()
    data class Assessment(val quizId: Long) : ModuleItemContent()
    data class Locked(val lockExplanation: String) : ModuleItemContent()
}