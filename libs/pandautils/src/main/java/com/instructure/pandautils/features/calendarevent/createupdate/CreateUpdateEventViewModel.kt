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

package com.instructure.pandautils.features.calendarevent.createupdate

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ical.values.Frequency
import com.google.ical.values.RRule
import com.google.ical.values.Weekday
import com.google.ical.values.WeekdayNum
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment.Companion.INITIAL_DATE
import com.instructure.pandautils.utils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CreateUpdateEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val repository: CreateUpdateEventRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUpdateEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CreateUpdateEventViewModelAction>()
    val events = _events.receiveAsFlow()

    private val initialDate = savedStateHandle.get<String>(INITIAL_DATE).toSimpleDate()?.toLocalDate() ?: LocalDate.now()

    init {
        loadCanvasContexts()
        setInitialState()
    }

    fun handleAction(action: CreateUpdateEventAction) {
        when (action) {
            is CreateUpdateEventAction.UpdateTitle -> {
                _uiState.update { it.copy(title = action.title) }
            }

            is CreateUpdateEventAction.UpdateDate -> {
                _uiState.update { it.copy(date = action.date, frequencyDialogUiState = getFrequencyDialogUiState(action.date)) }
            }

            is CreateUpdateEventAction.UpdateStartTime -> {
                _uiState.update { it.copy(startTime = action.time) }
            }

            is CreateUpdateEventAction.UpdateEndTime -> {
                _uiState.update { it.copy(endTime = action.time) }
            }

            is CreateUpdateEventAction.UpdateFrequency -> {
                _uiState.update {
                    val frequencyDialogUiState = it.frequencyDialogUiState.copy(selectedFrequency = action.frequency)
                    it.copy(frequencyDialogUiState = frequencyDialogUiState)
                }
            }

            is CreateUpdateEventAction.UpdateCanvasContext -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectCalendarUiState.copy(selectedCanvasContext = action.canvasContext)
                    it.copy(selectCalendarUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.UpdateLocation -> {
                _uiState.update { it.copy(location = action.location) }
            }

            is CreateUpdateEventAction.UpdateAddress -> {
                _uiState.update { it.copy(address = action.address) }
            }

            is CreateUpdateEventAction.UpdateDetails -> {
                _uiState.update { it.copy(details = action.details) }
            }

            is CreateUpdateEventAction.Save -> saveEvent()

            is CreateUpdateEventAction.SnackbarDismissed -> {
                _uiState.update { it.copy(errorSnack = null) }
            }

            is CreateUpdateEventAction.ShowSelectCalendarScreen -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectCalendarUiState.copy(show = true)
                    it.copy(selectCalendarUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.HideSelectCalendarScreen -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectCalendarUiState.copy(show = false)
                    it.copy(selectCalendarUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.CheckUnsavedChanges -> {
                if (checkUnsavedChanges()) {
                    _uiState.update { it.copy(showUnsavedChangesDialog = true) }
                } else {
                    handleAction(CreateUpdateEventAction.NavigateBack)
                }
            }

            is CreateUpdateEventAction.HideUnsavedChangesDialog -> {
                _uiState.update { it.copy(showUnsavedChangesDialog = false) }
            }

            is CreateUpdateEventAction.NavigateBack -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(canNavigateBack = true) }
                    _events.send(CreateUpdateEventViewModelAction.NavigateBack)
                }
            }

            is CreateUpdateEventAction.CustomFrequencySelected -> {
                val selectedFrequency = uiState.value.frequencyDialogUiState.frequencies.entries.find {
                    it.value?.toIcal() == action.rrule.toIcal()
                }?.key
                val frequencyDialogUiState = if (selectedFrequency != null) {
                    uiState.value.frequencyDialogUiState.copy(selectedFrequency = selectedFrequency)
                } else {
                    uiState.value.frequencyDialogUiState.copy(
                        selectedFrequency = resources.getString(R.string.eventFrequencyCustom),
                        frequencies = uiState.value.frequencyDialogUiState.frequencies.toMutableMap().apply {
                            put(resources.getString(R.string.eventFrequencyCustom), action.rrule)
                        }
                    )
                }
                _uiState.update { it.copy(frequencyDialogUiState = frequencyDialogUiState) }
            }
        }
    }

    private fun setInitialState() {
        _uiState.update {
            it.copy(
                date = initialDate,
                frequencyDialogUiState = getFrequencyDialogUiState(initialDate)
            )
        }

        // TODO when editing
    }

    private fun loadCanvasContexts() {
        _uiState.update { it.copy(loadingCanvasContexts = true) }
        val userList = listOfNotNull(apiPrefs.user)

        //TODO Get canvas contexts for Teacher

        _uiState.update {
            it.copy(
                loadingCanvasContexts = false,
                selectCalendarUiState = it.selectCalendarUiState.copy(
                    canvasContexts = userList,
                    selectedCanvasContext = apiPrefs.user
                )
            )
        }
    }

    private fun getFrequencyDialogUiState(date: LocalDate): FrequencyDialogUiState {
        val dayOfWeek = date.dayOfWeek
        val dayOfMonthOrdinal = (date.dayOfMonth - 1) / 7 + 1
        val isLastDayOfWeekInMonth = date.with(TemporalAdjusters.lastInMonth(dayOfWeek)) == date
        val dayOfWeekText = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val dateText = date.format(DateTimeFormatter.ofPattern(DateHelper.dayMonthDateFormat.toPattern()))
        val ordinal = when {
            isLastDayOfWeekInMonth -> resources.getString(R.string.eventFrequencyMonthlyLast)
            else -> when (dayOfMonthOrdinal) {
                1 -> resources.getString(R.string.eventFrequencyMonthlyFirst)
                2 -> resources.getString(R.string.eventFrequencyMonthlySecond)
                3 -> resources.getString(R.string.eventFrequencyMonthlyThird)
                4 -> resources.getString(R.string.eventFrequencyMonthlyFourth)
                else -> resources.getString(R.string.eventFrequencyMonthlyLast)
            }
        }

        val daily = RRule().apply {
            freq = Frequency.DAILY
            interval = 1
            count = 365
        }

        val weekly = RRule().apply {
            freq = Frequency.WEEKLY
            interval = 1
            byDay = listOf(WeekdayNum(0, weekDayFromDayOfWeek(dayOfWeek)))
            count = 52
        }

        val monthly = RRule().apply {
            freq = Frequency.MONTHLY
            interval = 1
            byDay = listOf(WeekdayNum(0, weekDayFromDayOfWeek(dayOfWeek)))
            count = 12
            bySetPos = intArrayOf(if (isLastDayOfWeekInMonth) -1 else dayOfMonthOrdinal)
        }

        val yearly = RRule().apply {
            freq = Frequency.YEARLY
            interval = 1
            byMonth = intArrayOf(date.monthValue)
            byMonthDay = intArrayOf(date.dayOfMonth)
            count = 5
        }

        val weekdays = RRule().apply {
            freq = Frequency.WEEKLY
            interval = 1
            byDay = listOf(
                WeekdayNum(0, Weekday.MO),
                WeekdayNum(0, Weekday.TU),
                WeekdayNum(0, Weekday.WE),
                WeekdayNum(0, Weekday.TH),
                WeekdayNum(0, Weekday.FR)
            )
            count = 260
        }

        return FrequencyDialogUiState(
            selectedFrequency = resources.getString(R.string.eventFrequencyDoesNotRepeat),
            frequencies = mapOf(
                resources.getString(R.string.eventFrequencyDoesNotRepeat) to null,
                resources.getString(R.string.eventFrequencyDaily) to daily,
                resources.getString(R.string.eventFrequencyWeekly, dayOfWeekText) to weekly,
                resources.getString(R.string.eventFrequencyMonthly, ordinal, dayOfWeekText) to monthly,
                resources.getString(R.string.eventFrequencyAnnually, dateText) to yearly,
                resources.getString(R.string.eventFrequencyWeekdays) to weekdays,
                resources.getString(R.string.eventFrequencyCustom) to null
            )
        )
    }

    private fun weekDayFromDayOfWeek(dayOfWeek: DayOfWeek): Weekday {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> Weekday.MO
            DayOfWeek.TUESDAY -> Weekday.TU
            DayOfWeek.WEDNESDAY -> Weekday.WE
            DayOfWeek.THURSDAY -> Weekday.TH
            DayOfWeek.FRIDAY -> Weekday.FR
            DayOfWeek.SATURDAY -> Weekday.SA
            DayOfWeek.SUNDAY -> Weekday.SU
        }
    }

    private fun saveEvent() = with(uiState.value) {
        _uiState.update { it.copy(saving = true) }
        viewModelScope.tryLaunch {
            val startDate = startTime?.let { LocalDateTime.of(date, it).toApiString() } ?: date.toApiString()
            val endDate = endTime?.let { LocalDateTime.of(date, it).toApiString() } ?: date.toApiString()

            val result = repository.createEvent(
                title = title,
                startDate = startDate.orEmpty(),
                endDate = endDate.orEmpty(),
                // Drop the "RRULE:" prefix
                rrule = frequencyDialogUiState.frequencies[frequencyDialogUiState.selectedFrequency]?.toIcal()?.drop(6).orEmpty(),
                contextCode = selectCalendarUiState.selectedCanvasContext?.contextId.orEmpty(),
                locationName = location,
                locationAddress = address,
                description = details
            )

            _uiState.update { it.copy(saving = false) }
            _events.send(
                CreateUpdateEventViewModelAction.RefreshCalendarDays(
                    result.mapNotNull { it.startDate?.toLocalDate() }
                )
            )
        } catch {
            _uiState.update {
                it.copy(
                    saving = false,
                    errorSnack = resources.getString(R.string.eventSaveErrorMessage)
                )
            }
        }
    }

    private fun checkUnsavedChanges(): Boolean = with(uiState.value) {
        return title.isNotEmpty() ||
                date != initialDate ||
                startTime != null ||
                endTime != null ||
                frequencyDialogUiState.selectedFrequency != frequencyDialogUiState.frequencies.keys.first() ||
                selectCalendarUiState.selectedCanvasContext != apiPrefs.user ||
                location.isNotEmpty() ||
                address.isNotEmpty() ||
                details.isNotEmpty()
    }
}
