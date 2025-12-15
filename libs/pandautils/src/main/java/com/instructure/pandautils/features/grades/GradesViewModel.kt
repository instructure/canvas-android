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
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertPercentScoreToLetterGrade
import com.instructure.canvasapi2.utils.convertPercentToPointBased
import com.instructure.canvasapi2.utils.getCurrentGradingPeriod
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.debounce
import com.instructure.pandautils.utils.filterHiddenAssignments
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getGrade
import com.instructure.pandautils.utils.getSubAssignmentSubmissionGrade
import com.instructure.pandautils.utils.getSubAssignmentSubmissionStateLabel
import com.instructure.pandautils.utils.getSubmissionStateLabel
import com.instructure.pandautils.utils.isAllowedToSubmitWithOverrides
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.orderedCheckpoints
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
    private val repository: GradesRepository,
    private val gradeFormatter: GradeFormatter,
    private val gradeCalculator: GradeCalculator,
    private val gradesViewModelBehavior: GradesViewModelBehavior,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(COURSE_ID_KEY).orDefault()

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<GradesViewModelAction>()
    val events = _events.receiveAsFlow()

    private var course: Course? = null
    private var courseGrade: CourseGrade? = null
    private var domainAssignmentGroups = emptyList<AssignmentGroup>()

    private var customStatuses = listOf<CustomGradeStatusesQuery.Node>()
    private var allItems = emptyList<AssignmentGroupUiState>()

    private val debouncedSearch = debounce<String>(
        coroutineScope = viewModelScope
    ) { query ->
        val filteredItems = filterItems(allItems, query)
        _uiState.update {
            it.copy(items = filteredItems)
        }
    }

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
                    isLoading = it.items.isEmpty(),
                    isRefreshing = it.items.isNotEmpty(),
                    isError = false
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

            val assignmentGroups =
                repository.loadAssignmentGroups(courseId, selectedGradingPeriod?.id, forceRefresh).filterHiddenAssignments()
            val enrollments = repository.loadEnrollments(courseId, selectedGradingPeriod?.id, forceRefresh)

            courseGrade = repository.getCourseGrade(course, repository.studentId, enrollments, selectedGradingPeriod?.id)
            domainAssignmentGroups = assignmentGroups

            allItems = when (sortBy) {
                SortBy.GROUP -> groupByAssignmentGroup(assignmentGroups)
                SortBy.DUE_DATE -> groupByDueDate(assignmentGroups)
            }.filter {
                it.assignments.isNotEmpty()
            }

            val filteredItems = filterItems(allItems, _uiState.value.searchQuery)

            val isWhatIfGradingEnabled = gradesViewModelBehavior.isWhatIfGradingEnabled(
                course,
                assignmentGroups,
                selectedGradingPeriod
            )

            _uiState.update {
                it.copy(
                    items = filteredItems,
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
                    isGradeLocked = courseGrade?.isLocked.orDefault(),
                    isWhatIfGradingEnabled = isWhatIfGradingEnabled,
                    showWhatIfScore = if (isWhatIfGradingEnabled) it.showWhatIfScore else false
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
                val dueAt = assignment.dueAt ?: assignment.orderedCheckpoints.firstOrNull { it.dueAt != null }?.dueAt
                val submission = assignment.submission
                val isWithoutGradedSubmission = submission == null || submission.isWithoutGradedSubmission
                val isOverdue = assignment.isAllowedToSubmitWithOverrides(course) && isWithoutGradedSubmission
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
                resources = context.resources,
                restrictQuantitativeData = course?.settings?.restrictQuantitativeData.orDefault(),
                gradingScheme = course?.gradingScheme.orEmpty(),
                showZeroPossiblePoints = true,
                showNotGraded = true
            ),
            checkpoints = assignment.orderedCheckpoints.map { checkpoint ->
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
                        resources = context.resources,
                        restrictQuantitativeData = course?.settings?.restrictQuantitativeData.orDefault(),
                        gradingScheme = course?.gradingScheme.orEmpty(),
                        showZeroPossiblePoints = true,
                        showNotGraded = true
                    ),
                    pointsPossible = checkpoint.pointsPossible?.toInt().orDefault()
                )
            },
            score = assignment.submission?.score,
            maxScore = assignment.pointsPossible,
            whatIfScore = null,
            checkpointsExpanded = false
        )
    }

    private fun getDateText(dueAt: Date?) = dueAt?.let {
        val dateText = DateHelper.monthDayYearDateFormatUniversalShort.format(it)
        val timeText = DateHelper.getFormattedTime(context, it)
        context.getString(R.string.due, "$dateText $timeText")
    } ?: context.getString(R.string.gradesNoDueDate)

    private fun filterItems(items: List<AssignmentGroupUiState>, query: String): List<AssignmentGroupUiState> {
        if (query.length < 3) return items

        return items.mapNotNull { group ->
            val filteredAssignments = group.assignments.filter { assignment ->
                assignment.name.contains(query, ignoreCase = true)
            }
            if (filteredAssignments.isEmpty()) {
                null
            } else {
                group.copy(assignments = filteredAssignments)
            }
        }
    }

    private fun calculateWhatIfGrade(
        items: List<AssignmentGroupUiState>,
        onlyGraded: Boolean
    ): String {
        val currentCourse = course ?: return ""

        val whatIfScores = items
            .flatMap { it.assignments }
            .filter { it.whatIfScore != null }
            .associate { it.id to it.whatIfScore!! }

        if (whatIfScores.isEmpty()) {
            return gradeFormatter.getGradeString(currentCourse, courseGrade, !onlyGraded)
        }

        val applyGroupWeights = currentCourse.isApplyAssignmentGroupWeights
        val calculatedGrade = gradeCalculator.calculateGrade(
            groups = domainAssignmentGroups,
            whatIfScores = whatIfScores,
            applyGroupWeights = applyGroupWeights,
            onlyGraded = onlyGraded
        )

        val restrictQuantitativeData = currentCourse.settings?.restrictQuantitativeData.orDefault()
        return if (restrictQuantitativeData) {
            val gradingScheme = currentCourse.gradingScheme
            if (gradingScheme.isNotEmpty()) {
                convertPercentScoreToLetterGrade(calculatedGrade / 100, gradingScheme)
            } else {
                context.getString(R.string.noGradeText)
            }
        } else {
            val result = if (currentCourse.pointsBasedGradingScheme) {
                convertPercentToPointBased(calculatedGrade, currentCourse.scalingFactor)
            } else {
                NumberHelper.doubleToPercentage(calculatedGrade)
            }
            if (courseGrade?.hasFinalGradeString() == true || courseGrade?.hasCurrentGradeString() == true) {
                val letterGrade = convertPercentScoreToLetterGrade(calculatedGrade / 100, currentCourse.gradingScheme)
                "$result $letterGrade"
            } else {
                result
            }
        }
    }

    private fun updateAssignmentWhatIfScore(
        items: List<AssignmentGroupUiState>,
        assignmentId: Long,
        score: Double?
    ): List<AssignmentGroupUiState> {
        return items.map { group ->
            group.copy(
                assignments = group.assignments.map { assignment ->
                    if (assignment.id == assignmentId) {
                        assignment.copy(whatIfScore = score)
                    } else {
                        assignment
                    }
                }
            )
        }
    }

    fun handleAction(action: GradesAction) {
        when (action) {
            is GradesAction.Refresh -> {
                _uiState.update {
                    it.copy(
                        items = if (action.clearItems) emptyList() else it.items,
                        showWhatIfScore = false
                    )
                }
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
                _uiState.update { state ->
                    val newGradeText = if (state.showWhatIfScore) {
                        calculateWhatIfGrade(state.items, action.checked)
                    } else {
                        gradeFormatter.getGradeString(course, courseGrade, !action.checked)
                    }

                    state.copy(
                        onlyGradedAssignmentsSwitchEnabled = action.checked,
                        gradeText = newGradeText
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

            is GradesAction.ToggleCheckpointsExpanded -> {
                val items = uiState.value.items.map { group ->
                    group.copy(
                        assignments = group.assignments.map {
                            if (it.id == action.assignmentId) {
                                it.copy(checkpointsExpanded = !it.checkpointsExpanded)
                            } else {
                                it
                            }
                        }
                    )
                }
                _uiState.update { it.copy(items = items) }
            }

            is GradesAction.ToggleSearch -> {
                val isExpanding = !uiState.value.isSearchExpanded
                _uiState.update {
                    it.copy(
                        isSearchExpanded = isExpanding,
                        searchQuery = if (!isExpanding) "" else it.searchQuery,
                        items = if (!isExpanding) allItems else it.items
                    )
                }
            }

            is GradesAction.SearchQueryChanged -> {
                _uiState.update {
                    it.copy(searchQuery = action.query)
                }
                debouncedSearch(action.query)
            }

            is GradesAction.ShowWhatIfScoreSwitchCheckedChange -> {
                _uiState.update { state ->
                    val newGradeText = if (action.checked) {
                        calculateWhatIfGrade(state.items, state.onlyGradedAssignmentsSwitchEnabled)
                    } else {
                        gradeFormatter.getGradeString(course, courseGrade, !state.onlyGradedAssignmentsSwitchEnabled)
                    }

                    state.copy(
                        showWhatIfScore = action.checked,
                        gradeText = newGradeText
                    )
                }
            }

            is GradesAction.ShowWhatIfScoreDialog -> {
                val assignment = uiState.value.items
                    .flatMap { it.assignments }
                    .find { it.id == action.assignmentId }

                assignment?.let {
                    val currentScore = it.score
                    val maxScore = it.maxScore

                    val currentScoreText = if (currentScore != null) {
                        context.getString(R.string.whatIfScoreCurrentGraded, currentScore, maxScore)
                    } else {
                        context.getString(R.string.whatIfScoreCurrentUngraded, maxScore)
                    }

                    _uiState.update { state ->
                        state.copy(
                            whatIfScoreDialogData = WhatIfScoreDialogData(
                                assignmentId = assignment.id,
                                assignmentName = assignment.name,
                                currentScoreText = currentScoreText,
                                whatIfScore = assignment.whatIfScore,
                                maxScore = maxScore
                            )
                        )
                    }
                }
            }

            is GradesAction.HideWhatIfScoreDialog -> {
                _uiState.update { it.copy(whatIfScoreDialogData = null) }
            }

            is GradesAction.UpdateWhatIfScore -> {
                _uiState.update { state ->
                    val updatedItems = updateAssignmentWhatIfScore(state.items, action.assignmentId, action.score)
                    allItems = updateAssignmentWhatIfScore(allItems, action.assignmentId, action.score)

                    val newGradeText = if (state.showWhatIfScore) {
                        calculateWhatIfGrade(updatedItems, state.onlyGradedAssignmentsSwitchEnabled)
                    } else {
                        state.gradeText
                    }

                    state.copy(
                        items = updatedItems,
                        gradeText = newGradeText,
                        whatIfScoreDialogData = null
                    )
                }
            }
        }
    }
}
