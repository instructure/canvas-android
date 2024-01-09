package com.instructure.pandautils.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {

    private val _selectedDay = MutableStateFlow(LocalDate.now())
    val selectedDay = _selectedDay.asStateFlow()

    private val _calendarData = MutableStateFlow(CalendarData(
        createMonthDataForLocalDate(LocalDate.now().minusMonths(1), true),
        createMonthDataForLocalDate(LocalDate.now(), true),
        createMonthDataForLocalDate(LocalDate.now().plusMonths(1), true),
    ))
    val calendarData = _calendarData.asStateFlow() // TODO map this instead of emitting

    private val _expanded = MutableStateFlow(true)
    val expanded = _expanded.asStateFlow()

    init {

    }

    fun dayChanged(newDay: LocalDate) {
        viewModelScope.launch {
            _selectedDay.emit(newDay)
            val dateFieldToAdd = if (_expanded.value) ChronoUnit.MONTHS else ChronoUnit.WEEKS
            _calendarData.emit(
                CalendarData(
                    createMonthDataForLocalDate(newDay.minus(1, dateFieldToAdd), _expanded.value),
                    createMonthDataForLocalDate(newDay, _expanded.value),
                    createMonthDataForLocalDate(newDay.plus(1, dateFieldToAdd), _expanded.value),
                )
            )
        }
    }

    fun expandChanged(expanded: Boolean) {
        viewModelScope.launch {
            _expanded.emit(expanded)

            val dateFieldToAdd = if (expanded) ChronoUnit.MONTHS else ChronoUnit.WEEKS

            _calendarData.emit(
                CalendarData(
                    createMonthDataForLocalDate(_selectedDay.value.minus(1, dateFieldToAdd), expanded),
                    createMonthDataForLocalDate(_selectedDay.value, expanded),
                    createMonthDataForLocalDate(_selectedDay.value.plus(1, dateFieldToAdd), expanded),
                )
            )
        }
    }

    fun jumpToToday() {
        dayChanged(LocalDate.now())
    }

    private fun createMonthDataForLocalDate(date: LocalDate, fullMonth: Boolean): MonthData {
        val year = date.year
        val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) // TODO maybe we need to get the locale from other source if app locale is overwritten
        val daysInMonth = date.lengthOfMonth()
        val firstDayOfMonth = date.withDayOfMonth(1)
        val firstDayOfWeekIndex = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 6 for Saturday

        val calendarRows = mutableListOf<CalendarRow>()
        val currentWeek = mutableListOf<Day>()

        // Fill the previous month's days if the first day of the month is not Sunday
        if (firstDayOfWeekIndex > 0) {
            val previousMonth = date.minusMonths(1).month
            val previousMonthYear = date.minusMonths(1).year
            val previousMonthFirstVisibleDay = firstDayOfMonth.minusDays(firstDayOfWeekIndex.toLong())
            val previousMonthDays = previousMonthFirstVisibleDay.dayOfMonth
            for (day in (previousMonthDays)until previousMonthDays + firstDayOfWeekIndex) {
                currentWeek.add(Day(day, LocalDate.of(previousMonthYear, previousMonth, day), enabled = false))
            }
        }

        // Fill the current month's days
        for (day in 1..daysInMonth) {
            val dateForDay = LocalDate.of(year, date.month, day)
            val enabled = dateForDay.dayOfWeek != DayOfWeek.SUNDAY && dateForDay.dayOfWeek != DayOfWeek.SATURDAY
            currentWeek.add(Day(day, dateForDay, enabled))
            if (currentWeek.size == 7) {
                calendarRows.add(CalendarRow(currentWeek.toList()))
                currentWeek.clear()
            }
        }

        // Fill the next month's days if the last day of the month is not Saturday
        if (currentWeek.isNotEmpty()) {
            val nextMonth = date.plusMonths(1).month
            val nextMonthYear = date.plusMonths(1).year
            val daysToAdd = 7 - currentWeek.size
            for (day in 1..daysToAdd) {
                currentWeek.add(Day(day, LocalDate.of(nextMonthYear, nextMonth, day), enabled = false))
            }
            calendarRows.add(CalendarRow(currentWeek.toList()))
        }

        val finalCalendarRows = if (fullMonth) calendarRows else calendarRows.filter { it.days.any { day -> day.date == date } }

        return MonthData(year.toString(), month, finalCalendarRows)
    }
}