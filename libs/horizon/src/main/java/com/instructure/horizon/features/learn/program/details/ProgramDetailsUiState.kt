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
package com.instructure.horizon.features.learn.program.details

import com.instructure.horizon.features.learn.program.details.components.ProgramProgressState
import com.instructure.horizon.horizonui.platform.LoadingState

data class ProgramDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val programName: String = "",
    val showProgressBar: Boolean = false,
    val progressBarUiState: ProgressBarUiState = ProgressBarUiState(),
    val description: String = "",
    val tags: List<ProgramDetailTag> = emptyList(),
    val programProgressState: ProgramProgressState = ProgramProgressState(courses = emptyList()),
    val navigateToCourseId: Long? = null,
    val onNavigateToCourse: () -> Unit = {},
)

data class ProgramDetailTag(
    val name: String,
    val iconRes: Int? = null,
)

data class ProgressBarUiState(
    val progress: Double = 0.0,
    val progressBarStatus: ProgressBarStatus = ProgressBarStatus.IN_PROGRESS
)

enum class ProgressBarStatus {
    NOT_STARTED,
    IN_PROGRESS
}