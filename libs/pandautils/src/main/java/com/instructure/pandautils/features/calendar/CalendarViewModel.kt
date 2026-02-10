/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.calendar

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.pandautils.utils.toLocalDateOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.sign

private const val MONTH_COUNT = 12

@HiltViewModel
class CalendarViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarRepository: CalendarRepository,
    private val apiPrefs: ApiPrefs,
    private val clock: Clock,
    private val calendarPrefs: CalendarPrefs,
    private val calendarStateMapper: CalendarStateMapper,
    private val calendarSharedEvents: CalendarSharedEvents,
    private val calendarBehavior: CalendarBehavior,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialSelectedDayString = savedStateHandle.get<String>(CalendarFragment.SELECTED_DAY)
    private var selectedDay = initialSelectedDayString?.toLocalDateOrNull() ?: LocalDate.now(clock)

    // Helper fields to handle page change animations when a day in a different month is selected
    private var pendingSelectedDay: LocalDate? = null
    private var scrollToPageOffset: Int = 0
    private var jumpToToday = false

    private var expandAllowed = true
    private var expanded = calendarPrefs.calendarExpanded && expandAllowed
    private var collapsing = false

    private val eventsByDay = mutableMapOf<LocalDate, MutableList<PlannerItem>>()
    private val loadingDays = mutableSetOf<LocalDate>()
    private val errorDays = mutableSetOf<LocalDate>()
    private val refreshingDays = mutableSetOf<LocalDate>()
    private val loadedMonths = mutableSetOf<YearMonth>()

    private val contextIdFilters = mutableSetOf<String>()

    private val _uiState =
        MutableStateFlow(CalendarScreenUiState(createCalendarUiState(loadingMonths = true)))
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CalendarViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        loadVisibleMonths()
        initializeCalendarBehavior()
    }
    
    private fun initializeCalendarBehavior() {
        viewModelScope.launch {
            val shouldShowAddEventButton = calendarBehavior.shouldShowAddEventButton()
            _uiState.update { it.copy(showAddEventButton = shouldShowAddEventButton) }
        }
    }

    private suspend fun loadFilters(filtersFromDb: CalendarFilterEntity?) {
        val result = calendarRepository.getCanvasContexts()
        if (result is DataResult.Success) {
            val canvasContexts = result.data
            val userIds = canvasContexts[CanvasContext.Type.USER]?.map { it.contextId } ?: emptyList()
            val courseIds = canvasContexts[CanvasContext.Type.COURSE]?.map { it.contextId } ?: emptyList()
            val groupIds = canvasContexts[CanvasContext.Type.GROUP]?.map { it.contextId } ?: emptyList()

            if (filtersFromDb == null && apiPrefs.user?.id != null) {
                val filterLimit = calendarRepository.getCalendarFilterLimit()
                val initialFilters = if (filterLimit != -1) {
                    (userIds + courseIds + groupIds).take(filterLimit)
                } else {
                    userIds + courseIds + groupIds
                }
                contextIdFilters.addAll(initialFilters)
                val filter = CalendarFilterEntity(
                    userDomain = apiPrefs.fullDomain,
                    userId = apiPrefs.user!!.id.toString(),
                    filters = contextIdFilters
                )
                calendarRepository.updateCalendarFilters(filter)
            } else if (calendarPrefs.firstStart) { // Case where we already have filters in the DB from the Flutter version, this can only happen in the student app
                calendarPrefs.firstStart = false
                if (contextIdFilters.isEmpty()) {
                    contextIdFilters.addAll(userIds)
                    contextIdFilters.addAll(courseIds)
                    contextIdFilters.addAll(groupIds)
                } else if (contextIdFilters.containsAll(userIds) && contextIdFilters.containsAll(courseIds)) {
                    // This is the case where previously all filters were selected, but groups were not supported so we should add those.
                    contextIdFilters.addAll(canvasContexts.values.flatten().map { it.contextId })
                }
            }
        }
    }

    private suspend fun initFiltersFromDb(): CalendarFilterEntity? {
        val filters = calendarRepository.getCalendarFilters()
        if (filters != null) {
            contextIdFilters.clear()
            contextIdFilters.addAll(filters.filters)
        }
        return filters
    }

    private fun loadVisibleMonths() {
        viewModelScope.launch {
            if (contextIdFilters.isEmpty()) {
                val filters = initFiltersFromDb()
                loadFilters(filters)
            }

            val loadedStates = awaitAll(
                async { loadEventsForMonth(selectedDay) },
                async { loadEventsForMonth(selectedDay.plusMonths(1)) },
                async { loadEventsForMonth(selectedDay.minusMonths(1)) }
            )

            if (loadedStates.all { it }) {
                _uiState.emit(createNewUiState(loadingMonths = false))
            }
        }
    }

    /**
     * @return true if the month was loaded, false if it was already loaded
     */
    private suspend fun loadEventsForMonth(date: LocalDate, refresh: Boolean = false): Boolean {
        val yearMonth = YearMonth.from(date)
        if (loadedMonths.contains(yearMonth) && !refresh) return false

        loadedMonths.add(yearMonth) // We add it here because we don't want to reload even when it's loading

        val startDate = date.withDayOfMonth(1).atStartOfDay()
        val endDate = date.plusMonths(1).withDayOfMonth(1).atStartOfDay()

        val daysToFetch = daysBetweenDates(startDate, endDate)
        try {
            errorDays.removeAll(daysToFetch)
            if (refresh) {
                refreshingDays.addAll(daysToFetch)
            } else {
                loadingDays.addAll(daysToFetch)
            }
            _uiState.emit(createNewUiState())

            val result = calendarRepository.getPlannerItems(
                startDate.toApiString() ?: "",
                endDate.toApiString() ?: "",
                contextIdFilters.toList(),
                true
            )

            if (refresh) {
                refreshingDays.removeAll(daysToFetch)
            } else {
                loadingDays.removeAll(daysToFetch)
            }

            // If this was a refresh we should clear all events only after we have the new ones.
            // At this point we should already have a successful request.
            if (refresh) {
                eventsByDay.clear()
                loadedMonths.clear()
                loadedMonths.add(yearMonth)
            }

            storeResults(result)
            _uiState.emit(createNewUiState())
            return true
        } catch (e: Exception) {
            if (refresh) {
                refreshingDays.removeAll(daysToFetch)
                viewModelScope.launch {
                    _uiState.emit(createNewUiState().copy(snackbarMessage = context.getString(R.string.calendarRefreshFailed)))
                }
            } else {
                loadedMonths.remove(yearMonth)
                loadingDays.removeAll(daysToFetch)
                errorDays.addAll(daysToFetch)
                viewModelScope.launch {
                    _uiState.emit(createNewUiState())
                }
            }
            return true
        }
    }

    private fun daysBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        var loadingDate = startDate
        while (loadingDate != endDate) {
            result.add(loadingDate.toLocalDate())
            loadingDate = loadingDate.plusDays(1)
        }

        return result
    }

    private fun createNewUiState(loadingMonths: Boolean? = null): CalendarScreenUiState {
        val currentDayForEvents = pendingSelectedDay ?: selectedDay
        val currentPage = createEventsPageForDate(currentDayForEvents)
        val previousPage = createEventsPageForDate(currentDayForEvents.minusDays(1))
        val nextPage = createEventsPageForDate(currentDayForEvents.plusDays(1))

        return _uiState.value.copy(
            calendarUiState = createCalendarUiState(loadingMonths),
            calendarEventsUiState = CalendarEventsUiState(
                previousPage = previousPage,
                currentPage = currentPage,
                nextPage = nextPage
            )
        )
    }

    private fun createCalendarUiState(loadingMonths: Boolean? = null): CalendarUiState {
        val eventIndicators = eventsByDay
            .mapValues {
                min(3, it.value.filter { plannerItem ->
                    contextIdFilters.contains(plannerItem.canvasContext.contextId)
                }.size)
            }
        return CalendarUiState(
            selectedDay = selectedDay,
            expanded = expanded && !collapsing,
            headerUiState = calendarStateMapper.createHeaderUiState(
                selectedDay,
                pendingSelectedDay,
                loadingMonths ?: _uiState.value.calendarUiState.headerUiState.loadingMonths
            ),
            bodyUiState = calendarStateMapper.createBodyUiState(expanded, selectedDay, jumpToToday, scrollToPageOffset, eventIndicators),
            scrollToPageOffset = scrollToPageOffset,
            pendingSelectedDay = pendingSelectedDay,
            todayTapped = jumpToToday
        )
    }

    private fun createEventsPageForDate(date: LocalDate): CalendarEventsPageUiState {
        val eventUiStates = eventsByDay[date]
            ?.filter { plannerItem ->
                contextIdFilters.contains(plannerItem.canvasContext.contextId)
            }
            ?.map {
                EventUiState(
                    it.plannable.id,
                    contextName = getContextNameForPlannerItem(it),
                    canvasContext = it.canvasContext,
                    iconRes = it.getIconForPlannerItem(),
                    name = it.plannable.title,
                    date = getDateForPlannerItem(it),
                    status = getStatusForPlannerItem(it),
                    tag = it.getTagForPlannerItem(context),
                )
            } ?: emptyList()

        return CalendarEventsPageUiState(
            date = date,
            loading = loadingDays.contains(date),
            refreshing = refreshingDays.contains(date),
            error = errorDays.contains(date),
            events = eventUiStates
        )
    }

    private fun getContextNameForPlannerItem(plannerItem: PlannerItem): String {
        return if (plannerItem.plannableType == PlannableType.PLANNER_NOTE) {
            if (plannerItem.contextName.isNullOrEmpty()) {
                context.getString(R.string.userCalendarToDo)
            } else {
                context.getString(R.string.courseToDo, plannerItem.contextName)
            }
        } else {
            plannerItem.contextName.orEmpty()
        }
    }

    private fun getDateForPlannerItem(plannerItem: PlannerItem): String? {
        return if (plannerItem.plannableType == PlannableType.PLANNER_NOTE) {
            plannerItem.plannable.todoDate.toDate()?.let {
                val dateText = DateHelper.dayMonthDateFormat.format(it)
                val timeText = DateHelper.getFormattedTime(context, it)
                context.getString(R.string.calendarAtDateTime, dateText, timeText)
            }
        } else if (plannerItem.plannableType == PlannableType.CALENDAR_EVENT) {
            val startDate = plannerItem.plannable.startAt
            val endDate = plannerItem.plannable.endAt
            if (startDate != null && endDate != null) {
                val dateText = DateHelper.dayMonthDateFormat.format(startDate)
                val startText = DateHelper.getFormattedTime(context, startDate)
                val endText = DateHelper.getFormattedTime(context, endDate)
                if (plannerItem.plannable.allDay == true) {
                    dateText
                } else if (startDate == endDate) {
                    context.getString(R.string.calendarAtDateTime, dateText, startText)
                } else {
                    context.getString(R.string.calendarFromTo, dateText, startText, endText)
                }
            } else null
        } else {
            plannerItem.plannable.dueAt?.let {
                val dateText = DateHelper.dayMonthDateFormat.format(it)
                val timeText = DateHelper.getFormattedTime(context, it)
                context.getString(R.string.calendarDueDate, dateText, timeText)
            }
        }
    }

    private fun getStatusForPlannerItem(plannerItem: PlannerItem): String? {
        val submissionState = plannerItem.submissionState
        return if (submissionState != null) {
            when {
                submissionState.excused -> context.getString(R.string.calendarEventExcused)
                submissionState.missing -> context.getString(R.string.calendarEventMissing)
                submissionState.graded -> context.getString(R.string.calendarEventGraded)
                submissionState.needsGrading -> context.getString(R.string.calendarEventSubmitted)
                plannerItem.plannable.pointsPossible != null -> context.getString(
                    R.string.calendarEventPoints,
                    NumberHelper.formatDecimal(plannerItem.plannable.pointsPossible!!, 1, true)
                )

                else -> null
            }
        } else {
            null
        }
    }

    fun handleAction(calendarAction: CalendarAction) {
        when (calendarAction) {
            is CalendarAction.DaySelected -> selectedDayChangedWithPageAnimation(calendarAction.selectedDay)
            is CalendarAction.ExpandChanged -> {
                if (expandAllowed) {
                    expandChangedWithAnimation(calendarAction.isExpanded)
                }
            }
            CalendarAction.ExpandDisabled -> {
                expandAllowed = false
                expandChanged(false, save = false)
            }
            CalendarAction.ExpandEnabled -> {
                if (!expandAllowed) {
                    expandAllowed = true
                    expandChanged(calendarPrefs.calendarExpanded, save = false)
                }
            }
            CalendarAction.TodayTapped -> {
                jumpToToday = true
                selectedDayChangedWithPageAnimation(LocalDate.now(clock))
            }
            is CalendarAction.PageChanged -> pageChanged(calendarAction.offset.toLong())
            is CalendarAction.EventPageChanged -> selectedDayChangedWithPageAnimation(selectedDay.plusDays(calendarAction.offset.toLong()))
            is CalendarAction.EventSelected -> openSelectedEvent(calendarAction.id)
            is CalendarAction.RefreshDay -> refreshDay(calendarAction.date)
            CalendarAction.Retry -> loadVisibleMonths()
            CalendarAction.SnackbarDismissed -> viewModelScope.launch {
                _uiState.emit(createNewUiState().copy(snackbarMessage = null))
            }

            CalendarAction.HeightAnimationFinished -> heightAnimationFinished()
            is CalendarAction.AddToDoTapped -> viewModelScope.launch {
                _events.send(CalendarViewModelAction.OpenCreateToDo(selectedDay.toApiString()))
            }

            CalendarAction.FilterTapped -> showFilters()
            CalendarAction.FiltersRefreshed -> {
                viewModelScope.launch {
                    initFiltersFromDb()
                    if (calendarRepository.getCalendarFilterLimit() == -1) { // If we don't have a limit just filter locally
                        _uiState.emit(createNewUiState())
                    } else {
                        clearAndReloadCalendar()
                    }
                }
            }
            is CalendarAction.AddEventTapped -> viewModelScope.launch {
                _events.send(CalendarViewModelAction.OpenCreateEvent(selectedDay.toApiString()))
            }
            is CalendarAction.RefreshCalendar -> viewModelScope.launch {
                clearAndReloadCalendar()
            }
            CalendarAction.PullToRefresh -> refreshCalendar()
            CalendarAction.TodayTapHandled -> _uiState.update {
                it.copy(
                    calendarUiState = it.calendarUiState.copy(todayTapped = false)
                )
            }
        }
    }

    private suspend fun clearAndReloadCalendar() {
        eventsByDay.clear()
        loadedMonths.clear()
        contextIdFilters.clear()

        _uiState.emit(createNewUiState(loadingMonths = true))

        loadVisibleMonths()
    }

    private fun selectedDayChangedWithPageAnimation(newDay: LocalDate) {
        val offset = if (expanded) {
            (newDay.year - selectedDay.year) * MONTH_COUNT + newDay.monthValue - selectedDay.monthValue
        } else {
            calculateWeekOffset(selectedDay, newDay)
        }

        if (offset == 0) {
            selectedDayChanged(newDay)
        } else {
            // Animate page change
            scrollToPageOffset = offset.sign
            pendingSelectedDay = newDay
            viewModelScope.launch {
                _uiState.emit(createNewUiState())
            }
        }
    }

    private fun calculateWeekOffset(currentDate: LocalDate, newDate: LocalDate): Int {
        val currentDayOfWeek = currentDate.dayOfWeek

        // Calculate the start and end of the current week
        // We need the modulo 7 because the first day of the week is Sunday, and it's value is 7, but should be 0 here.
        val startOfWeek = currentDate.minusDays(currentDayOfWeek.value.toLong() % 7)
        val endOfWeek = startOfWeek.plusDays(6)

        return when {
            newDate.isBefore(startOfWeek) -> -1
            newDate.isAfter(endOfWeek) -> 1
            else -> 0
        }
    }

    private fun selectedDayChanged(newDay: LocalDate) {
        selectedDay = newDay
        calendarSharedEvents.sendEvent(viewModelScope, SharedCalendarAction.TodayButtonVisible(selectedDay != LocalDate.now(clock)))
        viewModelScope.launch {
            _uiState.emit(createNewUiState())
        }
        loadVisibleMonths()
    }

    private fun pageChanged(offset: Long) {
        jumpToToday = false
        if (pendingSelectedDay != null) {
            // This is a page change animation triggered by an other event so we don't care about the offset
            scrollToPageOffset = 0
            val dayToSelect = pendingSelectedDay!!
            pendingSelectedDay = null
            selectedDayChanged(dayToSelect)
        } else {
            val dateFieldToAdd = if (expanded) ChronoUnit.MONTHS else ChronoUnit.WEEKS
            selectedDayChanged(selectedDay.plus(offset, dateFieldToAdd))
        }
    }

    private fun expandChangedWithAnimation(expanded: Boolean) {
        if (this.expanded && !expanded) {
            collapsing = true
            viewModelScope.launch {
                _uiState.emit(createNewUiState())
            }
        } else {
            expandChanged(expanded, true)
        }
    }

    private fun expandChanged(expanded: Boolean, save: Boolean) {
        this.expanded = expanded
        if (save) {
            calendarPrefs.calendarExpanded = expanded
        }
        viewModelScope.launch {
            _uiState.emit(createNewUiState())
        }
    }

    private fun openSelectedEvent(id: Long) {
        val plannerItem = eventsByDay.values.flatten().find { it.plannable.id == id } ?: return

        viewModelScope.launch {
            val event = when (plannerItem.plannableType) {
                PlannableType.ASSIGNMENT -> {
                    CalendarViewModelAction.OpenAssignment(
                        plannerItem.canvasContext,
                        plannerItem.plannable.assignmentId ?: plannerItem.plannable.id
                    )
                }

                PlannableType.SUB_ASSIGNMENT -> {
                    val regex = """assignments/(\d+)""".toRegex()
                    val matchResult = regex.find(plannerItem.htmlUrl.orEmpty())
                    matchResult?.groupValues?.getOrNull(1)?.toLongOrNull()?.let {
                        CalendarViewModelAction.OpenAssignment(plannerItem.canvasContext, it)
                    }
                }

                PlannableType.DISCUSSION_TOPIC -> {
                    CalendarViewModelAction.OpenDiscussion(plannerItem.canvasContext, plannerItem.plannable.id, plannerItem.plannable.assignmentId)
                }

                PlannableType.QUIZ -> {
                    if (plannerItem.plannable.assignmentId != null) {
                        // This is a quiz assignment, go to the assignment page
                        CalendarViewModelAction.OpenAssignment(plannerItem.canvasContext, plannerItem.plannable.assignmentId!!)
                    } else {
                        var htmlUrl = plannerItem.htmlUrl.orEmpty()
                        if (htmlUrl.startsWith('/')) htmlUrl = apiPrefs.fullDomain + htmlUrl
                        CalendarViewModelAction.OpenQuiz(plannerItem.canvasContext, htmlUrl)
                    }
                }

                PlannableType.CALENDAR_EVENT -> {
                    CalendarViewModelAction.OpenCalendarEvent(plannerItem.canvasContext, plannerItem.plannable.id)
                }

                PlannableType.PLANNER_NOTE -> {
                    CalendarViewModelAction.OpenToDo(plannerItem)
                }

                else -> null
            }

            event?.let { _events.send(it) }
        }
    }

    private fun refreshCalendar() {
        viewModelScope.launch {
            _uiState.emit(createNewUiState(loadingMonths = true))

            loadEventsForMonth(selectedDay, refresh = true)

            awaitAll(
                async { loadEventsForMonth(selectedDay.plusMonths(1)) },
                async { loadEventsForMonth(selectedDay.minusMonths(1)) }
            )

            _uiState.emit(createNewUiState(loadingMonths = false))
        }
    }

    private fun refreshDay(date: LocalDate) {
        val startDate = date.atStartOfDay()
        val endDate = date.plusDays(1).atStartOfDay()

        viewModelScope.tryLaunch {
            refreshingDays.add(date)
            _uiState.emit(createNewUiState())

            val result = calendarRepository.getPlannerItems(
                startDate.toApiString() ?: "",
                endDate.toApiString() ?: "",
                contextIdFilters.toList(),
                true
            )

            refreshingDays.remove(date)

            storeResults(result, date)
            _uiState.emit(createNewUiState())
        } catch {
            refreshingDays.remove(date)
            viewModelScope.launch {
                _uiState.emit(createNewUiState().copy(snackbarMessage = context.getString(R.string.calendarRefreshFailed)))
            }
        }
    }

    private fun storeResults(result: List<PlannerItem>, dateToClear: LocalDate? = null) {
        eventsByDay.getOrDefault(dateToClear, null)?.clear()
        result.forEach { plannerItem ->
            val plannableDate = plannerItem.plannableDate.toLocalDate()
            val plannerItemsForDay = eventsByDay.getOrPut(plannableDate) { mutableListOf() }
            val index =
                plannerItemsForDay.indexOfFirst { it.plannable.id == plannerItem.plannable.id }
            if (index == -1) {
                plannerItemsForDay.add(plannerItem)
            } else {
                plannerItemsForDay[index] = plannerItem
            }
        }
    }

    private fun heightAnimationFinished() {
        if (collapsing) {
            collapsing = false
            expanded = false
            calendarPrefs.calendarExpanded = false
            viewModelScope.launch {
                _uiState.emit(createNewUiState())
            }
        }
    }

    private fun showFilters() {
        viewModelScope.launch {
            _events.send(CalendarViewModelAction.OpenFilters)
        }
    }
}