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

import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.isValid

sealed class ModuleListEvent {
    object PullToRefresh : ModuleListEvent()
    object NextPageRequested : ModuleListEvent()
    data class ModuleItemClicked(val moduleItemId: Long) : ModuleListEvent()
    data class ModuleExpanded(val moduleId: Long, val isExpanded: Boolean) : ModuleListEvent()
    data class PageLoaded(val pageData: ModuleListPageData) : ModuleListEvent()
    data class ModuleItemLoadStatusChanged(val moduleItemIds: Set<Long>, val isLoading: Boolean) : ModuleListEvent()
    data class ItemRefreshRequested(val type: String, val predicate: (item: ModuleItem) -> Boolean) : ModuleListEvent()
    data class ReplaceModuleItems(val items: List<ModuleItem>) : ModuleListEvent()
    data class RemoveModuleItems(val type: String, val predicate: (item: ModuleItem) -> Boolean) : ModuleListEvent()
    data class BulkUpdateModule(val moduleId: Long, val action: BulkModuleUpdateAction, val skipContentTags: Boolean) :
        ModuleListEvent()

    data class BulkUpdateAllModules(val action: BulkModuleUpdateAction, val skipContentTags: Boolean) :
        ModuleListEvent()

    data class UpdateModuleItem(val itemId: Long, val isPublished: Boolean) : ModuleListEvent()
    data class ModuleItemUpdateSuccess(val item: ModuleItem, val published: Boolean) : ModuleListEvent()
    data class ModuleItemUpdateFailed(val itemId: Long) : ModuleListEvent()
    data class BulkUpdateSuccess(
        val skipContentTags: Boolean,
        val action: BulkModuleUpdateAction,
        val allModules: Boolean
    ) : ModuleListEvent()

    data class BulkUpdateFailed(val skipContentTags: Boolean) : ModuleListEvent()
    data class BulkUpdateStarted(
        val canvasContext: CanvasContext,
        val progressId: Long,
        val allModules: Boolean,
        val skipContentTags: Boolean,
        val affectedIds: List<Long>,
        val action: BulkModuleUpdateAction
    ) : ModuleListEvent()

    data class UpdateFileModuleItem(val fileId: Long, val contentDetails: ModuleContentDetails) : ModuleListEvent()
    object BulkUpdateCancelled : ModuleListEvent()
    data class ShowSnackbar(@StringRes val message: Int, val params: Array<Any> = emptyArray()): ModuleListEvent()
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

    data class ScrollToItem(val moduleItemId: Long, val scrollToHeaderItem: Boolean = false) : ModuleListEffect()
    data class MarkModuleExpanded(
        val canvasContext: CanvasContext,
        val moduleId: Long,
        val isExpanded: Boolean
    ) : ModuleListEffect()

    data class UpdateModuleItems(val canvasContext: CanvasContext, val items: List<ModuleItem>) : ModuleListEffect()

    data class BulkUpdateModules(
        val canvasContext: CanvasContext,
        val moduleIds: List<Long>,
        val affectedIds: List<Long>,
        val action: BulkModuleUpdateAction,
        val skipContentTags: Boolean,
        val allModules: Boolean
    ) : ModuleListEffect()

    data class UpdateModuleItem(
        val canvasContext: CanvasContext,
        val moduleId: Long,
        val itemId: Long,
        val published: Boolean
    ) : ModuleListEffect()

    data class ShowSnackbar(@StringRes val message: Int, val params: Array<Any> = emptyArray()) : ModuleListEffect() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ShowSnackbar

            if (message != other.message) return false
            if (!params.contentEquals(other.params)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = message
            result = 31 * result + params.contentHashCode()
            return result
        }
    }

    data class UpdateFileModuleItem(
        val fileId: Long,
        val contentDetails: ModuleContentDetails
    ) : ModuleListEffect()

    data class BulkUpdateStarted(
        val progressId: Long,
        val allModules: Boolean,
        val skipContentTags: Boolean,
        val action: BulkModuleUpdateAction
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
