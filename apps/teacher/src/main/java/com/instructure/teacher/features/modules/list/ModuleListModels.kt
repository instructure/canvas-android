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

sealed class ModuleListEvent {
    object PullToRefresh : ModuleListEvent()
    object NextPageRequested : ModuleListEvent()
    object BulkUpdateSuccess : ModuleListEvent()
    object BulkUpdateFailed : ModuleListEvent()
    data class ModuleItemClicked(val moduleItemId: Long) : ModuleListEvent()
    data class ModuleExpanded(val moduleId: Long, val isExpanded: Boolean) : ModuleListEvent()
    data class PageLoaded(val pageData: ModuleListPageData) : ModuleListEvent()
    data class ModuleItemLoadStatusChanged(val moduleItemIds: Set<Long>, val isLoading: Boolean) : ModuleListEvent()
    data class ItemRefreshRequested(val type: String, val predicate: (item: ModuleItem) -> Boolean) : ModuleListEvent()
    data class ReplaceModuleItems(val items: List<ModuleItem>) : ModuleListEvent()
    data class RemoveModuleItems(val type: String, val predicate: (item: ModuleItem) -> Boolean) : ModuleListEvent()
    data class BulkUpdateModule(val moduleId: Long, val action: BulkModuleUpdateAction, val skipContentTags: Boolean) : ModuleListEvent()
    data class BulkUpdateAllModules(val action: BulkModuleUpdateAction, val skipContentTags: Boolean) : ModuleListEvent()
    data class UpdateModuleItem(val itemId: Long, val isPublished: Boolean) : ModuleListEvent()
    data class ModuleItemUpdateSuccess(val item: ModuleItem): ModuleListEvent()
    data class ModuleItemUpdateFailed(val itemId: Long): ModuleListEvent()
    data class SetFileModuleItemPublished(val moduleItemId: Long, val fileId: Long, val isPublished: Boolean) : ModuleListEvent()
}

sealed class ModuleListEffect {
    data class ShowModuleItemDetailView(
        val moduleItem: ModuleItem,
        val canvasContext: CanvasContext
    ) : ModuleListEffect()

    data class LoadNextPage(
        val canvasContext: CanvasContext,
        val pageData: ModuleListPageData,
        val scrollToItemId: Long?
    ) : ModuleListEffect()

    data class ScrollToItem(val moduleItemId: Long) : ModuleListEffect()
    data class MarkModuleExpanded(
        val canvasContext: CanvasContext,
        val moduleId: Long,
        val isExpanded: Boolean
    ) : ModuleListEffect()

    data class UpdateModuleItems(val canvasContext: CanvasContext, val items: List<ModuleItem>) : ModuleListEffect()

    data class BulkUpdateModules(
        val canvasContext: CanvasContext,
        val moduleIds: List<Long>,
        val action: BulkModuleUpdateAction,
        val skipContentTags: Boolean
    ) : ModuleListEffect()

    data class UpdateModuleItem(
        val canvasContext: CanvasContext,
        val moduleId: Long,
        val itemId: Long,
        val published: Boolean
    ) : ModuleListEffect()

    data class SetFileModuleItemPublished(
        val canvasContext: CanvasContext,
        val moduleId: Long,
        val moduleItemId: Long,
        val fileId: Long,
        val isPublished: Boolean
    ) : ModuleListEffect()
}

data class ModuleListModel(
    val course: CanvasContext,
    val isLoading: Boolean = false,
    val scrollToItemId: Long? = null,
    val pageData: ModuleListPageData = ModuleListPageData(),
    val modules: List<ModuleObject> = emptyList(),
    val loadingModuleItemIds: Set<Long> = emptySet()
)

data class ModuleListPageData(
    val lastPageResult: DataResult<List<ModuleObject>>? = null,
    val forceNetwork: Boolean = false,
    val nextPageUrl: String? = null
) {
    val isFirstPage get() = lastPageResult == null
    val hasMorePages get() = isFirstPage || nextPageUrl.isValid()
}

enum class BulkModuleUpdateAction(val event: String) {
    PUBLISH("publish"),
    UNPUBLISH("unpublish")
}
