package com.instructure.pandautils.features.grades

import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
import com.instructure.pandautils.utils.DisplayGrade


data class GradesUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val canvasContextColor: Int = Color.BLACK,
    val items: List<AssignmentGroupUiState> = emptyList(),
    val gradePreferencesUiState: GradePreferencesUiState = GradePreferencesUiState(),
    val onlyGradedAssignmentsSwitchEnabled: Boolean = true,
    val gradeText: String? = null
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
    val dueDate: String?,
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
    data object Refresh : GradesAction()
    data class HeaderClick(val id: Long) : GradesAction()
    data object ShowGradePreferences : GradesAction()
    data object HideGradePreferences : GradesAction()
    data class GradePreferencesUpdated(val gradePreferencesUiState: GradePreferencesUiState) : GradesAction()
    data class OnlyGradedAssignmentsSwitchCheckedChange(val checked: Boolean) : GradesAction()
    data class AssignmentClick(val id: Long) : GradesAction()
}

sealed class GradesViewModelAction {
    data class NavigateToAssignmentDetails(val assignmentId: Long) : GradesViewModelAction()
}