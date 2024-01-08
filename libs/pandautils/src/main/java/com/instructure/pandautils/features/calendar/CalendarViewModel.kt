package com.instructure.pandautils.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _currentDay = MutableStateFlow(LocalDate.now())
    val currentDay = _currentDay.asStateFlow()

    init {

    }

    fun dayChanged(newDay: LocalDate) {
        viewModelScope.launch {
            _currentDay.emit(newDay)
        }
    }
}