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
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartSearchViewModel @Inject constructor(
    private val smartSearchRepository: SmartSearchRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val query: String = savedStateHandle.get<String>(QUERY).orEmpty()
    private val canvasContext: CanvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) ?: throw IllegalArgumentException("CanvasContext is required")

    private val _uiState = MutableStateFlow(SmartSearchUiState(query, emptyList()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            smartSearchRepository.smartSearch(canvasContext.id, query)
        }
    }
}