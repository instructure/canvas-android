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
package com.instructure.pandautils.features.calendar.filter

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.courseOrUserColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarFilterViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val resources: Resources
) : ViewModel() {

    private var canvasContexts = emptyMap<CanvasContext.Type, List<CanvasContext>>()
    private val contextIdFilters = mutableSetOf<String>()
    private val initialFilters = mutableSetOf<String>()
    private var filterEntityForCurrentUser: CalendarFilterEntity? = null
    private var filterLimit = -1

    private val _uiState = MutableStateFlow(CalendarFilterScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<CalendarFilterViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        loadFilters()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            _uiState.value = createNewUiState(loading = true)
            val result = calendarRepository.getCanvasContexts()
            if (result is DataResult.Success) {
                canvasContexts = result.data

                filterLimit = calendarRepository.getCalendarFilterLimit()
                val filters = calendarRepository.getCalendarFilters()
                if (filters != null) {
                    filterEntityForCurrentUser = filters
                    contextIdFilters.addAll(filters.filters)
                }

                _uiState.value = createNewUiState()
            } else {
                _uiState.value = createNewUiState(error = true)
            }
            initialFilters.addAll(contextIdFilters)
        }
    }

    private fun createNewUiState(error: Boolean = false, loading: Boolean = false, snackbarMessage: String? = null): CalendarFilterScreenUiState {
        val explanationMessage = if (filterLimit != -1) resources.getString(R.string.calendarFilterExplanationLimited, filterLimit) else null
        val selectAllAvailable = filterLimit == -1 && !loading // We don't want to show the button when the filters are loading.
        return CalendarFilterScreenUiState(
            createFilterItemsUiState(CanvasContext.Type.USER),
            createFilterItemsUiState(CanvasContext.Type.COURSE),
            createFilterItemsUiState(CanvasContext.Type.GROUP),
            error, loading, selectAllAvailable, explanationMessage, snackbarMessage
        )
    }

    private fun createFilterItemsUiState(type: CanvasContext.Type) = canvasContexts[type]?.map {
        val color = it.courseOrUserColor
        CalendarFilterItemUiState(it.contextId, it.name.orEmpty(), contextIdFilters.contains(it.contextId), color)
    } ?: emptyList()

    fun handleAction(calendarFilterAction: CalendarFilterAction) {
        when (calendarFilterAction) {
            is CalendarFilterAction.ToggleFilter -> toggleFilter(calendarFilterAction.contextId)
            CalendarFilterAction.Retry -> loadFilters()
            CalendarFilterAction.SnackbarDismissed -> _uiState.value = _uiState.value.copy(snackbarMessage = null)
            CalendarFilterAction.DeselectAll -> deselectAll()
            CalendarFilterAction.SelectAll -> selectAll()
        }
    }

    private fun toggleFilter(contextId: String) {
        var snackbarMessage: String? = null
        if (contextIdFilters.contains(contextId)) {
            contextIdFilters.remove(contextId)
        } else {
            if (contextIdFilters.size < filterLimit || filterLimit == -1) {
                contextIdFilters.add(contextId)
            } else {
                snackbarMessage = resources.getString(R.string.calendarFilterLimitSnackbar, filterLimit)
            }
        }
        viewModelScope.launch {
            filterEntityForCurrentUser?.let {
                val newFilter = it.copy(filters = contextIdFilters)
                calendarRepository.updateCalendarFilters(newFilter)
            }
            _uiState.emit(createNewUiState(snackbarMessage = snackbarMessage))
        }
    }

    fun filtersClosed() {
        viewModelScope.launch {
            _events.send(CalendarFilterViewModelAction.FiltersClosed(initialFilters != contextIdFilters))
        }
    }

    private fun deselectAll() {
        contextIdFilters.clear()
        viewModelScope.launch {
            filterEntityForCurrentUser?.let {
                val newFilter = it.copy(filters = contextIdFilters)
                calendarRepository.updateCalendarFilters(newFilter)
            }
            _uiState.emit(createNewUiState())
        }
    }

    private fun selectAll() {
        var snackbarMessage: String? = null
        canvasContexts.flatMap { it.value }.forEach {
            if (filterLimit == -1 || contextIdFilters.size < filterLimit) {
                contextIdFilters.add(it.contextId)
            } else {
                snackbarMessage = resources.getString(R.string.calendarFilterLimitSnackbar, filterLimit)
                return@forEach
            }
        }
        viewModelScope.launch {
            filterEntityForCurrentUser?.let {
                val newFilter = it.copy(filters = contextIdFilters)
                calendarRepository.updateCalendarFilters(newFilter)
            }
            _uiState.emit(createNewUiState(snackbarMessage = snackbarMessage))
        }
    }
}