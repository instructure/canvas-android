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
package com.instructure.horizon.features.dashboard

import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.model.LearningObjectType
import java.util.Date

data class DashboardUiState(
    val logoUrl: String = "",
    val onNotebookClick: () -> Unit = {},
    val onInboxClick: () -> Unit = {},
    val coursesUiState: List<DashboardCourseUiState> = emptyList(),
    val loadingState: LoadingState = LoadingState(),
)

data class DashboardCourseUiState(
    val courseId: Long,
    val courseName: String,
    val courseProgress: Double,
    val completed: Boolean = false,
    val nextModuleName: String? = null,
    val nextModuleItemName: String? = null,
    val nextModuleItemId: Long? = null,
    val progressLabel: String? = null,
    val remainingTime: String? = null,
    val learningObjectType: LearningObjectType? = null,
    val dueDate: Date? = null
)