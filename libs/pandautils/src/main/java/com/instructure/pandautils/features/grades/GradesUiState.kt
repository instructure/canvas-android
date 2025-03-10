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

import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import com.instructure.pandautils.utils.DisplayGrade


data class GradesUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val canvasContextColor: Int = Color.BLACK,
    val items: List<AssignmentGroupUiState> = emptyList(),
    val gradePreferencesUiState: GradePreferencesUiState = GradePreferencesUiState(),
    val onlyGradedAssignmentsSwitchEnabled: Boolean = true,
    val gradeText: String = "",
    val isGradeLocked: Boolean = false,
    val snackbarMessage: String? = null
)

data class AssignmentGroupUiState(
    val id: Long,
    val name: String,
    val assignments: List<AssignmentUiState>,
    val expanded: Boolean
)

data class AssignmentUiState(
    val id: Long,
    @DrawableRes val iconRes: Int,
    val name: String,
    val dueDate: String,
    val submissionStateLabel: SubmissionStateLabel,
    val displayGrade: DisplayGrade
)

enum class SubmissionStateLabel(
    @DrawableRes val iconRes: Int,
    @ColorRes val colorRes: Int,
    @StringRes val labelRes: Int
) {
    NOT_SUBMITTED(R.drawable.ic_unpublish, R.color.backgroundDark, R.string.notSubmitted),
    MISSING(R.drawable.ic_unpublish, R.color.textDanger, R.string.missingSubmissionLabel),
    LATE(R.drawable.ic_clock, R.color.textWarning, R.string.lateSubmissionLabel),
    SUBMITTED(R.drawable.ic_complete, R.color.textSuccess, R.string.submitted),
    GRADED(R.drawable.ic_complete_solid, R.color.textSuccess, R.string.gradedSubmissionLabel),
    NONE(0, 0, 0)
}

sealed class GradesAction {
    data class Refresh(val clearItems: Boolean = false) : GradesAction()
    data class GroupHeaderClick(val id: Long) : GradesAction()
    data object ShowGradePreferences : GradesAction()
    data object HideGradePreferences : GradesAction()
    data class GradePreferencesUpdated(val gradingPeriod: GradingPeriod?, val sortBy: SortBy) : GradesAction()
    data class OnlyGradedAssignmentsSwitchCheckedChange(val checked: Boolean) : GradesAction()
    data class AssignmentClick(val id: Long) : GradesAction()
    data object SnackbarDismissed : GradesAction()
}

sealed class GradesViewModelAction {
    data class NavigateToAssignmentDetails(val courseId: Long, val assignmentId: Long) : GradesViewModelAction()
}