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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarFilterViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val calendarFilterDao: CalendarFilterDao,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private var canvasContexts = emptyMap<CanvasContext.Type, List<CanvasContext>>()
    private val contextIdFilters = mutableSetOf<String>()
    private var filterEntityForCurrentUser: CalendarFilterEntity? = null

    private val _uiState = MutableStateFlow(CalendarFilterScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFilters()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            val result = calendarRepository.getCanvasContexts()
            if (result is DataResult.Success) {
                canvasContexts = result.data

                val filters = calendarFilterDao.findByUserIdAndDomain(apiPrefs.user?.id.orDefault(), apiPrefs.fullDomain)
                if (filters.isNotEmpty()) {
                    val filterEntity = filters[0]
                    filterEntityForCurrentUser = filterEntity
                    contextIdFilters.addAll(filterEntity.filters)
                }

                _uiState.value = createNewUiState()
            }
        }
    }

    private fun createNewUiState(): CalendarFilterScreenUiState {
        return CalendarFilterScreenUiState(
            createFilterItemsUiState(CanvasContext.Type.USER),
            createFilterItemsUiState(CanvasContext.Type.COURSE),
            createFilterItemsUiState(CanvasContext.Type.GROUP)
        )
    }

    private fun createFilterItemsUiState(type: CanvasContext.Type) = canvasContexts[type]?.map {
        CalendarFilterItemUiState(it.contextId, it.name.orEmpty(), contextIdFilters.contains(it.contextId))
    } ?: emptyList()

    fun handleAction(calendarFilterAction: CalendarFilterAction) {
        when (calendarFilterAction) {
            is CalendarFilterAction.ToggleFilter -> toggleFilter(calendarFilterAction.contextId)
        }
    }

    private fun toggleFilter(contextId: String) {
        if (contextIdFilters.contains(contextId)) {
            contextIdFilters.remove(contextId)
        } else {
            contextIdFilters.add(contextId)
        }
        viewModelScope.launch {
            filterEntityForCurrentUser?.let {
                val newFilter = it.copy(filters = contextIdFilters)
                calendarFilterDao.insertOrUpdate(newFilter)
            }
            _uiState.emit(createNewUiState())
        }
    }
}