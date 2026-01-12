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

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.data.model.GradedSubmission
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.domain.usecase.assignment.LoadMissingAssignmentsParams
import com.instructure.pandautils.domain.usecase.assignment.LoadMissingAssignmentsUseCase
import com.instructure.pandautils.domain.usecase.assignment.LoadUpcomingAssignmentsParams
import com.instructure.pandautils.domain.usecase.assignment.LoadUpcomingAssignmentsUseCase
import com.instructure.pandautils.domain.usecase.audit.LoadRecentGradeChangesParams
import com.instructure.pandautils.domain.usecase.audit.LoadRecentGradeChangesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ForecastWidgetViewModel @Inject constructor(
    private val loadMissingAssignmentsUseCase: LoadMissingAssignmentsUseCase,
    private val loadUpcomingAssignmentsUseCase: LoadUpcomingAssignmentsUseCase,
    private val loadRecentGradeChangesUseCase: LoadRecentGradeChangesUseCase,
    private val observeForecastConfigUseCase: ObserveForecastConfigUseCase,
    private val forecastWidgetDataStore: ForecastWidgetDataStore,
    private val forecastWidgetRouter: ForecastWidgetRouter,
    private val apiPrefs: ApiPrefs,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private var missingAssignments: List<Assignment> = emptyList()
    private var upcomingPlannerItems: List<PlannerItem> = emptyList()
    private var recentGradedSubmissions: List<GradedSubmission> = emptyList()
    private var currentWeekOffset: Int = 0
    private var weekNavigationJob: Job? = null

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

        observeSelectedSection()
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
                dueAssignments = mapUpcomingAssignments(upcomingPlannerItems),
                isLoadingItems = it.selectedSection != null
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
                _uiState.update { it.copy(isLoadingItems = false, isError = true) }
                crashlytics.recordException(e)
            }
        }
    }

    private fun toggleSection(section: ForecastSection) {
        viewModelScope.launch {
            val currentSection = _uiState.value.selectedSection
            val newSection = if (currentSection == section) null else section
            forecastWidgetDataStore.setSelectedSection(newSection)
        }
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

    private fun observeSelectedSection() {
        viewModelScope.launch {
            forecastWidgetDataStore.observeSelectedSection()
                .catch { crashlytics.recordException(it) }
                .collect { section ->
                    _uiState.update { it.copy(selectedSection = section) }
                }
        }
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
        _uiState.update {
            it.copy(missingAssignments = mapMissingAssignments(missingAssignments))
        }
    }

    private suspend fun loadUpcomingAssignments(forceRefresh: Boolean) {
        val weekPeriod = _uiState.value.weekPeriod ?: calculateWeekPeriod(currentWeekOffset)
        val startDate = weekPeriod.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endDate = weekPeriod.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()

        upcomingPlannerItems = loadUpcomingAssignmentsUseCase(
            LoadUpcomingAssignmentsParams(
                startDate = startDate.toString(),
                endDate = endDate.toString(),
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
        val startDate = weekPeriod.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endDate = weekPeriod.endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()

        recentGradedSubmissions = loadRecentGradeChangesUseCase(
            LoadRecentGradeChangesParams(
                studentId = userId,
                startTime = startDate.toString(),
                endTime = endDate.toString(),
                forceRefresh = forceRefresh
            )
        )
        _uiState.update {
            it.copy(recentGrades = mapRecentGrades(recentGradedSubmissions))
        }
    }

    private fun mapMissingAssignments(assignments: List<Assignment>): List<AssignmentItem> {
        return assignments
            .sortedBy { it.dueAt }
            .map { assignment ->
                AssignmentItem(
                    id = assignment.id,
                    courseId = assignment.courseId,
                    courseName = assignment.course?.name.orEmpty(),
                    assignmentName = assignment.name.orEmpty(),
                    dueDate = assignment.dueAt?.toDate(),
                    gradedDate = null,
                    pointsPossible = assignment.pointsPossible,
                    weight = null, // TODO: Add weight if available
                    iconRes = assignment.getAssignmentIcon(),
                    url = assignment.htmlUrl.orEmpty()
                )
            }
    }

    private fun mapUpcomingAssignments(plannerItems: List<PlannerItem>): List<AssignmentItem> {
        return plannerItems
            .sortedBy { it.plannableDate }
            .map { item ->
                AssignmentItem(
                    id = item.plannable.id,
                    courseId = item.courseId ?: 0,
                    courseName = item.contextName.orEmpty(),
                    assignmentName = item.plannable.title,
                    dueDate = item.plannableDate,
                    gradedDate = null,
                    pointsPossible = item.plannable.pointsPossible ?: 0.0,
                    weight = null,
                    iconRes = item.getIconForPlannerItem(),
                    url = item.htmlUrl.orEmpty()
                )
            }
    }

    private fun mapRecentGrades(submissions: List<GradedSubmission>): List<AssignmentItem> {
        return submissions
            .sortedByDescending { it.gradedAt }
            .map { submission ->
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
                    grade = formatGrade(submission.grade)
                )
            }
    }

    private fun formatGrade(grade: String?): String? {
        if (grade.isNullOrBlank()) return null

        return try {
            val number = grade.toDouble()
            val formatted = "%.2f".format(Locale.US, number)
            formatted.trimEnd('0').trimEnd('.')
        } catch (_: NumberFormatException) {
            grade
        }
    }

    private fun calculateWeekPeriod(offsetWeeks: Int): WeekPeriod {
        val locale = getLocale()
        val today = LocalDate.now()
        val firstDayOfWeek = if (locale.country == "US") DayOfWeek.SUNDAY else DayOfWeek.MONDAY

        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        val targetWeekStart = currentWeekStart.plusWeeks(offsetWeeks.toLong())
        val targetWeekEnd = targetWeekStart.plusDays(6)

        val formatter = DateTimeFormatter.ofPattern("MMM d", locale)
        val displayText = "${targetWeekStart.format(formatter)} - ${targetWeekEnd.format(formatter)}"

        val weekNumber = targetWeekStart.get(WeekFields.of(locale).weekOfYear())

        return WeekPeriod(
            startDate = targetWeekStart,
            endDate = targetWeekEnd,
            displayText = displayText,
            weekNumber = weekNumber
        )
    }

    private fun getLocale(): Locale {
        val selectedLocale = apiPrefs.selectedLocale
        return if (!selectedLocale.isNullOrEmpty()) {
            Locale.forLanguageTag(selectedLocale)
        } else {
            Locale.getDefault()
        }
    }
}