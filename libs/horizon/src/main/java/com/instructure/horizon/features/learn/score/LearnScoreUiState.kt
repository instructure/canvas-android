package com.instructure.horizon.features.learn.score

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Grades
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnScoreUiState(
    val screenState : LoadingState = LoadingState(),
    val courseId: Long = -1,
    val assignmentGroups: List<AssignmentGroup> = emptyList(),
    val grades: Grades? = null
)
