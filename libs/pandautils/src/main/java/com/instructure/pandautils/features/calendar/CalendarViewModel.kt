package com.instructure.pandautils.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private var selectedDay = LocalDate.now()
    private var expanded = true

    private val eventsByDay = mutableMapOf<String, MutableList<PlannerItem>>()
    private val loadingDays = mutableSetOf<LocalDate>()
    private val loadedMonths = mutableSetOf<YearMonth>()

    private val _uiState = MutableStateFlow(CalendarUiState(selectedDay, expanded))
    val uiState = _uiState.asStateFlow()

    init {
        loadEventsForMonth(selectedDay)
        loadEventsForMonth(selectedDay.plusMonths(1))
        loadEventsForMonth(selectedDay.minusMonths(1))
    }

    private fun createDateKey(date: LocalDate): String {
        return "${date.year}-${date.monthValue}-${date.dayOfMonth}"
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
                val key = createDateKey(plannableDate)
                val plannerItemsForDay = eventsByDay.getOrPut(key) { mutableListOf() }
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

        return _uiState.value.copy(
            selectedDay = selectedDay,
            expanded = expanded,
            calendarEventsUiState = CalendarEventsUiState(
                previousPage = previousPage,
                currentPage = currentPage,
                nextPage = nextPage
            )
        )
    }

    private fun createEventsPageForDate(date: LocalDate): CalendarEventsPageUiState {
        val eventUiStates = eventsByDay[createDateKey(date)]?.map {
            EventUiState(
                contextName = it.contextName ?: "",
                canvasContext = it.canvasContext,
                name = it.plannable.title
            )
        } ?: emptyList()

        return CalendarEventsPageUiState(
            date = date,
            loading = loadingDays.contains(date),
            events = eventUiStates
        )
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