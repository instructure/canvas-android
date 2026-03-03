/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.grades

import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import com.instructure.pandautils.utils.DisplayGrade


data class AppBarUiState(
    val title: String,
    val subtitle: String,
    val navigationActionClick: () -> Unit,
    val bookmarkable: Boolean,
    val addBookmarkClick: () -> Unit
)

data class GradesUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<AssignmentGroupUiState> = emptyList(),
    val gradePreferencesUiState: GradePreferencesUiState = GradePreferencesUiState(),
    val onlyGradedAssignmentsSwitchEnabled: Boolean = true,
    val isWhatIfGradingEnabled: Boolean = false,
    val showWhatIfScore: Boolean = false,
    val whatIfScoreDialogData: WhatIfScoreDialogData? = null,
    val gradeText: String = "",
    val isGradeLocked: Boolean = false,
    val snackbarMessage: String? = null,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false
)

data class WhatIfScoreDialogData(
    val assignmentId: Long,
    val assignmentName: String,
    val currentScoreText: String,
    val whatIfScore: Double?,
    val maxScore: Double?
)

data class AssignmentGroupUiState(
    val id: Long,
    val name: String,
    val assignments: List<AssignmentUiState>,
    val expanded: Boolean
)

data class AssignmentUiState(
    val id: Long,
    val iconRes: Int,
    val name: String,
    val dueDate: String,
    val submissionStateLabel: SubmissionStateLabel,
    val displayGrade: DisplayGrade,
    val score: Double?,
    val maxScore: Double?,
    val whatIfScore: Double?,
    val checkpoints: List<DiscussionCheckpointUiState> = emptyList(),
    val checkpointsExpanded: Boolean = false
)

sealed class GradesAction {
    data class Refresh(val clearItems: Boolean = false) : GradesAction()
    data class GroupHeaderClick(val id: Long) : GradesAction()
    data object ShowGradePreferences : GradesAction()
    data object HideGradePreferences : GradesAction()
    data class GradePreferencesUpdated(val gradingPeriod: GradingPeriod?, val sortBy: SortBy) : GradesAction()
    data class OnlyGradedAssignmentsSwitchCheckedChange(val checked: Boolean) : GradesAction()
    data class ShowWhatIfScoreSwitchCheckedChange(val checked: Boolean) : GradesAction()
    data class ShowWhatIfScoreDialog(val assignmentId: Long) : GradesAction()
    data object HideWhatIfScoreDialog : GradesAction()
    data class UpdateWhatIfScore(val assignmentId: Long, val score: Double?) : GradesAction()
    data class AssignmentClick(val id: Long) : GradesAction()
    data object SnackbarDismissed : GradesAction()
    data class ToggleCheckpointsExpanded(val assignmentId: Long) : GradesAction()
    data object ToggleSearch : GradesAction()
    data class SearchQueryChanged(val query: String) : GradesAction()
}

sealed class GradesViewModelAction {
    data class NavigateToAssignmentDetails(val courseId: Long, val assignmentId: Long) : GradesViewModelAction()
}