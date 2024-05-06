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
package com.instructure.teacher.features.modules.list.ui

import android.content.Context
import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.teacher.adapters.GroupedRecyclerAdapter
import com.instructure.teacher.adapters.ListItemCallback
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListEmptyBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListEmptyItemBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListFullErrorBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListInlineErrorBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListItemBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListLoadingBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListModuleBinder
import com.instructure.teacher.features.modules.list.ui.binders.ModuleListSubHeaderBinder


interface ModuleListCallback : ListItemCallback {
    fun retryNextPage()
    fun moduleItemClicked(moduleItemId: Long)
    fun markModuleExpanded(moduleId: Long, isExpanded: Boolean)
    fun updateModuleItem(itemId: Long, isPublished: Boolean)
    fun publishModule(moduleId: Long)
    fun publishModuleAndItems(moduleId: Long)
    fun unpublishModuleAndItems(moduleId: Long)
    fun updateFileModuleItem(fileId: Long, contentDetails: ModuleContentDetails)

    fun showSnackbar(@StringRes message: Int, params: Array<Any>)
}

class ModuleListRecyclerAdapter(
    context: Context,
    callback: ModuleListCallback
) : GroupedRecyclerAdapter<ModuleListItemData, ModuleListCallback>(
    context,
    ModuleListItemData::class.java,
    callback
) {

    init {
        isExpandedByDefault = true
    }

    override fun registerBinders() {
        register(ModuleListEmptyBinder())
        register(ModuleListFullErrorBinder())
        register(ModuleListInlineErrorBinder())
        register(ModuleListModuleBinder())
        register(ModuleListItemBinder())
        register(ModuleListLoadingBinder())
        register(ModuleListEmptyItemBinder())
        register(ModuleListSubHeaderBinder())
    }

    fun setData(items: List<ModuleListItemData>, collapsedModuleIds: Set<Long>) {
        clear()
        clearExpanded()
        markExpanded(collapsedModuleIds, false)
        for (item in items) {
            if (item is ModuleListItemData.ModuleData && item.moduleItems.isNotEmpty()) {
                addOrUpdateAllItems(item, item.moduleItems)
            } else {
                addOrUpdateGroup(item)
            }
        }
    }

}


