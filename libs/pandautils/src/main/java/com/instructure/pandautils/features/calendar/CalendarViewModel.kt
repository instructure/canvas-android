package com.instructure.pandautils.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(createUiState())
    val uiState = _uiState.asStateFlow()

    private var selectedDay = LocalDate.now()
    private var expanded = true

    private fun createUiState(): CalendarUiState {
        return CalendarUiState(selectedDay, expanded)
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
        }
    }

    private fun selectedDayChanged(newDay: LocalDate) {
        selectedDay = newDay
        viewModelScope.launch {
            _uiState.emit(createUiState())
        }
    }

    private fun expandChanged(expanded: Boolean) {
        this.expanded = expanded
        viewModelScope.launch {
            _uiState.emit(createUiState())
        }
    }
}