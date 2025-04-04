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
package com.instructure.pandautils.features.smartsearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartSearchViewModel @Inject constructor(
    private val smartSearchRepository: SmartSearchRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val query: String = savedStateHandle.get<String>(QUERY).orEmpty()
    private val canvasContext: CanvasContext =
        savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) ?: throw IllegalArgumentException(
            "CanvasContext is required"
        )

    private val _uiState = MutableStateFlow(
        SmartSearchUiState(
            query,
            canvasContext,
            emptyList(),
            actionHandler = this::handleAction
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SmartSearchViewModelAction>()
    val events = _events.receiveAsFlow()

    private val visitedItems = mutableSetOf<String>()
    private var lastVisited = ""

    init {
        viewModelScope.launch {
            search(canvasContext.id, query)
        }
    }

    private suspend fun search(
        courseId: Long,
        query: String,
        filters: List<SmartSearchFilter> = SmartSearchFilter.entries
    ) {
        _uiState.emit(
            _uiState.value.copy(error = false, loading = true, query = query, filters = filters)
        )
        try {
            val results = smartSearchRepository.smartSearch(courseId, query, filters)
                .map { result ->
                    SmartSearchResultUiState(
                        title = result.title,
                        body = result.body,
                        relevance = result.relevance,
                        url = result.htmlUrl,
                        type = result.contentType,
                        visited = visitedItems.contains(result.htmlUrl),
                        lastVisited = result.htmlUrl == lastVisited
                    )
                }
            _uiState.emit(_uiState.value.copy(results = results, loading = false))
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.emit(_uiState.value.copy(error = true, loading = false))
        }
    }

    private fun handleAction(action: SmartSearchAction) {
        when (action) {
            is SmartSearchAction.Search -> {
                viewModelScope.launch {
                    search(canvasContext.id, action.query)
                }
            }

            is SmartSearchAction.Route -> {
                viewModelScope.launch {
                    visitedItems.add(action.url)
                    lastVisited = action.url
                    _uiState.emit(
                        _uiState.value.copy(
                            results = _uiState.value.results.map { result ->
                                if (result.url == action.url) {
                                    result.copy(visited = true, lastVisited = true)
                                } else {
                                    result.copy(lastVisited = false)
                                }
                            }
                        )
                    )
                    _events.send(SmartSearchViewModelAction.Route(action.url))
                }
            }

            is SmartSearchAction.Filter -> {
                viewModelScope.launch {
                    if (action.filters.toSet() == _uiState.value.filters.toSet()) return@launch
                    search(canvasContext.id, _uiState.value.query, action.filters)
                }
            }
        }
    }
}