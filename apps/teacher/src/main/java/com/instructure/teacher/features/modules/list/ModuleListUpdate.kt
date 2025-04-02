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
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.utils.patchedBy
import com.instructure.teacher.R
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
                    if (model.scrollToItemId != null) {
                        if (newModules.any { module -> module.items.any { it.id == model.scrollToItemId } }) {
                            newModel = newModel.copy(scrollToItemId = null)
                            effects += ModuleListEffect.ScrollToItem(model.scrollToItemId)
                        } else if (newModules.any { it.id == model.scrollToItemId }) {
                            newModel = newModel.copy(scrollToItemId = null)
                            val idToScroll = newModules.first { it.id == model.scrollToItemId }.items.firstOrNull()?.id
                            idToScroll?.let {
                                effects += ModuleListEffect.ScrollToItem(it, true)
                            }
                        }
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
                return Next.dispatch(
                    setOf(
                        ModuleListEffect.MarkModuleExpanded(
                            model.course,
                            event.moduleId,
                            event.isExpanded
                        )
                    )
                )
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
                    affectedIds,
                    event.action,
                    event.skipContentTags,
                    false
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
                    affectedIds,
                    event.action,
                    event.skipContentTags,
                    true
                )
                return Next.next(newModel, setOf(effect))
            }

            is ModuleListEvent.BulkUpdateSuccess -> {
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

                val message = getBulkUpdateSnackbarMessage(event.action, event.skipContentTags, event.allModules)

                val snackbarEffect = ModuleListEffect.ShowSnackbar(message)

                return Next.next(newModel, setOf(effect, snackbarEffect))
            }

            is ModuleListEvent.BulkUpdateFailed -> {
                val newModel = model.copy(
                    loadingModuleItemIds = emptySet()
                )

                val snackbarEffect = ModuleListEffect.ShowSnackbar(R.string.errorOccurred)

                return Next.next(newModel, setOf(snackbarEffect))
            }

            is ModuleListEvent.UpdateModuleItem -> {
                val newModel = model.copy(
                    loadingModuleItemIds = model.loadingModuleItemIds + event.itemId
                )
                val effect = ModuleListEffect.UpdateModuleItem(
                    model.course,
                    model.modules.first { it.items.any { it.id == event.itemId } }.id,
                    event.itemId,
                    event.isPublished
                )
                return Next.next(newModel, setOf(effect))
            }

            is ModuleListEvent.ModuleItemUpdateSuccess -> {
                val newModel = model.copy(
                    modules = model.modules.map { module ->
                        if (event.item.moduleId == module.id) {
                            module.copy(items = module.items.patchedBy(listOf(event.item)) { it.id })
                        } else {
                            module
                        }
                    },
                    loadingModuleItemIds = model.loadingModuleItemIds - event.item.id
                )

                val snackbarEffect =
                    ModuleListEffect.ShowSnackbar(if (event.item.published == true) R.string.moduleItemPublished else R.string.moduleItemUnpublished)

                return Next.next(newModel, setOf(snackbarEffect))
            }

            is ModuleListEvent.ModuleItemUpdateFailed -> {
                val newModel = model.copy(
                    loadingModuleItemIds = model.loadingModuleItemIds - event.itemId
                )

                val snackbarEffect = ModuleListEffect.ShowSnackbar(R.string.errorOccurred)

                return Next.next(newModel, setOf(snackbarEffect))
            }

            is ModuleListEvent.UpdateFileModuleItem -> {
                val effect = ModuleListEffect.UpdateFileModuleItem(
                    event.fileId,
                    event.contentDetails
                )
                return Next.dispatch(setOf(effect))
            }

            is ModuleListEvent.BulkUpdateCancelled -> {
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
                val snackbarEffect = ModuleListEffect.ShowSnackbar(R.string.updateCancelled)
                return Next.next(newModel, setOf(effect, snackbarEffect))
            }

            is ModuleListEvent.BulkUpdateStarted -> {
                val newModel = model.copy(
                    loadingModuleItemIds = model.loadingModuleItemIds + event.affectedIds
                )
                val effect = ModuleListEffect.BulkUpdateStarted(
                    event.progressId,
                    event.allModules,
                    event.skipContentTags,
                    event.action
                )
                return Next.next(newModel, setOf(effect))
            }

            is ModuleListEvent.ShowSnackbar -> {
            val effect = ModuleListEffect.ShowSnackbar(event.message, event.params)
                return Next.dispatch(setOf(effect))
            }
        }
    }

    @StringRes
    private fun getBulkUpdateSnackbarMessage(
        action: BulkModuleUpdateAction,
        skipContentTags: Boolean,
        allModules: Boolean
    ): Int {
        return if (allModules) {
            if (action == BulkModuleUpdateAction.PUBLISH) {
                if (skipContentTags) {
                    R.string.onlyModulesPublished
                } else {
                    R.string.allModulesAndAllItemsPublished
                }
            } else {
                R.string.allModulesAndAllItemsUnpublished
            }
        } else {
            if (action == BulkModuleUpdateAction.PUBLISH) {
                if (skipContentTags) {
                    R.string.onlyModulePublished
                } else {
                    R.string.moduleAndAllItemsPublished
                }
            } else {
                R.string.moduleAndAllItemsUnpublished
            }
        }
    }

}
