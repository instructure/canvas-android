/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.getCurrentGradingPeriod
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.filterHiddenAssignments
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getGrade
import com.instructure.pandautils.utils.getSubAssignmentSubmissionGrade
import com.instructure.pandautils.utils.getSubAssignmentSubmissionStateLabel
import com.instructure.pandautils.utils.getSubmissionStateLabel
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


const val COURSE_ID_KEY = "course-id"

@HiltViewModel
class GradesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gradesBehaviour: GradesBehaviour,
    private val repository: GradesRepository,
    private val gradeFormatter: GradeFormatter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(COURSE_ID_KEY).orDefault()

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<GradesViewModelAction>()
    val events = _events.receiveAsFlow()

    private var course: Course? = null
    private var courseGrade: CourseGrade? = null

    private var customStatuses = listOf<CustomGradeStatusesQuery.Node>()

    init {
        loadGrades(
            forceRefresh = false,
            initialize = true
        )
    }

    private fun loadGrades(forceRefresh: Boolean, initialize: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    canvasContextColor = gradesBehaviour.canvasContextColor,
                    isLoading = it.items.isEmpty(),
                    isRefreshing = it.items.isNotEmpty(),
                    isError = false,
                    gradePreferencesUiState = it.gradePreferencesUiState.copy(
                        canvasContextColor = gradesBehaviour.canvasContextColor
                    )
                )
            }

            customStatuses = repository.getCustomGradeStatuses(courseId, forceRefresh)
            val course = repository.loadCourse(courseId, forceRefresh)
            this@GradesViewModel.course = course
            val gradingPeriods = repository.loadGradingPeriods(courseId, forceRefresh)
            val currentGradingPeriod = gradingPeriods.getCurrentGradingPeriod()

            var selectedGradingPeriod = _uiState.value.gradePreferencesUiState.selectedGradingPeriod
            var sortBy = _uiState.value.gradePreferencesUiState.sortBy

            if (initialize) {
                selectedGradingPeriod = currentGradingPeriod
                sortBy = repository.getSortBy() ?: sortBy
            } else {
                repository.setSortBy(_uiState.value.gradePreferencesUiState.sortBy)
            }

            val assignmentGroups = repository.loadAssignmentGroups(courseId, selectedGradingPeriod?.id, forceRefresh).filterHiddenAssignments()
            val enrollments = repository.loadEnrollments(courseId, selectedGradingPeriod?.id, forceRefresh)

            courseGrade = repository.getCourseGrade(course, repository.studentId, enrollments, selectedGradingPeriod?.id)

            val items = when (sortBy) {
                SortBy.GROUP -> groupByAssignmentGroup(assignmentGroups)
                SortBy.DUE_DATE -> groupByDueDate(assignmentGroups)
            }.filter {
                it.assignments.isNotEmpty()
            }

            _uiState.update {
                it.copy(
                    items = items,
                    isLoading = false,
                    isRefreshing = false,
                    gradePreferencesUiState = it.gradePreferencesUiState.copy(
                        courseName = course.name,
                        gradingPeriods = gradingPeriods,
                        defaultGradingPeriod = currentGradingPeriod,
                        selectedGradingPeriod = selectedGradingPeriod,
                        sortBy = sortBy
                    ),
                    gradeText = gradeFormatter.getGradeString(course, courseGrade, !it.onlyGradedAssignmentsSwitchEnabled),
                    isGradeLocked = courseGrade?.isLocked.orDefault()
                )
            }
        } catch {
            _uiState.update {
                val showSnack = forceRefresh && it.items.isNotEmpty()
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = !showSnack,
                    snackbarMessage = context.getString(R.string.gradesRefreshFailed).takeIf { showSnack }
                )
            }
        }
    }

    private fun groupByAssignmentGroup(assignmentGroups: List<AssignmentGroup>) = assignmentGroups.map { group ->
        AssignmentGroupUiState(
            id = group.id,
            name = group.name.orEmpty(),
            assignments = mapAssignments(group.assignments),
            expanded = true
        )
    }

    private fun groupByDueDate(assignmentGroups: List<AssignmentGroup>): List<AssignmentGroupUiState> {
        val today = Date()

        val overdue = mutableListOf<Assignment>()
        val upcoming = mutableListOf<Assignment>()
        val undated = mutableListOf<Assignment>()
        val past = mutableListOf<Assignment>()

        assignmentGroups
            .flatMap { it.assignments }
            .forEach { assignment ->
                val dueAt = assignment.dueAt
                val submission = assignment.submission
                val isWithoutGradedSubmission = submission == null || submission.isWithoutGradedSubmission
                val isOverdue = assignment.isAllowedToSubmit && isWithoutGradedSubmission
                if (dueAt == null) {
                    undated.add(assignment)
                } else {
                    when {
                        today.before(dueAt.toDate()) -> upcoming.add(assignment)
                        isOverdue -> overdue.add(assignment)
                        else -> past.add(assignment)
                    }
                }
            }

        return listOf(
            AssignmentGroupUiState(
                id = 0,
                name = context.getString(R.string.overdueAssignments),
                assignments = mapAssignments(overdue),
                expanded = true
            ),
            AssignmentGroupUiState(
                id = 1,
                name = context.getString(R.string.upcomingAssignments),
                assignments = mapAssignments(upcoming),
                expanded = true
            ),
            AssignmentGroupUiState(
                id = 2,
                name = context.getString(R.string.undatedAssignments),
                assignments = mapAssignments(undated),
                expanded = true
            ),
            AssignmentGroupUiState(
                id = 3,
                name = context.getString(R.string.pastAssignments),
                assignments = mapAssignments(past),
                expanded = true
            )
        )
    }

    private fun mapAssignments(assignments: List<Assignment>) = assignments.sortedBy { it.position }.map { assignment ->
        val iconRes = assignment.getAssignmentIcon()

        val submissionStateLabel = assignment.getSubmissionStateLabel(customStatuses)

        AssignmentUiState(
            id = assignment.id,
            iconRes = iconRes,
            name = assignment.name.orEmpty(),
            dueDate = getDateText(assignment.dueDate),
            submissionStateLabel = submissionStateLabel,
            displayGrade = assignment.getGrade(
                submission = assignment.submission,
                context = context,
                restrictQuantitativeData = course?.settings?.restrictQuantitativeData.orDefault(),
                gradingScheme = course?.gradingScheme.orEmpty(),
                showZeroPossiblePoints = true,
                showNotGraded = true
            ),
            checkpoints = assignment.checkpoints.map { checkpoint ->
                val subAssignmentSubmission = assignment.submission?.subAssignmentSubmissions?.find {
                    it.subAssignmentTag == checkpoint.tag
                }

                DiscussionCheckpointUiState(
                    name = when (checkpoint.tag) {
                        Const.REPLY_TO_TOPIC -> context.getString(R.string.reply_to_topic)
                        Const.REPLY_TO_ENTRY -> context.getString(
                            R.string.additional_replies,
                            assignment.discussionTopicHeader?.replyRequiredCount
                        )
                        else -> checkpoint.name.orEmpty()
                    },
                    dueDate = getDateText(checkpoint.dueDate),
                    submissionStateLabel = assignment.getSubAssignmentSubmissionStateLabel(
                        subAssignmentSubmission,
                        customStatuses
                    ),
                    displayGrade = assignment.getSubAssignmentSubmissionGrade(
                        possiblePoints = checkpoint.pointsPossible.orDefault(),
                        submission = subAssignmentSubmission,
                        context = context,
                        restrictQuantitativeData = course?.settings?.restrictQuantitativeData.orDefault(),
                        gradingScheme = course?.gradingScheme.orEmpty(),
                        showZeroPossiblePoints = true,
                        showNotGraded = true
                    )
                )
            }
        )
    }

    private fun getDateText(dueAt: Date?) = dueAt?.let {
        val dateText = DateHelper.monthDayYearDateFormatUniversalShort.format(it)
        val timeText = DateHelper.getFormattedTime(context, it)
        context.getString(R.string.due, "$dateText $timeText")
    } ?: context.getString(R.string.gradesNoDueDate)

    fun handleAction(action: GradesAction) {
        when (action) {
            is GradesAction.Refresh -> {
                if (action.clearItems) _uiState.update { it.copy(items = emptyList()) }
                loadGrades(true)
            }

            is GradesAction.GroupHeaderClick -> {
                val items = uiState.value.items.map { group ->
                    if (group.id == action.id) {
                        group.copy(expanded = !group.expanded)
                    } else {
                        group
                    }
                }
                _uiState.update { it.copy(items = items) }
            }

            is GradesAction.ShowGradePreferences -> {
                _uiState.update { it.copy(gradePreferencesUiState = it.gradePreferencesUiState.copy(show = true)) }
            }

            is GradesAction.HideGradePreferences -> {
                _uiState.update { it.copy(gradePreferencesUiState = it.gradePreferencesUiState.copy(show = false)) }
            }

            is GradesAction.GradePreferencesUpdated -> {
                _uiState.update {
                    it.copy(
                        gradePreferencesUiState = it.gradePreferencesUiState.copy(
                            selectedGradingPeriod = action.gradingPeriod,
                            sortBy = action.sortBy
                        )
                    )
                }
                loadGrades(false)
            }

            is GradesAction.OnlyGradedAssignmentsSwitchCheckedChange -> {
                _uiState.update {
                    it.copy(
                        onlyGradedAssignmentsSwitchEnabled = action.checked,
                        gradeText = gradeFormatter.getGradeString(course, courseGrade, !action.checked)
                    )
                }
            }

            is GradesAction.AssignmentClick -> {
                viewModelScope.launch {
                    _events.send(GradesViewModelAction.NavigateToAssignmentDetails(courseId, action.id))
                }
            }

            is GradesAction.SnackbarDismissed -> {
                _uiState.update { it.copy(snackbarMessage = null) }
            }
        }
    }
}
