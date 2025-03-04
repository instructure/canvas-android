/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.features.submission

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.color
import com.instructure.teacher.R
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionRepository
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter
import com.instructure.teacher.utils.getState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmissionListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val submissionListRepository: AssignmentSubmissionRepository
) : ViewModel() {

    private val assignment: Assignment = savedStateHandle["assignment"]
        ?: throw IllegalArgumentException("Assignment must be passed to SubmissionListViewModel")
    private val course: Course = savedStateHandle["course"]
        ?: throw IllegalArgumentException("Course must be passed to SubmissionListViewModel")
    private val filter: SubmissionListFilter =
        savedStateHandle["filter"] ?: SubmissionListFilter.ALL

    private var submissions: List<GradeableStudentSubmission> = emptyList()

    private val _uiState = MutableStateFlow(
        SubmissionListUiState(
            assignmentName = assignment.name.orEmpty(),
            courseColor = Color(course.color),
            filter = filter,
            actionHandler = this::handleAction
        )
    )
    val uiState: StateFlow<SubmissionListUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            loadData()
        }
    }

    private suspend fun loadData(forceNetwork: Boolean = false) {
        submissions = submissionListRepository.getGradeableStudentSubmissions(
            assignment,
            course.id,
            forceNetwork
        )

        val groups = mutableMapOf(
            R.string.submitted to mutableListOf<SubmissionUiState>(),
            R.string.not_submitted to mutableListOf<SubmissionUiState>(),
            R.string.graded to mutableListOf<SubmissionUiState>()
        )

        submissions.forEach {
            when {
                it.submission?.let {
                    assignment.getState(it, true) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED,
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE
                    ) || !it.isGradeMatchesCurrentSubmission
                } == true -> {
                    groups[R.string.submitted] =
                        (groups[R.string.submitted]?.plus(getSubmissionUiState(it)))?.toMutableList()
                            ?: mutableListOf(getSubmissionUiState(it))
                }

                it.submission?.workflowState == "unsubmitted" || it.submission == null -> {
                    groups[R.string.not_submitted] =
                        (groups[R.string.not_submitted]?.plus(getSubmissionUiState(it)))?.toMutableList()
                            ?: mutableListOf(getSubmissionUiState(it))
                }

                it.submission?.let {
                    assignment.getState(it, true) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED,
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE,
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING,
                        AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED
                    ) && it.isGradeMatchesCurrentSubmission
                } == true -> {
                    groups[R.string.graded] =
                        (groups[R.string.graded]?.plus(getSubmissionUiState(it)))?.toMutableList()
                            ?: mutableListOf(getSubmissionUiState(it))
                }
            }
        }

        _uiState.update { it.copy(submissions = groups, loading = false, refreshing = false) }
    }

    private fun getSubmissionUiState(submission: GradeableStudentSubmission): SubmissionUiState {
        return SubmissionUiState(
            submissionId = submission.id,
            userName = submission.assignee.name,
            avatarUrl = if (submission.assignee is StudentAssignee) (submission.assignee as StudentAssignee).student.avatarUrl else null,
            tags = getTags(submission.submission),
            grade = getGrade(submission.submission)
        )
    }

    private fun getTags(submission: Submission?): List<SubmissionTag> {
        val tags = mutableListOf<SubmissionTag>()

        if (submission == null || submission.workflowState == "unsubmitted") {
            tags.add(SubmissionTag.NOT_SUBMITTED)
        } else {
            if (submission.late) {
                tags.add(SubmissionTag.LATE)
            }

            if (!submission.late && !submission.excused && !submission.isGraded) {
                tags.add(SubmissionTag.SUBMITTED)
            }

            if (submission.missing) {
                tags.add(SubmissionTag.MISSING)
            }

            if (submission.excused) {
                tags.add(SubmissionTag.EXCUSED)
            }

            if (submission.isGraded) {
                tags.add(SubmissionTag.GRADED)
            }

            if (!submission.isGraded && !submission.excused) {
                tags.add(SubmissionTag.NEEDS_GRADING)
            }
        }

        return tags
    }

    private fun handleAction(action: SubmissionListAction) {
        when (action) {
            is SubmissionListAction.Refresh -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(refreshing = true) }
                    loadData(true)
                }
            }

            is SubmissionListAction.SubmissionClicked -> {
                // Handle click
            }
        }
    }

    private fun getGrade(submission: Submission?): String {
        return when {
            submission == null -> "-"
            submission.excused -> resources.getString(R.string.excused)
            submission.isGraded -> {
                when (submission.grade) {
                    "complete" -> resources.getString(R.string.complete_grade)
                    "incomplete" -> resources.getString(R.string.incomplete_grade)
                    else -> {
                        try {
                            if (assignment.gradingType == Assignment.PERCENT_TYPE) {
                                val value: Double = submission.grade?.removeSuffix("%")?.toDouble() as Double
                                NumberHelper.doubleToPercentage(value, 2)
                            } else {
                                NumberHelper.formatDecimal(submission.grade?.toDouble() as Double, 2, true)
                            }
                        } catch (e: Exception) {
                            submission.grade ?: "-"
                        }
                    }
                }
            }
            else -> "-"
        }
    }
}