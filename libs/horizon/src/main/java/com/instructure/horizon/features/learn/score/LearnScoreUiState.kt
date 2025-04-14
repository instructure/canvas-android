package com.instructure.horizon.features.learn.score

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Grades
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnScoreUiState(
    val screenState : LoadingState = LoadingState(),
    val courseId: Long = -1,
    val assignmentGroups: List<AssignmentGroup> = emptyList(),
    val selectedSortOption: LearnScoreSortOption = LearnScoreSortOption.AssignmentName,
    val sortedAssignments: List<Assignment> = emptyList(),
    val grades: Grades? = null
)

enum class LearnScoreSortOption(@StringRes val label: Int) {
    DueDate(R.string.dueDate),
    AssignmentName(R.string.assignmentName),
}