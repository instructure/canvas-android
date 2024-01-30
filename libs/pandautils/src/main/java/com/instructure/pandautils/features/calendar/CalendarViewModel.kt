package com.instructure.pandautils.features.calendar

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class CalendarViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private var selectedDay = LocalDate.now()
    private var expanded = true

    private val eventsByDay = mutableMapOf<LocalDate, MutableList<PlannerItem>>()
    private val loadingDays = mutableSetOf<LocalDate>()
    private val loadedMonths = mutableSetOf<YearMonth>()

    private val _uiState = MutableStateFlow(CalendarUiState(selectedDay, expanded, eventIndicators = emptyMap()))
    val uiState = _uiState.asStateFlow()

    init {
        loadEventsForMonth(selectedDay)
        loadEventsForMonth(selectedDay.plusMonths(1))
        loadEventsForMonth(selectedDay.minusMonths(1))
    }

    private fun loadEventsForMonth(date: LocalDate) {
        val yearMonth = YearMonth.from(date)
        if (loadedMonths.contains(yearMonth)) return

        loadedMonths.add(yearMonth) // We add it here because we don't want to reload even when it's loading

        val startDate = date.withDayOfMonth(1).atStartOfDay()
        val endDate = date.plusMonths(1).withDayOfMonth(1).atStartOfDay()

        viewModelScope.tryLaunch {
            loadingDays.addAll(daysBetweenDates(startDate, endDate))
            _uiState.emit(createNewUiState())

            val result = calendarRepository.getPlannerItems(
                startDate.toApiString() ?: "",
                endDate.toApiString() ?: "",
                emptyList(),
                true
            )

            loadingDays.removeAll(daysBetweenDates(startDate, endDate))

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

            _uiState.emit(createNewUiState())
        } catch {
            loadedMonths.remove(yearMonth)
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

    private fun createNewUiState(): CalendarUiState {
        val currentPage = createEventsPageForDate(selectedDay)
        val previousPage = createEventsPageForDate(selectedDay.minusDays(1))
        val nextPage = createEventsPageForDate(selectedDay.plusDays(1))

        val eventIndicators = eventsByDay.mapValues { min(3, it.value.size) }

        return _uiState.value.copy(
            selectedDay = selectedDay,
            expanded = expanded,
            calendarEventsUiState = CalendarEventsUiState(
                previousPage = previousPage,
                currentPage = currentPage,
                nextPage = nextPage
            ),
            eventIndicators = eventIndicators
        )
    }

    private fun createEventsPageForDate(date: LocalDate): CalendarEventsPageUiState {
        val eventUiStates = eventsByDay[date]?.map {
            EventUiState(
                contextName = getContextNameForPlannerItem(it),
                canvasContext = it.canvasContext,
                iconRes = getIconForPlannerItem(it),
                name = it.plannable.title,
                date = getDateForPlannerItem(it),
                status = getStatusForPlannerItem(it)
            )
        } ?: emptyList()

        return CalendarEventsPageUiState(
            date = date,
            loading = loadingDays.contains(date),
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

    @DrawableRes
    private fun getIconForPlannerItem(plannerItem: PlannerItem): Int {
        return when (plannerItem.plannableType) {
            PlannableType.ASSIGNMENT -> R.drawable.ic_assignment
            PlannableType.QUIZ -> R.drawable.ic_quiz
            PlannableType.CALENDAR_EVENT -> R.drawable.ic_calendar
            PlannableType.DISCUSSION_TOPIC -> R.drawable.ic_discussion
            PlannableType.PLANNER_NOTE -> R.drawable.ic_todo
            else -> R.drawable.ic_calendar
        }
    }

    private fun getDateForPlannerItem(plannerItem: PlannerItem): String? {
        return if (plannerItem.plannableType == PlannableType.PLANNER_NOTE) {
            val date = plannerItem.plannable.todoDate.toDate()
            date?.let {
                DateHelper.getDateTimeString(context, it)
            }
        } else {
            plannerItem.plannable.dueAt?.let {
                context.getString(R.string.calendarDueDate, DateHelper.getDateTimeString(context, it))
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
            is CalendarAction.DaySelected -> selectedDayChanged(calendarAction.selectedDay)
            CalendarAction.ExpandChanged -> expandChanged(!expanded)
            CalendarAction.ExpandDisabled -> expandChanged(false)
            CalendarAction.TodayTapped -> selectedDayChanged(LocalDate.now())
            is CalendarAction.PageChanged -> {
                val dateFieldToAdd = if (expanded) ChronoUnit.MONTHS else ChronoUnit.WEEKS
                selectedDayChanged(selectedDay.plus(calendarAction.offset.toLong(), dateFieldToAdd))
            }

            is CalendarAction.EventPageChanged -> selectedDayChanged(
                selectedDay.plusDays(
                    calendarAction.offset.toLong()
                )
            )
        }
    }

    private fun selectedDayChanged(newDay: LocalDate) {
        selectedDay = newDay
        viewModelScope.launch {
            _uiState.emit(createNewUiState())
        }
        loadEventsForMonth(selectedDay)
        loadEventsForMonth(selectedDay.plusMonths(1))
        loadEventsForMonth(selectedDay.minusMonths(1))
    }

    private fun expandChanged(expanded: Boolean) {
        this.expanded = expanded
        viewModelScope.launch {
            _uiState.emit(createNewUiState())
        }
    }
}