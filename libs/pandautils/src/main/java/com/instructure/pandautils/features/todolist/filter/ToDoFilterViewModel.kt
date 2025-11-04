/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.todolist.filter

import android.content.Context
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import com.instructure.pandautils.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val FILTER_PERSONAL_TODOS = "personal_todos"
private const val FILTER_CALENDAR_EVENTS = "calendar_events"
private const val FILTER_SHOW_COMPLETED = "show_completed"
private const val FILTER_FAVORITE_COURSES = "favorite_courses"

@HiltViewModel
class ToDoFilterViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val checkboxStates = mutableMapOf(
        FILTER_PERSONAL_TODOS to false,
        FILTER_CALENDAR_EVENTS to true,
        FILTER_SHOW_COMPLETED to false,
        FILTER_FAVORITE_COURSES to false
    )

    private val _uiState = MutableStateFlow(createInitialUiState())
    val uiState = _uiState.asStateFlow()

    init {
        createInitialUiState()
    }

    private fun createInitialUiState(): ToDoFilterUiState {
        return ToDoFilterUiState(
            checkboxItems = createCheckboxItems(),
            pastDateOptions = createPastDateOptions(),
            selectedPastOption = DateRangeSelection.ONE_WEEK,
            futureDateOptions = createFutureDateOptions(),
            selectedFutureOption = DateRangeSelection.ONE_WEEK,
            onPastDaysChanged = { handlePastDaysChanged(it) },
            onFutureDaysChanged = { handleFutureDaysChanged(it) },
            onDone = { handleDone() },
            onDismiss = { handleDismiss() }
        )
    }

    private fun createCheckboxItems(): List<FilterCheckboxItem> {
        return listOf(
            FilterCheckboxItem(
                titleRes = R.string.todoFilterShowPersonalToDos,
                checked = checkboxStates[FILTER_PERSONAL_TODOS] ?: false,
                onToggle = { handleCheckboxToggle(FILTER_PERSONAL_TODOS, it) }
            ),
            FilterCheckboxItem(
                titleRes = R.string.todoFilterShowCalendarEvents,
                checked = checkboxStates[FILTER_CALENDAR_EVENTS] ?: true,
                onToggle = { handleCheckboxToggle(FILTER_CALENDAR_EVENTS, it) }
            ),
            FilterCheckboxItem(
                titleRes = R.string.todoFilterShowCompleted,
                checked = checkboxStates[FILTER_SHOW_COMPLETED] ?: false,
                onToggle = { handleCheckboxToggle(FILTER_SHOW_COMPLETED, it) }
            ),
            FilterCheckboxItem(
                titleRes = R.string.todoFilterFavoriteCoursesOnly,
                checked = checkboxStates[FILTER_FAVORITE_COURSES] ?: false,
                onToggle = { handleCheckboxToggle(FILTER_FAVORITE_COURSES, it) }
            )
        )
    }

    private fun handleCheckboxToggle(id: String, checked: Boolean) {
        checkboxStates[id] = checked
        _uiState.update {
            it.copy(checkboxItems = createCheckboxItems())
        }
    }

    private fun handlePastDaysChanged(option: DateRangeSelection) {
        _uiState.update {
            it.copy(selectedPastOption = option)
        }
    }

    private fun handleFutureDaysChanged(option: DateRangeSelection) {
        _uiState.update {
            it.copy(selectedFutureOption = option)
        }
    }

    private fun handleDone() {

    }

    private fun handleDismiss() {
        // No need to apply changes when dismissing
    }

    // TODO maybe move these to some common place to be used on the ToDo screen and widget
    fun calculatePastDateRange(selection: DateRangeSelection): Date {
        val calendar = Calendar.getInstance().apply { time = Date() }

        val weeksToAdd = when (selection) {
            DateRangeSelection.TODAY -> return calendar.apply { setStartOfDay() }.time
            DateRangeSelection.THIS_WEEK -> 0
            DateRangeSelection.ONE_WEEK -> -1
            DateRangeSelection.TWO_WEEKS -> -2
            DateRangeSelection.THREE_WEEKS -> -3
            DateRangeSelection.FOUR_WEEKS -> -4
        }

        return calendar.apply {
            add(Calendar.WEEK_OF_YEAR, weeksToAdd)
            set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            setStartOfDay()
        }.time
    }

    fun calculateFutureDateRange(selection: DateRangeSelection): Date {
        val calendar = Calendar.getInstance().apply { time = Date() }

        val weeksToAdd = when (selection) {
            DateRangeSelection.TODAY -> return calendar.apply { setEndOfDay() }.time
            DateRangeSelection.THIS_WEEK -> 0
            DateRangeSelection.ONE_WEEK -> 1
            DateRangeSelection.TWO_WEEKS -> 2
            DateRangeSelection.THREE_WEEKS -> 3
            DateRangeSelection.FOUR_WEEKS -> 4
        }

        return calendar.apply {
            add(Calendar.WEEK_OF_YEAR, weeksToAdd)
            set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            add(Calendar.DAY_OF_YEAR, 6)
            setEndOfDay()
        }.time
    }

    private fun Calendar.setStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun Calendar.setEndOfDay() {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    private fun formatDateText(date: Date, isPast: Boolean): String {
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMM")
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        return if (isPast) {
            context.getString(R.string.todoFilterFromDate, formattedDate)
        } else {
            context.getString(R.string.todoFilterUntilDate, formattedDate)
        }
    }

    private fun createPastDateOptions(): List<DateRangeOption> {
        return DateRangeSelection.entries.reversed().map { selection ->
            val date = calculatePastDateRange(selection)
            DateRangeOption(
                selection = selection,
                labelText = context.getString(selection.pastLabelResId),
                dateText = formatDateText(date, isPast = true)
            )
        }
    }

    private fun createFutureDateOptions(): List<DateRangeOption> {
        return DateRangeSelection.entries.map { selection ->
            val date = calculateFutureDateRange(selection)
            DateRangeOption(
                selection = selection,
                labelText = context.getString(selection.futureLabelResId),
                dateText = formatDateText(date, isPast = false)
            )
        }
    }
}