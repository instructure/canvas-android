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
package com.instructure.horizon.features.learn.programs

import com.instructure.horizon.features.learn.programs.components.ProgramProgressState
import com.instructure.horizon.horizonui.platform.LoadingState

data class ProgramDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val programName: String = "",
    val progress: Double = 0.0,
    val description: String = "",
    val tags: List<ProgramDetailTag> = emptyList(),
    val programProgressState: ProgramProgressState = ProgramProgressState(courses = emptyList()),
)

data class ProgramDetailTag(
    val name: String,
    val iconRes: Int? = null,
)