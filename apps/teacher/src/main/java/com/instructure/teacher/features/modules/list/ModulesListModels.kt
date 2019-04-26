/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.isValid

sealed class ModulesListEvent {
    object PullToRefresh : ModulesListEvent()
    object NextPageRequested : ModulesListEvent()
    data class ModuleItemClicked(val moduleItemId: Long) : ModulesListEvent()
    data class ModuleExpanded(val moduleId: Long, val isExpanded: Boolean) : ModulesListEvent()
    data class PageLoaded(val pageData: ModuleListPageData) : ModulesListEvent()
}

sealed class ModulesListEffect {
    data class ShowModuleItemDetailView(val moduleItem: ModuleItem) : ModulesListEffect()
    data class LoadNextPage(
        val canvasContext: CanvasContext,
        val pageData: ModuleListPageData,
        val scrollToItemId: Long?
    ) : ModulesListEffect()
    data class ScrollToItem(val moduleItemId: Long) : ModulesListEffect()
    data class MarkModuleExpanded(
        val canvasContext: CanvasContext,
        val moduleId: Long,
        val isExpanded: Boolean
    ) : ModulesListEffect()
}

data class ModuleListModel(
    val course: CanvasContext,
    val isLoading: Boolean = false,
    val scrollToItemId: Long? = null,
    val pageData: ModuleListPageData = ModuleListPageData(),
    val modules: List<ModuleObject> = emptyList()
)

data class ModuleListPageData(
    val lastPageResult: DataResult<List<ModuleObject>>? = null,
    val forceNetwork: Boolean = false,
    val nextPageUrl: String? = null
) {
    val isFirstPage get() = lastPageResult == null
    val hasMorePages get() = isFirstPage || nextPageUrl.isValid()
}
