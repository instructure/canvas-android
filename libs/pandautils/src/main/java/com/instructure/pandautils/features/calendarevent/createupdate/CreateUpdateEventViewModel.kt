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
import com.google.ical.values.DateTimeValueImpl
import com.google.ical.values.Frequency
import com.google.ical.values.RRule
import com.google.ical.values.Weekday
import com.google.ical.values.WeekdayNum
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventFragment.Companion.INITIAL_DATE
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.pandautils.utils.toLocalTime
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
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CreateUpdateEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val repository: CreateUpdateEventRepository,
    private val apiPrefs: ApiPrefs,
    private val createUpdateEventViewModelBehavior: CreateUpdateEventViewModelBehavior
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUpdateEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CreateUpdateEventViewModelAction>()
    val events = _events.receiveAsFlow()

    private val initialDate = savedStateHandle.get<String>(INITIAL_DATE).toSimpleDate()?.toLocalDate() ?: LocalDate.now()
    private val scheduleItem: ScheduleItem? = savedStateHandle.get<ScheduleItem>(CreateUpdateEventFragment.SCHEDULE_ITEM)

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
                _uiState.update { it.copy(date = action.date, selectFrequencyUiState = getFrequencyUiState(action.date)) }
            }

            is CreateUpdateEventAction.UpdateStartTime -> {
                _uiState.update {
                    if (it.endTime == null || action.time <= it.endTime) {
                        it.copy(startTime = action.time)
                    } else {
                        it.copy(errorSnack = resources.getString(R.string.eventStartTimeError))
                    }
                }
            }

            is CreateUpdateEventAction.UpdateEndTime -> {
                _uiState.update {
                    if (it.startTime == null || action.time >= it.startTime) {
                        it.copy(endTime = action.time)
                    } else {
                        it.copy(errorSnack = resources.getString(R.string.eventEndTimeError))
                    }
                }
            }

            is CreateUpdateEventAction.UpdateFrequency -> {
                _uiState.update {
                    val frequencyUiState = it.selectFrequencyUiState.copy(selectedFrequency = action.frequency)
                    it.copy(selectFrequencyUiState = frequencyUiState)
                }
            }

            is CreateUpdateEventAction.UpdateCanvasContext -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectContextUiState.copy(selectedCanvasContext = action.canvasContext)
                    it.copy(selectContextUiState = selectCalendarUiState)
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

            is CreateUpdateEventAction.Save -> save(action.modifyEventScope, action.isSeriesEvent)

            is CreateUpdateEventAction.SnackbarDismissed -> {
                _uiState.update { it.copy(errorSnack = null) }
            }

            is CreateUpdateEventAction.ShowSelectCalendarScreen -> {
                _uiState.update {
                    val selectCalendarUiState = it.selectContextUiState.copy(show = true)
                    it.copy(selectContextUiState = selectCalendarUiState)
                }
            }

            is CreateUpdateEventAction.HideSelectCalendarScreen -> hideSelectCalendarScreen()

            is CreateUpdateEventAction.CheckUnsavedChanges -> checkUnsavedChanges()

            is CreateUpdateEventAction.HideUnsavedChangesDialog -> {
                _uiState.update { it.copy(showUnsavedChangesDialog = false) }
            }

            is CreateUpdateEventAction.NavigateBack -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(canNavigateBack = true) }
                    _events.send(CreateUpdateEventViewModelAction.NavigateBack)
                }
            }

            is CreateUpdateEventAction.ShowFrequencyDialog -> {
                _uiState.update {
                    val frequencyUiState = it.selectFrequencyUiState.copy(showFrequencyDialog = true)
                    it.copy(selectFrequencyUiState = frequencyUiState)
                }
            }

            is CreateUpdateEventAction.HideFrequencyDialog -> {
                _uiState.update {
                    val frequencyUiState = it.selectFrequencyUiState.copy(showFrequencyDialog = false)
                    it.copy(selectFrequencyUiState = frequencyUiState)
                }
            }

            is CreateUpdateEventAction.ShowCustomFrequencyScreen -> {
                _uiState.update {
                    val customFrequencyUiState = getCustomFrequencyUiState()
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.HideCustomFrequencyScreen -> hideCustomFrequencyScreen()

            is CreateUpdateEventAction.UpdateCustomFrequencyQuantity -> {
                _uiState.update {
                    val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(
                        quantity = action.quantity,
                        timeUnits = getTimeUnits(action.quantity.coerceAtLeast(1))
                    )
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.UpdateCustomFrequencySelectedTimeUnitIndex -> {
                _uiState.update {
                    val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(
                        selectedTimeUnitIndex = action.index,
                        daySelectorVisible = action.index == 1,
                        repeatsOnVisible = action.index == 2
                    )
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.UpdateCustomFrequencySelectedDays -> {
                _uiState.update {
                    val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(selectedDays = action.days)
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.UpdateCustomFrequencySelectedRepeatsOnIndex -> {
                _uiState.update {
                    val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(selectedRepeatsOnIndex = action.index)
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.UpdateCustomFrequencyEndDate -> {
                _uiState.update {
                    val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(
                        selectedDate = action.date,
                        selectedOccurrences = 0
                    )
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.UpdateCustomFrequencyOccurrences -> {
                _uiState.update {
                    val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(
                        selectedOccurrences = action.occurrences,
                        selectedDate = null
                    )
                    it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
                }
            }

            is CreateUpdateEventAction.SaveCustomFrequency -> {
                val customFrequencyRRule = getCustomFrequencyRRule()

                val selectedFrequency = uiState.value.selectFrequencyUiState.frequencies.entries.find {
                    it.value?.toIcal() == customFrequencyRRule.toIcal()
                }?.key
                val frequencyUiState = if (selectedFrequency != null) {
                    uiState.value.selectFrequencyUiState.copy(selectedFrequency = selectedFrequency)
                } else {
                    uiState.value.selectFrequencyUiState.copy(
                        selectedFrequency = resources.getString(R.string.eventFrequencyCustom),
                        frequencies = uiState.value.selectFrequencyUiState.frequencies.toMutableMap().apply {
                            put(resources.getString(R.string.eventFrequencyCustom), customFrequencyRRule)
                        }
                    )
                }
                _uiState.update { it.copy(selectFrequencyUiState = frequencyUiState) }
            }
        }
    }

    fun onBackPressed(): Boolean {
        return if (uiState.value.selectContextUiState.show) {
            hideSelectCalendarScreen()
            true
        } else if (uiState.value.selectFrequencyUiState.customFrequencyUiState.show) {
            hideCustomFrequencyScreen()
            true
        } else if (!uiState.value.canNavigateBack) {
            checkUnsavedChanges()
            true
        } else {
            false
        }
    }

    private fun hideSelectCalendarScreen() {
        _uiState.update {
            val selectCalendarUiState = it.selectContextUiState.copy(show = false)
            it.copy(selectContextUiState = selectCalendarUiState)
        }
    }

    private fun hideCustomFrequencyScreen() {
        _uiState.update {
            val customFrequencyUiState = it.selectFrequencyUiState.customFrequencyUiState.copy(show = false)
            it.copy(selectFrequencyUiState = it.selectFrequencyUiState.copy(customFrequencyUiState = customFrequencyUiState))
        }
    }

    private fun checkUnsavedChanges() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            handleAction(CreateUpdateEventAction.NavigateBack)
        }
    }

    private fun setInitialState() {
        scheduleItem?.let {
            _uiState.update { state ->
                state.copy(
                    title = it.title.orEmpty(),
                    date = it.startDate?.toLocalDate() ?: state.date,
                    startTime = it.startDate?.toLocalTime(),
                    endTime = it.endDate?.toLocalTime(),
                    selectFrequencyUiState = getFrequencyUiState(it.startDate?.toLocalDate() ?: state.date),
                    location = it.locationName.orEmpty(),
                    address = it.locationAddress.orEmpty(),
                    details = it.description.orEmpty(),
                    isSeriesEvent = it.isRecurring,
                    isSeriesHead = it.seriesHead
                )
            }
        } ?: run {
            _uiState.update {
                it.copy(
                    date = initialDate,
                    selectFrequencyUiState = getFrequencyUiState(initialDate)
                )
            }
        }
    }

    private fun loadCanvasContexts() {
        _uiState.update { it.copy(loadingCanvasContexts = true) }
        viewModelScope.tryLaunch {
            val canvasContexts = repository.getCanvasContexts()
            _uiState.update {
                it.copy(
                    loadingCanvasContexts = false,
                    selectContextUiState = it.selectContextUiState.copy(
                        canvasContexts = canvasContexts,
                        selectedCanvasContext = canvasContexts.firstOrNull { canvasContext ->
                            canvasContext.id == scheduleItem?.contextId
                        } ?: apiPrefs.user
                    )
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    loadingCanvasContexts = false,
                    selectContextUiState = it.selectContextUiState.copy(
                        canvasContexts = emptyList(),
                        selectedCanvasContext = null
                    )
                )
            }
        }
    }

    private fun getFrequencyUiState(date: LocalDate): SelectFrequencyUiState {
        val dayOfWeek = date.dayOfWeek
        val dayOfMonthOrdinal = (date.dayOfMonth - 1) / 7 + 1
        val isLastDayOfWeekInMonth = date.with(TemporalAdjusters.lastInMonth(dayOfWeek)) == date
        val dayOfWeekText = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val dateText = date.format(DateTimeFormatter.ofPattern(DateHelper.dayMonthDateFormat.toPattern()))
        val ordinal = when {
            isLastDayOfWeekInMonth -> resources.getString(R.string.eventFrequencyMonthlyLast)
            else -> getWeekDayInMonthOrdinal(dayOfMonthOrdinal)
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

        val frequencies = mutableMapOf(
            resources.getString(R.string.eventFrequencyDoesNotRepeat) to null,
            resources.getString(R.string.eventFrequencyDaily) to daily,
            resources.getString(R.string.eventFrequencyWeekly, dayOfWeekText) to weekly,
            resources.getString(R.string.eventFrequencyMonthly, ordinal, dayOfWeekText) to monthly,
            resources.getString(R.string.eventFrequencyAnnually, dateText) to yearly,
            resources.getString(R.string.eventFrequencyWeekdays) to weekdays
        )

        var selectedFrequency = resources.getString(R.string.eventFrequencyDoesNotRepeat)

        scheduleItem?.let { scheduleItem ->
            if (!scheduleItem.rrule.isNullOrEmpty()) {
                frequencies.entries.find {
                    it.value?.toApiString() == scheduleItem.rrule
                }?.let {
                    selectedFrequency = it.key
                } ?: run {
                    val frequencyText = scheduleItem.seriesNaturalLanguage.orEmpty()
                    selectedFrequency = frequencyText
                    frequencies[frequencyText] = scheduleItem.getRRule()
                }
            }
        }

        frequencies[resources.getString(R.string.eventFrequencyCustom)] = null

        return SelectFrequencyUiState(
            selectedFrequency = selectedFrequency,
            frequencies = frequencies
        )
    }

    private fun getWeekDayInMonthOrdinal(dayOfMonthOrdinal: Int) = when (dayOfMonthOrdinal) {
        1 -> resources.getString(R.string.eventFrequencyMonthlyFirst)
        2 -> resources.getString(R.string.eventFrequencyMonthlySecond)
        3 -> resources.getString(R.string.eventFrequencyMonthlyThird)
        4 -> resources.getString(R.string.eventFrequencyMonthlyFourth)
        else -> resources.getString(R.string.eventFrequencyMonthlyLast)
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

    private fun save(modifyEventScope: CalendarEventAPI.ModifyEventScope, isSeriesEvent: Boolean) = with(uiState.value) {
        _uiState.update { it.copy(saving = true) }
        viewModelScope.tryLaunch {
            val startDate = LocalDateTime.of(date, startTime ?: LocalTime.of(6, 0)).toApiString().orEmpty()
            val endDate = LocalDateTime.of(date, endTime ?: LocalTime.of(6, 0)).toApiString().orEmpty()
            val rrule = selectFrequencyUiState.frequencies[selectFrequencyUiState.selectedFrequency]?.toApiString().orEmpty()
            val contextCode = selectContextUiState.selectedCanvasContext?.contextId.orEmpty()

            val updated = scheduleItem != null
            val result = scheduleItem?.let {
                repository.updateEvent(
                    eventId = it.id,
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    rrule = rrule,
                    contextCode = contextCode,
                    locationName = location,
                    locationAddress = address,
                    description = details,
                    modifyEventScope = modifyEventScope,
                    isSeriesEvent = isSeriesEvent
                ).also {
                    CanvasRestAdapter.clearCacheUrls("calendar_events/")
                }
            } ?: run {
                repository.createEvent(
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    rrule = rrule,
                    contextCode = contextCode,
                    locationName = location,
                    locationAddress = address,
                    description = details
                )
            }

            _uiState.update { it.copy(saving = false, canNavigateBack = true) }

            val announceEvent = if (updated) {
                CreateUpdateEventViewModelAction.AnnounceEventUpdate(result.first().title.orEmpty())
            } else {
                CreateUpdateEventViewModelAction.AnnounceEventCreation(result.first().title.orEmpty())
            }
            _events.send(announceEvent)

            if (rrule.isNotEmpty() || !scheduleItem?.rrule.isNullOrEmpty()) {
                _events.send(CreateUpdateEventViewModelAction.RefreshCalendar)
            } else {
                val daysToRefresh = listOfNotNull(scheduleItem?.startDate?.toLocalDate()) + result.mapNotNull { it.startDate?.toLocalDate() }
                _events.send(CreateUpdateEventViewModelAction.RefreshCalendarDays(daysToRefresh))
            }

            createUpdateEventViewModelBehavior.updateWidget()
        } catch {
            _uiState.update {
                it.copy(
                    saving = false,
                    errorSnack = resources.getString(R.string.eventSaveErrorMessage)
                )
            }
        }
    }

    private fun hasUnsavedChanges(): Boolean = with(uiState.value) {
        return scheduleItem?.let {
            title != it.title.orEmpty() ||
                    date != it.startDate?.toLocalDate() ||
                    startTime != it.startDate?.toLocalTime() ||
                    endTime != it.endDate?.toLocalTime() ||
                    selectFrequencyUiState.frequencies[selectFrequencyUiState.selectedFrequency]?.toApiString() != it.getRRule()?.toApiString() ||
                    selectContextUiState.selectedCanvasContext?.contextId != it.contextCode ||
                    location != it.locationName.orEmpty() ||
                    address != it.locationAddress.orEmpty() ||
                    details != it.description.orEmpty()
        } ?: run {
            title.isNotEmpty() ||
                    date != initialDate ||
                    startTime != null ||
                    endTime != null ||
                    selectFrequencyUiState.selectedFrequency != selectFrequencyUiState.frequencies.keys.first() ||
                    selectContextUiState.selectedCanvasContext != apiPrefs.user ||
                    location.isNotEmpty() ||
                    address.isNotEmpty() ||
                    details.isNotEmpty()
        }
    }

    private fun getCustomFrequencyUiState(): CustomFrequencyUiState {
        val selectedRRule = uiState.value.selectFrequencyUiState.frequencies[uiState.value.selectFrequencyUiState.selectedFrequency]
        val timeUnitIndex = when (selectedRRule?.freq) {
            Frequency.WEEKLY -> 1
            Frequency.MONTHLY -> 2
            Frequency.YEARLY -> 3
            else -> 0
        }
        val daysOfWeek = DayOfWeek.entries.toTypedArray()

        val shiftedDaysOfWeekStartingSunday = Array(7) { daysOfWeek[(it + 6) % 7] }.toList()
        val selectedDays = selectedRRule?.byDay?.map { shiftedDaysOfWeekStartingSunday[it.wday.ordinal] }?.toSet().orEmpty()

        val dayOfMonthOrdinal = selectedRRule?.bySetPos?.firstOrNull()?.let {
            getWeekDayInMonthOrdinal(it)
        } ?: run {
            val date = uiState.value.date
            val dayOfWeek = date.dayOfWeek
            val dayOfMonthOrdinal = (date.dayOfMonth - 1) / 7 + 1
            val isLastDayOfWeekInMonth = date.with(TemporalAdjusters.lastInMonth(dayOfWeek)) == date
            when {
                isLastDayOfWeekInMonth -> resources.getString(R.string.eventFrequencyMonthlyLast)
                else -> getWeekDayInMonthOrdinal(dayOfMonthOrdinal)
            }
        }
        val weekDayOfMonth = selectedRRule?.byDay?.firstOrNull()?.wday?.let {
            shiftedDaysOfWeekStartingSunday[it.ordinal]
        } ?: run {
            uiState.value.date.dayOfWeek
        }
        val defaultDayOfMonth = uiState.value.date.dayOfMonth
        val repeatsOn = listOf(
            resources.getString(R.string.eventCustomFrequencyScreenOnDay, selectedRRule?.byMonthDay?.firstOrNull().orDefault(defaultDayOfMonth)),
            resources.getString(
                R.string.eventCustomFrequencyScreenOnWeekday,
                dayOfMonthOrdinal,
                weekDayOfMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())
            )
        )
        val endsOn = selectedRRule?.until?.let { LocalDate.of(it.year(), it.month(), it.day()) }

        val localeFirstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
        // Shift the starting point to the correct day
        val shiftAmount = localeFirstDayOfWeek - daysOfWeek.first().value
        val shiftedDaysOfWeekLocalized = Array(7) { daysOfWeek[(it + shiftAmount) % 7] }.toList()

        return CustomFrequencyUiState(
            show = true,
            quantity = selectedRRule?.interval.orDefault(),
            timeUnits = getTimeUnits(selectedRRule?.interval.orDefault(1)),
            selectedTimeUnitIndex = timeUnitIndex,
            daySelectorVisible = selectedRRule?.freq == Frequency.WEEKLY,
            days = shiftedDaysOfWeekLocalized,
            selectedDays = selectedDays,
            repeatsOnVisible = selectedRRule?.freq == Frequency.MONTHLY,
            repeatsOn = repeatsOn,
            selectedRepeatsOnIndex = if (selectedRRule?.byMonthDay?.isNotEmpty().orDefault()) 0 else 1,
            selectedDate = endsOn,
            selectedOccurrences = selectedRRule?.count.orDefault()
        )
    }

    private fun getTimeUnits(quantity: Int): List<String> {
        return listOf(
            resources.getQuantityString(R.plurals.eventCustomFrequencyScreenDay, quantity),
            resources.getQuantityString(R.plurals.eventCustomFrequencyScreenWeek, quantity),
            resources.getQuantityString(R.plurals.eventCustomFrequencyScreenMonth, quantity),
            resources.getQuantityString(R.plurals.eventCustomFrequencyScreenYear, quantity),
        )
    }

    private fun getCustomFrequencyRRule(): RRule {
        val customFrequencyUiState = uiState.value.selectFrequencyUiState.customFrequencyUiState
        return RRule().apply {
            interval = customFrequencyUiState.quantity
            when (customFrequencyUiState.selectedTimeUnitIndex) {
                0 -> {
                    freq = Frequency.DAILY
                }

                1 -> {
                    freq = Frequency.WEEKLY
                    byDay = customFrequencyUiState.selectedDays.map { WeekdayNum(0, weekDayFromDayOfWeek(it)) }
                }

                2 -> {
                    freq = Frequency.MONTHLY
                    if (customFrequencyUiState.selectedRepeatsOnIndex == 0) {
                        byMonthDay = intArrayOf(uiState.value.date.dayOfMonth)
                    } else {
                        val date = uiState.value.date
                        byDay = listOf(WeekdayNum(0, weekDayFromDayOfWeek(date.dayOfWeek)))
                        val isLastDayOfWeekInMonth = date.with(TemporalAdjusters.lastInMonth(date.dayOfWeek)) == date
                        bySetPos = intArrayOf(if (isLastDayOfWeekInMonth) -1 else (uiState.value.date.dayOfMonth - 1) / 7 + 1)
                    }
                }

                3 -> {
                    freq = Frequency.YEARLY
                    byMonth = intArrayOf(uiState.value.date.monthValue)
                    byMonthDay = intArrayOf(uiState.value.date.dayOfMonth)
                }
            }
            if (customFrequencyUiState.selectedDate != null) {
                val date = customFrequencyUiState.selectedDate
                until = DateTimeValueImpl(date.year, date.monthValue, date.dayOfMonth, 0, 0, 0)
            } else {
                count = customFrequencyUiState.selectedOccurrences
            }
        }
    }
}

private fun RRule.toApiString(): String {
    // Drop the "RRULE:" prefix
    return toIcal().drop(6)
}

private fun ScheduleItem.getRRule(): RRule? {
    return try {
        RRule("RRULE:$rrule")
    } catch (e: Exception) {
        null
    }
}