package com.instructure.horizon.features.learn.score

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.model.AssignmentStatus
import com.instructure.horizon.model.getStatus
import com.instructure.pandautils.utils.stringValueWithoutTrailingZeros
import java.util.Date

data class LearnScoreUiState(
    val screenState : LoadingState = LoadingState(),
    val courseId: Long = -1,
    val assignmentGroups: List<AssignmentGroupScoreItem> = emptyList(),
    val selectedSortOption: LearnScoreSortOption = LearnScoreSortOption.AssignmentName,
    val sortedAssignments: List<AssignmentScoreItem> = emptyList(),
    val currentScore: String? = null
)

data class AssignmentScoreItem(
    val name: String,
    val status: AssignmentStatus,
    val pointsPossible: Double,
    val submissionCommentsCount: Int,
    val lastScore: Double?,
    val dueDate: Date?
) {
    constructor(assignment: Assignment): this(
        name = assignment.name.orEmpty(),
        status = assignment.getStatus(),
        pointsPossible = assignment.pointsPossible,
        lastScore = assignment.submission?.score,
        submissionCommentsCount = assignment.lastActualSubmission?.submissionComments?.size ?: 0,
        dueDate = assignment.dueDate
    )
}

data class AssignmentGroupScoreItem(
    val name: String,
    val assignmentItems: List<AssignmentScoreItem>,
    val groupWeight: String,
) {
    constructor(assignmentGroup: AssignmentGroup): this(
        name = assignmentGroup.name.orEmpty(),
        assignmentItems = assignmentGroup.assignments.map { AssignmentScoreItem(it) },
        groupWeight = assignmentGroup.groupWeight.stringValueWithoutTrailingZeros
    )
}

enum class LearnScoreSortOption(@StringRes val label: Int) {
    DueDate(R.string.dueDate),
    AssignmentName(R.string.assignmentName),
}