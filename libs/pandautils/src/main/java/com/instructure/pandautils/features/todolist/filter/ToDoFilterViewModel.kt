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
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val FILTER_PERSONAL_TODOS = "personal_todos"
private const val FILTER_CALENDAR_EVENTS = "calendar_events"
private const val FILTER_SHOW_COMPLETED = "show_completed"
private const val FILTER_FAVORITE_COURSES = "favorite_courses"

@HiltViewModel
class ToDoFilterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val toDoFilterDao: ToDoFilterDao
) : ViewModel() {

    private val checkboxStates = mutableMapOf(
        FILTER_PERSONAL_TODOS to false,
        FILTER_CALENDAR_EVENTS to false,
        FILTER_SHOW_COMPLETED to false,
        FILTER_FAVORITE_COURSES to false
    )

    private var selectedPastOption = DateRangeSelection.FOUR_WEEKS
    private var selectedFutureOption = DateRangeSelection.THIS_WEEK

    private val _uiState = MutableStateFlow(createInitialUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFiltersFromDatabase()
    }

    private fun loadFiltersFromDatabase() {
        viewModelScope.launch {
            val savedFilters = toDoFilterDao.findByUser(
                apiPrefs.fullDomain,
                apiPrefs.user?.id.orDefault()
            )

            if (savedFilters != null) {
                checkboxStates[FILTER_PERSONAL_TODOS] = savedFilters.personalTodos
                checkboxStates[FILTER_CALENDAR_EVENTS] = savedFilters.calendarEvents
                checkboxStates[FILTER_SHOW_COMPLETED] = savedFilters.showCompleted
                checkboxStates[FILTER_FAVORITE_COURSES] = savedFilters.favoriteCourses
                selectedPastOption = savedFilters.pastDateRange
                selectedFutureOption = savedFilters.futureDateRange
            }

            _uiState.update { createInitialUiState() }
        }
    }

    private fun createInitialUiState(): ToDoFilterUiState {
        return ToDoFilterUiState(
            checkboxItems = createCheckboxItems(),
            pastDateOptions = createPastDateOptions(),
            selectedPastOption = selectedPastOption,
            futureDateOptions = createFutureDateOptions(),
            selectedFutureOption = selectedFutureOption,
            onPastDaysChanged = { handlePastDaysChanged(it) },
            onFutureDaysChanged = { handleFutureDaysChanged(it) },
            onDone = { handleDone() },
            onFiltersApplied = { handleFiltersApplied() }
        )
    }

    private fun handleFiltersApplied() {
        _uiState.update {
            it.copy(shouldCloseAndApplyFilters = false, areDateFiltersChanged = false)
        }
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
        selectedPastOption = option
        _uiState.update {
            it.copy(selectedPastOption = option)
        }
    }

    private fun handleFutureDaysChanged(option: DateRangeSelection) {
        selectedFutureOption = option
        _uiState.update {
            it.copy(selectedFutureOption = option)
        }
    }

    private fun handleDone() {
        viewModelScope.launch {
            val savedFilters = toDoFilterDao.findByUser(
                apiPrefs.fullDomain,
                apiPrefs.user?.id.orDefault()
            )

            val areDateFiltersChanged = savedFilters?.let {
                it.pastDateRange != selectedPastOption || it.futureDateRange != selectedFutureOption
            } ?: true

            val filterEntity = ToDoFilterEntity(
                id = savedFilters?.id ?: 0,
                userDomain = apiPrefs.fullDomain,
                userId = apiPrefs.user?.id.orDefault(),
                personalTodos = checkboxStates[FILTER_PERSONAL_TODOS] ?: false,
                calendarEvents = checkboxStates[FILTER_CALENDAR_EVENTS] ?: false,
                showCompleted = checkboxStates[FILTER_SHOW_COMPLETED] ?: false,
                favoriteCourses = checkboxStates[FILTER_FAVORITE_COURSES] ?: false,
                pastDateRange = selectedPastOption,
                futureDateRange = selectedFutureOption
            )
            toDoFilterDao.insertOrUpdate(filterEntity)

            _uiState.update {
                it.copy(
                    shouldCloseAndApplyFilters = true,
                    areDateFiltersChanged = areDateFiltersChanged
                )
            }
        }
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
            val date = selection.calculatePastDateRange()
            DateRangeOption(
                selection = selection,
                labelText = context.getString(selection.pastLabelResId),
                dateText = formatDateText(date, isPast = true)
            )
        }
    }

    private fun createFutureDateOptions(): List<DateRangeOption> {
        return DateRangeSelection.entries.map { selection ->
            val date = selection.calculateFutureDateRange()
            DateRangeOption(
                selection = selection,
                labelText = context.getString(selection.futureLabelResId),
                dateText = formatDateText(date, isPast = false)
            )
        }
    }
}