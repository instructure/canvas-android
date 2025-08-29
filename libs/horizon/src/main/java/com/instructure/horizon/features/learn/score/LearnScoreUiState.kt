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
    val selectedSortOption: LearnScoreSortOption = LearnScoreSortOption.AssignmentNameAscending,
    val sortedAssignments: List<AssignmentScoreItem> = emptyList(),
    val currentScore: String? = null
)

data class AssignmentScoreItem(
    val assignmentId: Long,
    val name: String,
    val status: AssignmentStatus,
    val pointsPossible: Double,
    val submissionCommentsCount: Int,
    val lastScore: String?,
    val dueDate: Date?
) {
    constructor(assignment: Assignment): this(
        assignmentId = assignment.id,
        name = assignment.name.orEmpty(),
        status = assignment.getStatus(),
        pointsPossible = assignment.pointsPossible,
        lastScore = assignment.submission?.grade,
        submissionCommentsCount = assignment.lastGradedOrSubmittedSubmission?.submissionComments?.size ?: 0,
        dueDate = assignment.dueDate
    )
}

data class AssignmentGroupScoreItem(
    val name: String,
    val groupWeight: String,
) {
    constructor(assignmentGroup: AssignmentGroup): this(
        name = assignmentGroup.name.orEmpty(),
        groupWeight = assignmentGroup.groupWeight.stringValueWithoutTrailingZeros
    )
}

enum class LearnScoreSortOption(@StringRes val label: Int) {
    DueDateDescending(R.string.scoresSortingDueDateDescending),
    AssignmentNameAscending(R.string.scoresSortingAssignmentNameAscending),
}