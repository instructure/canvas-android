/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.manageofflinecontent

enum class ManageOfflineContentMode {
    SELECTING,
    SYNCING,
    DELETING,
}

enum class CourseOfflineState {
    NONE,
    PARTIAL,
    ALL,
}

data class OfflineCourseItemUiState(
    val courseId: Long,
    val courseName: String,
    val offlineState: CourseOfflineState = CourseOfflineState.NONE,
    val isExpanded: Boolean = false,
    val files: List<OfflineFileItemUiState> = emptyList(),
    val onToggleExpanded: () -> Unit = {},
    val onOfflineStateChanged: (CourseOfflineState) -> Unit = {},
)

data class OfflineFileItemUiState(
    val fileId: Long,
    val fileName: String,
    val fileSizeLabel: String,
    val isSelected: Boolean = false,
    val onSelectionChanged: (Boolean) -> Unit = {},
)

data class ManageOfflineContentUiState(
    val mode: ManageOfflineContentMode = ManageOfflineContentMode.SELECTING,
    val storageUsedLabel: String = "",
    val courses: List<OfflineCourseItemUiState> = emptyList(),
    val onRemoveContentClick: () -> Unit = {},
    val onSyncClick: () -> Unit = {},
    val syncProgress: Float = 0f,
)
