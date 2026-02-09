/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.forecast

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R
import com.instructure.pandautils.data.model.GradedSubmission
import com.instructure.pandautils.domain.usecase.assignment.LoadAssignmentGroupsUseCase
import com.instructure.pandautils.domain.usecase.assignment.LoadMissingAssignmentsParams
import com.instructure.pandautils.domain.usecase.assignment.LoadMissingAssignmentsUseCase
import com.instructure.pandautils.domain.usecase.assignment.LoadUpcomingAssignmentsParams
import com.instructure.pandautils.domain.usecase.assignment.LoadUpcomingAssignmentsUseCase
import com.instructure.pandautils.domain.usecase.audit.LoadRecentGradeChangesParams
import com.instructure.pandautils.domain.usecase.audit.LoadRecentGradeChangesUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCaseParams
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getIconForPlannerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ForecastWidgetViewModel @Inject constructor(
    private val loadMissingAssignmentsUseCase: LoadMissingAssignmentsUseCase,
    private val loadUpcomingAssignmentsUseCase: LoadUpcomingAssignmentsUseCase,
    private val loadRecentGradeChangesUseCase: LoadRecentGradeChangesUseCase,
    private val observeForecastConfigUseCase: ObserveForecastConfigUseCase,
    private val loadCourseUseCase: LoadCourseUseCase,
    private val loadAssignmentGroupsUseCase: LoadAssignmentGroupsUseCase,
    private val assignmentWeightCalculator: AssignmentWeightCalculator,
    private val forecastWidgetRouter: ForecastWidgetRouter,
    private val apiPrefs: ApiPrefs,
    private val crashlytics: FirebaseCrashlytics,
    private val resources: Resources
) : ViewModel() {

    private var missingAssignments: List<Assignment> = emptyList()
    private var upcomingPlannerItems: List<PlannerItem> = emptyList()
    private var recentGradedSubmissions: List<GradedSubmission> = emptyList()
    private var currentWeekOffset: Int = 0
    private var weekNavigationJob: Job? = null

    // Cache for courses and assignment groups
    private val coursesCache = mutableMapOf<Long, Course>()
    private val assignmentGroupsCache = mutableMapOf<Long, List<AssignmentGroup>>()

    private val _uiState = MutableStateFlow(
        ForecastWidgetUiState(
            onNavigatePrevious = ::navigatePrevious,
            onNavigateNext = ::navigateNext,
            onSectionSelected = ::toggleSection,
            onAssignmentClick = ::onAssignmentClick,
            onRetry = ::retry
        )
    )
    val uiState: StateFlow<ForecastWidgetUiState> = _uiState.asStateFlow()

    init {
        val initialWeekPeriod = calculateWeekPeriod(currentWeekOffset)
        _uiState.update { it.copy(weekPeriod = initialWeekPeriod) }

        observeConfig()
        loadData(forceRefresh = false)
    }

    private fun navigatePrevious() {
        currentWeekOffset -= 1
        updateWeekPeriodAndReload()
    }

    private fun navigateNext() {
        currentWeekOffset += 1
        updateWeekPeriodAndReload()
    }

    private fun updateWeekPeriodAndReload() {
        val weekPeriod = calculateWeekPeriod(currentWeekOffset)

        weekNavigationJob?.cancel()

        _uiState.update {
            it.copy(
                weekPeriod = weekPeriod,
                isLoadingItems = it.selectedSection != null,
                isError = false
            )
        }

        weekNavigationJob = viewModelScope.launch {
            delay(300)

            try {
                loadUpcomingAssignments(forceRefresh = true)
                loadRecentGrades(forceRefresh = true)
                _uiState.update { it.copy(isLoadingItems = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Only set error state if this job wasn't cancelled by new navigation
                if (isActive) {
                    _uiState.update { it.copy(isLoadingItems = false, isError = true) }
                    crashlytics.recordException(e)
                }
            }
        }
    }

    private fun toggleSection(section: ForecastSection) {
        val currentSection = _uiState.value.selectedSection
        val newSection = if (currentSection == section) null else section
        _uiState.update { it.copy(selectedSection = newSection) }
    }

    private fun onAssignmentClick(activity: FragmentActivity, assignmentId: Long, courseId: Long) {
        forecastWidgetRouter.routeToAssignmentDetails(activity, assignmentId, courseId)
    }

    private fun retry() {
        loadData(forceRefresh = true)
    }

    fun refresh() {
        loadData(forceRefresh = true)
    }

    private fun observeConfig() {
        viewModelScope.launch {
            observeForecastConfigUseCase(Unit)
                .catch { crashlytics.recordException(it) }
                .collect { config ->
                    val themedColor = ColorKeeper.createThemedColor(config.backgroundColor)
                    _uiState.update { it.copy(backgroundColor = themedColor) }
                }
        }
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }

            try {
                loadMissingAssignments(forceRefresh)
                loadUpcomingAssignments(forceRefresh)
                loadRecentGrades(forceRefresh)

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
                crashlytics.recordException(e)
            }
        }
    }

    private suspend fun loadMissingAssignments(forceRefresh: Boolean) {
        missingAssignments = loadMissingAssignmentsUseCase(LoadMissingAssignmentsParams(forceRefresh))
        val mappedAssignments = mapMissingAssignments(missingAssignments)
        _uiState.update {
            it.copy(
                missingAssignments = mappedAssignments,
                selectedSection = if (mappedAssignments.isNotEmpty()) ForecastSection.MISSING else null
            )
        }
    }

    private suspend fun loadUpcomingAssignments(forceRefresh: Boolean) {
        val weekPeriod = _uiState.value.weekPeriod ?: calculateWeekPeriod(currentWeekOffset)
        val startDate = weekPeriod.startDate.atStartOfDay().toApiString()
        val endDate = weekPeriod.endDate.atTime(23, 59, 59).toApiString()

        upcomingPlannerItems = loadUpcomingAssignmentsUseCase(
            LoadUpcomingAssignmentsParams(
                startDate = startDate.orEmpty(),
                endDate = endDate.orEmpty(),
                forceRefresh = forceRefresh
            )
        )
        _uiState.update {
            it.copy(dueAssignments = mapUpcomingAssignments(upcomingPlannerItems))
        }
    }

    private suspend fun loadRecentGrades(forceRefresh: Boolean) {
        val userId = apiPrefs.user?.id ?: return
        val weekPeriod = _uiState.value.weekPeriod ?: calculateWeekPeriod(currentWeekOffset)
        val startDate = weekPeriod.startDate.atStartOfDay().toApiString()
        val endDate = weekPeriod.endDate.atTime(23, 59, 59).toApiString()

        recentGradedSubmissions = loadRecentGradeChangesUseCase(
            LoadRecentGradeChangesParams(
                studentId = userId,
                startTime = startDate.orEmpty(),
                endTime = endDate.orEmpty(),
                forceRefresh = forceRefresh
            )
        )
        _uiState.update {
            it.copy(recentGrades = mapRecentGrades(recentGradedSubmissions))
        }
    }

    private suspend fun mapMissingAssignments(assignments: List<Assignment>): List<AssignmentItem> {
        return assignments
            .sortedBy { it.dueAt }
            .map { assignment ->
                val weight = calculateAssignmentWeight(assignment)
                AssignmentItem(
                    id = assignment.id,
                    courseId = assignment.courseId,
                    courseName = assignment.course?.name.orEmpty(),
                    assignmentName = assignment.name.orEmpty(),
                    dueDate = assignment.dueAt?.toDate(),
                    gradedDate = null,
                    pointsPossible = assignment.pointsPossible,
                    weight = weight,
                    iconRes = assignment.getAssignmentIcon(),
                    url = assignment.htmlUrl.orEmpty()
                )
            }
    }

    private suspend fun mapUpcomingAssignments(plannerItems: List<PlannerItem>): List<AssignmentItem> {
        return plannerItems
            .sortedBy { it.plannableDate }
            .map { item ->
                val weight = calculatePlannerItemWeight(item)
                AssignmentItem(
                    id = item.plannable.id,
                    courseId = item.courseId ?: 0,
                    courseName = item.contextName.orEmpty(),
                    assignmentName = item.plannable.title,
                    dueDate = item.plannableDate,
                    gradedDate = null,
                    pointsPossible = item.plannable.pointsPossible ?: 0.0,
                    weight = weight,
                    iconRes = item.getIconForPlannerItem(),
                    url = item.htmlUrl.orEmpty()
                )
            }
    }

    private suspend fun mapRecentGrades(submissions: List<GradedSubmission>): List<AssignmentItem> {
        return submissions
            .sortedByDescending { it.gradedAt }
            .map { submission ->
                val course = try {
                    getCourseOrLoad(submission.courseId)
                } catch (e: Exception) {
                    null
                }
                AssignmentItem(
                    id = submission.assignmentId,
                    courseId = submission.courseId,
                    courseName = submission.courseName,
                    assignmentName = submission.assignmentName,
                    dueDate = null,
                    gradedDate = submission.gradedAt,
                    pointsPossible = submission.pointsPossible ?: 0.0,
                    weight = null,
                    iconRes = R.drawable.ic_assignment,
                    url = submission.assignmentUrl ?: "",
                    score = submission.score,
                    grade = formatGrade(submission, course)
                )
            }
    }

    private fun formatGrade(submission: GradedSubmission, course: Course?): String? {
        if (submission.excused) {
            return resources.getString(R.string.gradingStatus_excused)
        }

        val grade = submission.grade
        if (grade.isNullOrBlank()) return null

        val restrictQuantitativeData = course?.settings?.restrictQuantitativeData == true

        // When quantitative data is restricted, only show the grade string (letter grade)
        if (restrictQuantitativeData) {
            return grade
        }

        // For point-based assignments, show "score/pointsPossible"
        if (submission.gradingType == GradingType.points) {
            val score = submission.score ?: return grade
            val pointsPossible = submission.pointsPossible ?: return grade

            val formattedScore = formatNumber(score)
            val formattedPoints = formatNumber(pointsPossible)
            return "$formattedScore / $formattedPoints"
        }

        // For other grading types, format the grade value
        return try {
            val number = grade.toDouble()
            formatNumber(number)
        } catch (_: NumberFormatException) {
            grade
        }
    }

    private fun formatNumber(number: Double): String {
        val formatted = "%.2f".format(Locale.getDefault(), number)
        return formatted.trimEnd('0').trimEnd('.')
    }

    private fun calculateWeekPeriod(offsetWeeks: Int): WeekPeriod {
        val locale = Locale.getDefault()
        val today = LocalDate.now()
        val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek

        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        val targetWeekStart = currentWeekStart.plusWeeks(offsetWeeks.toLong())
        val targetWeekEnd = targetWeekStart.plusDays(6)

        val currentYear = today.year
        val startYear = targetWeekStart.year
        val endYear = targetWeekEnd.year

        val displayText = when {
            startYear != currentYear || endYear != currentYear -> {
                val formatterWithYear = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)
                "${targetWeekStart.format(formatterWithYear)} - ${targetWeekEnd.format(formatterWithYear)}"
            }
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d", locale)
                "${targetWeekStart.format(formatter)} - ${targetWeekEnd.format(formatter)}"
            }
        }

        val weekNumber = targetWeekStart.get(WeekFields.of(locale).weekOfYear())

        return WeekPeriod(
            startDate = targetWeekStart,
            endDate = targetWeekEnd,
            displayText = displayText,
            weekNumber = weekNumber
        )
    }

    private suspend fun calculateAssignmentWeight(assignment: Assignment): Double? {
        return try {
            val course = getCourseOrLoad(assignment.courseId)
            val assignmentGroups = getAssignmentGroupsOrLoad(assignment.courseId)

            assignmentWeightCalculator.calculateWeight(assignment, course, assignmentGroups)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            null
        }
    }

    private suspend fun calculatePlannerItemWeight(plannerItem: PlannerItem): Double? {
        return try {
            val courseId = plannerItem.courseId ?: return null
            val assignmentId = plannerItem.plannable.id

            val course = getCourseOrLoad(courseId)
            val assignmentGroups = getAssignmentGroupsOrLoad(courseId)

            val assignment = assignmentGroups
                .flatMap { it.assignments }
                .find { it.id == assignmentId }
                ?: return null

            assignmentWeightCalculator.calculateWeight(assignment, course, assignmentGroups)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            null
        }
    }

    private suspend fun getCourseOrLoad(courseId: Long): Course {
        return coursesCache.getOrPut(courseId) {
            loadCourseUseCase(LoadCourseUseCaseParams(courseId, forceNetwork = false))
        }
    }

    private suspend fun getAssignmentGroupsOrLoad(courseId: Long): List<AssignmentGroup> {
        return assignmentGroupsCache.getOrPut(courseId) {
            loadAssignmentGroupsUseCase(LoadAssignmentGroupsUseCase.Params(courseId, forceNetwork = false))
        }
    }
}