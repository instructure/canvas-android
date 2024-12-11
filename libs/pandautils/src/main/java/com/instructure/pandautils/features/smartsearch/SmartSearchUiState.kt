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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.models.SmartSearchFilter

data class SmartSearchUiState(
    val query: String,
    val canvasContext: CanvasContext,
    val results: List<SmartSearchResultUiState>,
    val loading: Boolean = true,
    val error: Boolean = false,
    val filters: List<SmartSearchFilter> = SmartSearchFilter.entries,
    val sortType: SmartSearchSortType = SmartSearchSortType.RELEVANCE,
    val actionHandler: (SmartSearchAction) -> Unit
)

data class SmartSearchResultUiState(
    val title: String,
    val body: String,
    val relevance: Int,
    val url: String,
    val type: SmartSearchContentType
)

sealed class SmartSearchAction {
    data class Search(val query: String) : SmartSearchAction()
    data class Route(val url: String) : SmartSearchAction()
    data class Filter(val filters: List<SmartSearchFilter>, val sortType: SmartSearchSortType) :
        SmartSearchAction()
}

sealed class SmartSearchViewModelAction {
    data class Route(val url: String) : SmartSearchViewModelAction()
}