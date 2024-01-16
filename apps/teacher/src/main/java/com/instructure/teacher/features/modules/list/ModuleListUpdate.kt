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

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.utils.patchedBy
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData
import com.instructure.teacher.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class ModuleListUpdate : UpdateInit<ModuleListModel, ModuleListEvent, ModuleListEffect>() {

    override fun performInit(model: ModuleListModel): First<ModuleListModel, ModuleListEffect> {
        return First.first(
            model.copy(isLoading = true),
            setOf(
                ModuleListEffect.LoadNextPage(
                    model.course,
                    model.pageData,
                    model.scrollToItemId
                )
            )
        )
    }

    override fun update(model: ModuleListModel, event: ModuleListEvent): Next<ModuleListModel, ModuleListEffect> {
        when (event) {
            ModuleListEvent.PullToRefresh -> {
                val newModel = model.copy(
                    isLoading = true,
                    modules = emptyList(),
                    pageData = ModuleListPageData(forceNetwork = true)
                )
                val effect = ModuleListEffect.LoadNextPage(
                    newModel.course,
                    newModel.pageData,
                    newModel.scrollToItemId
                )
                return Next.next(newModel, setOf(effect))
            }
            is ModuleListEvent.ModuleItemClicked -> {
                val item = model.modules.flatMap { it.items }.first { it.id == event.moduleItemId }
                return Next.dispatch(setOf(ModuleListEffect.ShowModuleItemDetailView(item, model.course)))
            }
            is ModuleListEvent.PageLoaded -> {
                val effects = mutableSetOf<ModuleListEffect>()
                var newModel = model.copy(
                    isLoading = false,
                    pageData = event.pageData
                )

                if (event.pageData.lastPageResult?.isSuccess == true) {
                    val newModules = event.pageData.lastPageResult.dataOrThrow
                    newModel = newModel.copy(modules = model.modules + newModules)
                    if (model.scrollToItemId != null
                        && newModules.any { module -> module.items.any { it.id == model.scrollToItemId } }) {
                        newModel = newModel.copy(scrollToItemId = null)
                        effects += ModuleListEffect.ScrollToItem(model.scrollToItemId)
                    }
                }

                return Next.next(newModel, effects)
            }
            ModuleListEvent.NextPageRequested -> {
                return if (model.isLoading || !model.pageData.hasMorePages) {
                    // Do nothing if we're already loading or all pages have loaded
                    Next.noChange()
                } else {
                    val newModel = model.copy(isLoading = true)
                    val effect = ModuleListEffect.LoadNextPage(
                        model.course,
                        model.pageData,
                        model.scrollToItemId
                    )
                    Next.next(newModel, setOf(effect))
                }
            }
            is ModuleListEvent.ModuleExpanded -> {
                return Next.dispatch(setOf(ModuleListEffect.MarkModuleExpanded(
                    model.course,
                    event.moduleId,
                    event.isExpanded
                )))
            }
            is ModuleListEvent.ModuleItemLoadStatusChanged -> {
                return Next.next(
                    model.copy(
                        loadingModuleItemIds = if (event.isLoading) {
                            model.loadingModuleItemIds + event.moduleItemIds
                        } else {
                            model.loadingModuleItemIds - event.moduleItemIds
                        }
                    )
                )
            }
            is ModuleListEvent.ItemRefreshRequested -> {
                val items = model.modules.flatMap { it.items }.filter { it.type == event.type }.filter(event.predicate)
                return if (items.isEmpty()) {
                    Next.noChange()
                } else {
                    val effect = ModuleListEffect.UpdateModuleItems(model.course, items)
                    Next.dispatch(setOf(effect))
                }
            }
            is ModuleListEvent.ReplaceModuleItems -> {
                val itemGroups = event.items.groupBy { it.moduleId }
                val newModel = model.copy(
                    modules = model.modules.map { module ->
                        val items = itemGroups[module.id]
                        if (items == null) {
                            module
                        } else {
                            module.copy(items = module.items.patchedBy(items) { it.id })
                        }
                    }
                )
                return Next.next(newModel)
            }
            is ModuleListEvent.RemoveModuleItems -> {
                val newModel = model.copy(
                    modules = model.modules.map { module ->
                        module.copy(
                            items = module.items.filter { it.type != event.type || !event.predicate(it) }
                        )
                    }
                )
                CanvasRestAdapter.clearCacheUrls("""/modules""")
                return Next.next(newModel)
            }
            is ModuleListEvent.BulkUpdateModule -> {
                val affectedIds = mutableListOf(event.moduleId)
                if (!event.skipContentTags) {
                    affectedIds.addAll(model.modules.filter { it.id == event.moduleId }
                        .flatMap { it.items }
                        .map { it.id })
                }

                val newModel = model.copy(
                    loadingModuleItemIds = model.loadingModuleItemIds + affectedIds
                )
                val effect = ModuleListEffect.BulkUpdateModules(
                    model.course,
                    listOf(event.moduleId),
                    event.event,
                    event.skipContentTags
                )
                return Next.next(newModel, setOf(effect))
            }
            is ModuleListEvent.BulkUpdateAllModules -> {
                val affectedIds = mutableListOf<Long>()
                affectedIds.addAll(model.modules.map { it.id })
                if (!event.skipContentTags) {
                    affectedIds.addAll(model.modules.flatMap { it.items }.map { it.id })
                }

                val newModel = model.copy(
                    loadingModuleItemIds = model.loadingModuleItemIds + affectedIds
                )
                val effect = ModuleListEffect.BulkUpdateModules(
                    model.course,
                    model.modules.map { it.id },
                    event.event,
                    event.skipContentTags
                )
                return Next.next(newModel, setOf(effect))
            }
            is ModuleListEvent.BulkUpdateFinished -> {
                val newModel = model.copy(
                    isLoading = true,
                    modules = emptyList(),
                    pageData = ModuleListPageData(forceNetwork = true),
                    loadingModuleItemIds = emptySet()
                )
                val effect = ModuleListEffect.LoadNextPage(
                    newModel.course,
                    newModel.pageData,
                    newModel.scrollToItemId
                )
                return Next.next(newModel, setOf(effect))
            }
        }
    }

}
