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
package com.instructure.teacher.features.assignment.submission

import android.content.res.Resources
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.DifferentiationTagsQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.pandautils.features.speedgrader.AssignmentSubmissionRepository
import com.instructure.pandautils.features.speedgrader.SubmissionListFilter
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.R
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

    companion object {
        // Fixed seed ensures consistent ordering across app restarts for anonymous grading
        private const val ANONYMOUS_SHUFFLE_SEED = 1234
    }

    private val assignment: Assignment = savedStateHandle[SubmissionListFragment.ASSIGNMENT]
        ?: throw IllegalArgumentException("Assignment must be passed to SubmissionListViewModel")
    private val course: Course = savedStateHandle[SubmissionListFragment.COURSE]
        ?: throw IllegalArgumentException("Course must be passed to SubmissionListViewModel")
    private var selectedFilters: Set<SubmissionListFilter> = setOf(
        savedStateHandle[SubmissionListFragment.FILTER_TYPE] ?: SubmissionListFilter.ALL
    )
    private var filterValueAbove: Double? = null
    private var filterValueBelow: Double? = null
    private var searchQuery: String = ""

    private var submissions: List<GradeableStudentSubmission> = emptyList()
    private var sections: List<Section> = emptyList()
    private var selectedSectionIds = listOf<Long>()
    private var customStatuses = listOf<CustomGradeStatusesQuery.Node>()
    private var differentiationTags: List<DifferentiationTag> = emptyList()
    private var selectedDifferentiationTagIds: Set<String> = emptySet()
    private var includeStudentsWithoutTags: Boolean = false
    private var sortOrder: SubmissionSortOrder = SubmissionSortOrder.STUDENT_SORTABLE_NAME
    private var selectedCustomStatusIds: Set<String> = emptySet()

    private val _uiState = MutableStateFlow(
        SubmissionListUiState(
            assignmentName = assignment.name.orEmpty(),
            courseColor = Color(course.color),
            anonymousGrading = assignment.anonymousGrading,
            selectedFilters = selectedFilters,
            assignmentMaxPoints = assignment.pointsPossible,
            headerTitle = getHeaderTitle(selectedFilters, filterValueAbove, filterValueBelow),
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
        try {
            customStatuses = submissionListRepository.getCustomGradeStatuses(
                course.id,
                forceNetwork
            )
            submissions = submissionListRepository.getGradeableStudentSubmissions(
                assignment,
                course.id,
                forceNetwork
            )
            sections = submissionListRepository.getSections(course.id, forceNetwork)

            val rawDifferentiationTags = submissionListRepository.getDifferentiationTags(
                course.id,
                forceNetwork
            )
            differentiationTags = rawDifferentiationTags.map { (group, groupSetName) ->
                val groupName = group.name.orEmpty()
                DifferentiationTag(
                    id = group._id,
                    name = groupName,
                    groupSetName = if (groupSetName != groupName) groupSetName else null,
                    userIds = group.membersConnection?.nodes?.mapNotNull { it?.user?._id }.orEmpty()
                )
            }

            _uiState.update {
                it.copy(
                    sections = sections,
                    differentiationTags = differentiationTags,
                    customGradeStatuses = customStatuses.map { status ->
                        CustomGradeStatus(
                            id = status._id,
                            name = status.name
                        )
                    }
                )
            }
            filterData()
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(error = true, loading = false, refreshing = false) }
        }
    }

    private fun filterData() {
        var filteredSubmissions = submissions

        filteredSubmissions = filteredSubmissions.filter { submission ->
            if (selectedFilters.contains(SubmissionListFilter.ALL) && selectedCustomStatusIds.isEmpty()) return@filter true

            val activeFilters = if (selectedCustomStatusIds.isNotEmpty()) {
                selectedFilters - SubmissionListFilter.ALL
            } else {
                selectedFilters
            }

            val matchesStandardFilter = activeFilters.any { filter ->
                when (filter) {
                    SubmissionListFilter.ALL -> true
                    SubmissionListFilter.LATE -> submission.submission?.let {
                        assignment.getState(it, true) in listOf(
                            AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE,
                            AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE
                        )
                    } == true

                    SubmissionListFilter.NOT_GRADED -> submission.submission?.let {
                        (assignment.getState(it, true) in listOf(
                            AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED,
                            AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE
                        ) || !it.isGradeMatchesCurrentSubmission) && submission.submission?.customGradeStatusId == null
                    } == true

                    SubmissionListFilter.GRADED -> submission.submission?.let {
                        (assignment.getState(it, true) in listOf(
                            AssignmentUtils2.ASSIGNMENT_STATE_GRADED,
                            AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE,
                            AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING,
                            AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED
                        ) && it.isGradeMatchesCurrentSubmission)
                    } == true

                    SubmissionListFilter.MISSING -> submission.submission?.workflowState == "unsubmitted" || submission.submission == null

                    SubmissionListFilter.SUBMITTED -> submission.submission?.let {
                        !it.late && !it.excused && !it.isGraded && it.workflowState != "unsubmitted"
                    } == true

                    SubmissionListFilter.ABOVE_VALUE, SubmissionListFilter.BELOW_VALUE -> false
                }
            }

            val matchesCustomStatus = if (selectedCustomStatusIds.isNotEmpty()) {
                submission.submission?.customGradeStatusId?.let { statusId ->
                    selectedCustomStatusIds.contains(statusId.toString())
                } == true
            } else {
                false
            }

            matchesStandardFilter || matchesCustomStatus
        }

        filterValueAbove?.let { aboveValue ->
            filteredSubmissions = filteredSubmissions.filter { submission ->
                submission.submission?.let { !it.excused && it.isGraded && it.score >= aboveValue } == true
            }
        }

        filterValueBelow?.let { belowValue ->
            filteredSubmissions = filteredSubmissions.filter { submission ->
                submission.submission?.let { !it.excused && it.isGraded && it.score < belowValue } == true
            }
        }

        filteredSubmissions = filteredSubmissions.filter { submission ->
            if (selectedSectionIds.isEmpty()) return@filter true
            (submission.assignee as? StudentAssignee)?.student?.enrollments?.any { it.courseSectionId in selectedSectionIds } == true
        }

        if (selectedDifferentiationTagIds.isNotEmpty() || includeStudentsWithoutTags) {
            filteredSubmissions = filteredSubmissions.filter { submission ->
                matchesDifferentiationTagFilter(submission, selectedDifferentiationTagIds, includeStudentsWithoutTags)
            }
        }

        filteredSubmissions = filteredSubmissions.filter { submission ->
            submission.assignee.name.contains(searchQuery, true)
        }

        val sorted = filteredSubmissions.sortedWith(getSortComparator())

        val submissionUiStates = if (assignment.anonymousGrading) {
            sorted.shuffled(Random(ANONYMOUS_SHUFFLE_SEED)).map { getSubmissionUiState(it) }
        } else {
            sorted.map { getSubmissionUiState(it) }
        }

        _uiState.update {
            it.copy(
                submissions = submissionUiStates,
                loading = false,
                refreshing = false
            )
        }
    }

    private fun matchesDifferentiationTagFilter(
        submission: GradeableStudentSubmission,
        selectedTags: Set<String>,
        includeWithoutTags: Boolean
    ): Boolean {
        val studentId = (submission.assignee as? StudentAssignee)?.student?.id?.toString()

        val belongsToSelectedTag = if (selectedTags.isNotEmpty()) {
            studentId?.let { id ->
                differentiationTags.any { tag ->
                    tag.id in selectedTags && id in tag.userIds
                }
            } == true
        } else {
            false
        }

        val belongsToAnyTag = studentId?.let { id ->
            differentiationTags.any { tag -> id in tag.userIds }
        } == true

        return when {
            includeWithoutTags && selectedTags.isNotEmpty() -> belongsToSelectedTag || !belongsToAnyTag
            includeWithoutTags -> !belongsToAnyTag
            else -> belongsToSelectedTag
        }
    }

    private fun getSortComparator(): Comparator<GradeableStudentSubmission> {
        return when (sortOrder) {
            SubmissionSortOrder.STUDENT_SORTABLE_NAME -> compareBy {
                (it.assignee as? StudentAssignee)?.student?.sortableName?.lowercase(java.util.Locale.getDefault())
                    ?: it.assignee.name.lowercase(java.util.Locale.getDefault())
            }

            SubmissionSortOrder.STUDENT_NAME -> compareBy {
                (it.assignee as? StudentAssignee)?.student?.name?.lowercase(java.util.Locale.getDefault())
                    ?: it.assignee.name.lowercase(java.util.Locale.getDefault())
            }

            SubmissionSortOrder.SUBMISSION_DATE -> compareByDescending<GradeableStudentSubmission> {
                it.submission?.submittedAt?.time ?: 0L
            }.thenBy {
                (it.assignee as? StudentAssignee)?.student?.sortableName?.lowercase(java.util.Locale.getDefault())
                    ?: it.assignee.name.lowercase(java.util.Locale.getDefault())
            }

            SubmissionSortOrder.SUBMISSION_STATUS -> compareBy<GradeableStudentSubmission> {
                when {
                    it.submission == null || it.submission?.workflowState == "unsubmitted" -> 3
                    it.submission?.missing == true -> 2
                    it.submission?.late == true -> 1
                    else -> 0
                }
            }.thenBy {
                (it.assignee as? StudentAssignee)?.student?.sortableName?.lowercase(java.util.Locale.getDefault())
                    ?: it.assignee.name.lowercase(java.util.Locale.getDefault())
            }
        }
    }

    private fun getSubmissionUiState(submission: GradeableStudentSubmission): SubmissionUiState {
        return SubmissionUiState(
            submissionId = submission.id,
            userName = submission.assignee.name,
            isFakeStudent = (submission.assignee as? StudentAssignee)?.student?.isFakeStudent == true,
            avatarUrl = if (submission.assignee is StudentAssignee) (submission.assignee as StudentAssignee).student.avatarUrl else null,
            tags = getTags(submission.submission),
            grade = getGrade(submission.submission),
            hidden = submission.submission?.let { it.postedAt == null } == true,
            assigneeId = submission.assigneeId,
            group = submission.assignee is GroupAssignee
        )
    }

    private fun getTags(submission: Submission?): List<SubmissionTag> {
        val tags = mutableListOf<SubmissionTag>()

        val matchedCustomStatus = submission?.customGradeStatusId?.let { id ->
            customStatuses.find { it._id.toLongOrNull() == id }
        }

        if (matchedCustomStatus != null) {
            tags.add(
                SubmissionTag.Custom(
                    text = matchedCustomStatus.name,
                    icon = R.drawable.ic_flag,
                    color = R.color.textInfo
                )
            )
            return tags
        }

        when {
            submission == null -> tags.add(SubmissionTag.NotSubmitted)
            submission.missing -> tags.add(SubmissionTag.Missing)
            submission.workflowState == "unsubmitted" -> tags.add(SubmissionTag.NotSubmitted)
            else -> {
                if (!submission.late && !submission.excused && !submission.isGraded) {
                    tags.add(SubmissionTag.Submitted)
                }
                if (submission.late) {
                    tags.add(SubmissionTag.Late)
                }

                if (submission.excused) {
                    tags.add(SubmissionTag.Excused)
                } else if (submission.isGraded) {
                    tags.add(SubmissionTag.Graded)
                } else {
                    tags.add(SubmissionTag.NeedsGrading)
                }
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
                            filteredSubmissionIds = submissions.map {
                                if (RemoteConfigUtils.getBoolean(RemoteConfigParam.SPEEDGRADER_V2)) {
                                    it.assigneeId
                                } else {
                                    it.submissionId
                                }
                            }
                                .toLongArray(),
                            selectedFilters = selectedFilters,
                            filterValueAbove = filterValueAbove,
                            filterValueBelow = filterValueBelow
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
                selectedFilters = action.selectedFilters
                filterValueAbove = action.filterValueAbove
                filterValueBelow = action.filterValueBelow
                selectedSectionIds = action.selectedSections
                selectedDifferentiationTagIds = action.selectedDifferentiationTagIds
                includeStudentsWithoutTags = action.includeStudentsWithoutTags
                sortOrder = action.sortOrder
                selectedCustomStatusIds = action.selectedCustomStatusIds
                _uiState.update {
                    it.copy(
                        selectedFilters = action.selectedFilters,
                        filterValueAbove = action.filterValueAbove,
                        filterValueBelow = action.filterValueBelow,
                        selectedSections = action.selectedSections,
                        selectedDifferentiationTagIds = action.selectedDifferentiationTagIds,
                        includeStudentsWithoutTags = action.includeStudentsWithoutTags,
                        sortOrder = action.sortOrder,
                        selectedCustomStatusIds = action.selectedCustomStatusIds,
                        headerTitle = getHeaderTitle(action.selectedFilters, action.filterValueAbove, action.filterValueBelow)
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
                            subject = resources.getString(
                                R.string.submissionMessageSubject,
                                _uiState.value.headerTitle,
                                assignment.name
                            )
                        )
                    )
                }
            }

            is SubmissionListAction.AvatarClicked -> {
                viewModelScope.launch {
                    _events.send(
                        SubmissionListViewModelAction.RouteToUser(
                            action.userId,
                            course.id
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
                                val value = submission.grade?.removeSuffix("%")?.toDouble() ?: 0.0
                                NumberHelper.doubleToPercentage(value, 2)
                            } else {
                                val gradeValue = submission.grade?.toDouble() ?: 0.0
                                NumberHelper.formatDecimal(gradeValue, 2, true)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            submission.grade ?: "-"
                        }
                    }
                }
            }

            else -> "-"
        }
    }

    private fun getHeaderTitle(
        filters: Set<SubmissionListFilter>,
        filterValueAbove: Double?,
        filterValueBelow: Double?
    ): String {
        val hasScoreFilters = filterValueAbove != null || filterValueBelow != null
        val hasStatusFilters = !filters.contains(SubmissionListFilter.ALL) && filters.isNotEmpty()

        val scoreText = when {
            filterValueAbove != null && filterValueBelow != null -> {
                resources.getString(
                    R.string.scored_between_values,
                    NumberHelper.formatDecimal(filterValueAbove, 2, true),
                    NumberHelper.formatDecimal(filterValueBelow, 2, true)
                )
            }
            filterValueAbove != null -> {
                resources.getString(
                    R.string.scored_more_than_value,
                    NumberHelper.formatDecimal(filterValueAbove, 2, true)
                )
            }
            filterValueBelow != null -> {
                resources.getString(
                    R.string.scored_less_than_value,
                    NumberHelper.formatDecimal(filterValueBelow, 2, true)
                )
            }
            else -> ""
        }

        return when {
            hasScoreFilters && !hasStatusFilters -> scoreText
            hasScoreFilters && hasStatusFilters -> {
                val statusText = if (filters.size > 1) {
                    resources.getString(R.string.multiple_filters)
                } else {
                    when (filters.first()) {
                        SubmissionListFilter.LATE -> resources.getString(R.string.submitted_late)
                        SubmissionListFilter.MISSING -> resources.getString(R.string.havent_submitted_yet)
                        SubmissionListFilter.NOT_GRADED -> resources.getString(R.string.havent_been_graded)
                        SubmissionListFilter.GRADED -> resources.getString(R.string.graded)
                        SubmissionListFilter.SUBMITTED -> resources.getString(R.string.submitted)
                        else -> resources.getString(R.string.all_submissions)
                    }
                }
                "$statusText • $scoreText"
            }
            !hasScoreFilters && hasStatusFilters -> {
                if (filters.size > 1) {
                    resources.getString(R.string.multiple_filters)
                } else {
                    when (filters.first()) {
                        SubmissionListFilter.LATE -> resources.getString(R.string.submitted_late)
                        SubmissionListFilter.MISSING -> resources.getString(R.string.havent_submitted_yet)
                        SubmissionListFilter.NOT_GRADED -> resources.getString(R.string.havent_been_graded)
                        SubmissionListFilter.GRADED -> resources.getString(R.string.graded)
                        SubmissionListFilter.SUBMITTED -> resources.getString(R.string.submitted)
                        else -> resources.getString(R.string.all_submissions)
                    }
                }
            }
            else -> resources.getString(R.string.all_submissions)
        }
    }

    private fun getRecipients(): List<Recipient> {
        val filteredSubmissions = submissions.filter {
            _uiState.value.submissions.any { submission -> submission.submissionId == it.id }
        }
        return filteredSubmissions.map { submission ->
            when (val assignee = submission.assignee) {
                is StudentAssignee -> Recipient.from(assignee.student)
                is GroupAssignee -> Recipient.from(assignee.group)
            }
        }
    }
}