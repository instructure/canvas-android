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
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.R
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionRepository
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter
import com.instructure.teacher.utils.getState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

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
    private var filter: SubmissionListFilter =
        savedStateHandle["filter"] ?: SubmissionListFilter.ALL
    private var filterValue: Double? = 0.0
    private var searchQuery: String = ""

    private var submissions: List<GradeableStudentSubmission> = emptyList()
    private var sections: List<Section> = emptyList()
    private val selectedSectionIds = mutableSetOf<Long>()

    private val _uiState = MutableStateFlow(
        SubmissionListUiState(
            assignmentName = assignment.name.orEmpty(),
            courseColor = Color(course.color),
            anonymousGrading = assignment.anonymousGrading,
            filter = filter,
            headerTitle = getHeaderTitle(filter, filterValue),
            searchQuery = "",
            actionHandler = this::handleAction
        )
    )
    val uiState: StateFlow<SubmissionListUiState>
        get() = _uiState.asStateFlow()

    private val _events = Channel<SubmissionListViewModelAction>()
    val events = _events.receiveAsFlow()

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
        sections = submissionListRepository.getSections(course.id, forceNetwork)
        _uiState.update { it.copy(sections = sections) }
        filterData()
    }

    private fun filterData() {
        val submissionUiStates = submissions.filter {
            when (filter) {
                SubmissionListFilter.ALL -> true
                SubmissionListFilter.LATE -> it.submission?.let {
                    assignment.getState(
                        it,
                        true
                    ) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE,
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE
                    )
                } ?: false

                SubmissionListFilter.NOT_GRADED -> it.submission?.let {
                    assignment.getState(
                        it,
                        true
                    ) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED,
                        AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE
                    ) || !it.isGradeMatchesCurrentSubmission
                } ?: false

                SubmissionListFilter.GRADED -> it.submission?.let {
                    assignment.getState(
                        it,
                        true
                    ) in listOf(
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED,
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE,
                        AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING,
                        AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED
                    ) && it.isGradeMatchesCurrentSubmission
                } ?: false

                SubmissionListFilter.ABOVE_VALUE -> it.submission?.let { it.isGraded && it.score >= filterValue.orDefault() }
                    ?: false

                SubmissionListFilter.BELOW_VALUE -> it.submission?.let { it.isGraded && it.score < filterValue.orDefault() }
                    ?: false
                // Filtering by ASSIGNMENT_STATE_MISSING here doesn't work because it assumes that the due date has already passed, which isn't necessarily the case when the teacher wants to see
                // which students haven't submitted yet
                SubmissionListFilter.MISSING -> it.submission?.workflowState == "unsubmitted" || it.submission == null
            }
        }
            .filter { it.assignee.name.contains(searchQuery, true) }
            .filter {
                if (selectedSectionIds.isEmpty()) return@filter true

                (it.assignee as? StudentAssignee)?.student?.enrollments?.any { it.courseSectionId in selectedSectionIds }
                    ?: false
            }
            .shuffled(Random(1234))
            .map { getSubmissionUiState(it) }

        _uiState.update {
            it.copy(
                submissions = submissionUiStates,
                loading = false,
                refreshing = false
            )
        }
    }

    private fun getSubmissionUiState(submission: GradeableStudentSubmission): SubmissionUiState {
        return SubmissionUiState(
            submissionId = submission.id,
            userName = submission.assignee.name,
            avatarUrl = if (submission.assignee is StudentAssignee) (submission.assignee as StudentAssignee).student.avatarUrl else null,
            tags = getTags(submission.submission),
            grade = getGrade(submission.submission),
            hidden = submission.submission?.let { it.postedAt == null } ?: false
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
                val submissions = _uiState.value.submissions
                val selected = submissions.indexOfFirst { it.submissionId == action.submissionId }
                viewModelScope.launch {
                    _events.send(
                        SubmissionListViewModelAction.RouteToSubmission(
                            courseId = course.id,
                            assignmentId = assignment.id,
                            selectedIdx = selected,
                            anonymousGrading = assignment.anonymousGrading,
                            filteredSubmissionIds = submissions.map { it.submissionId }
                                .toLongArray(),
                            filter = filter,
                            filterValue = filterValue.orDefault(
                            )
                        )
                    )
                }
            }

            is SubmissionListAction.Search -> {
                searchQuery = action.query
                _uiState.update { it.copy(searchQuery = action.query) }
                filterData()
            }

            is SubmissionListAction.SetFilters -> {
                filter = action.filter
                filterValue = action.filterValue
                selectedSectionIds.clear()
                selectedSectionIds.addAll(action.selectedSections)
                _uiState.update {
                    it.copy(
                        filter = action.filter,
                        filterValue = action.filterValue,
                        selectedSections = action.selectedSections,
                        headerTitle = getHeaderTitle(action.filter, action.filterValue)
                    )
                }
                filterData()
            }

            is SubmissionListAction.ShowPostPolicy -> {
                viewModelScope.launch {
                    _events.send(
                        SubmissionListViewModelAction.ShowPostPolicy(
                            course = course,
                            assignment = assignment
                        )
                    )
                }
            }

            is SubmissionListAction.SendMessage -> {
                viewModelScope.launch {
                    _events.send(
                        SubmissionListViewModelAction.SendMessage(
                            contextCode = course.contextId,
                            contextName = course.name,
                            recipients = getRecipients(),
                            subject = _uiState.value.headerTitle + " " + resources.getString(R.string.on) + " " + assignment.name
                        )
                    )
                }
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
                                val value: Double =
                                    submission.grade?.removeSuffix("%")?.toDouble() as Double
                                NumberHelper.doubleToPercentage(value, 2)
                            } else {
                                NumberHelper.formatDecimal(
                                    submission.grade?.toDouble() as Double,
                                    2,
                                    true
                                )
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

    private fun getHeaderTitle(filter: SubmissionListFilter, filterValue: Double?): String {
        return when (filter) {
            SubmissionListFilter.ALL -> {
                resources.getString(R.string.all_submissions)
            }

            SubmissionListFilter.LATE -> resources.getString(R.string.submitted_late)
            SubmissionListFilter.MISSING -> resources.getString(R.string.havent_submitted_yet)
            SubmissionListFilter.NOT_GRADED -> resources.getString(R.string.havent_been_graded)
            SubmissionListFilter.GRADED -> resources.getString(R.string.graded)
            SubmissionListFilter.BELOW_VALUE -> {
                resources.getString(
                    R.string.scored_less_than_value,
                    NumberHelper.formatDecimal(filterValue.orDefault(), 2, true)
                )
            }

            SubmissionListFilter.ABOVE_VALUE -> {
                resources.getString(
                    R.string.scored_more_than_value,
                    NumberHelper.formatDecimal(filterValue.orDefault(), 2, true)
                )
            }
        }
    }

    private fun getRecipients() : List<Recipient> {
        val filteredSubmissions = submissions.filter {
            _uiState.value.submissions.any { submission -> submission.submissionId == it.id }
        }
        return filteredSubmissions.map { submission ->
            when(val assignee = submission.assignee) {
                is StudentAssignee -> Recipient.from(assignee.student)
                is GroupAssignee -> Recipient.from(assignee.group)
            }
        }
    }
}