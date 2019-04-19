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

import com.instructure.teacher.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class ModuleListUpdate : UpdateInit<ModulesListModel, ModulesListEvent, ModulesListEffect>() {

    override fun performInit(model: ModulesListModel): First<ModulesListModel, ModulesListEffect> {
        return First.first(
            model.copy(isLoading = true),
            setOf(
                ModulesListEffect.LoadNextPage(
                    model.course,
                    model.pageData,
                    model.scrollToItemId
                )
            )
        )
    }

    override fun update(model: ModulesListModel, event: ModulesListEvent): Next<ModulesListModel, ModulesListEffect> {
        when (event) {
            ModulesListEvent.PullToRefresh -> {
                val newModel = model.copy(
                    isLoading = true,
                    modules = emptyList(),
                    pageData = ModuleListPageData(forceNetwork = true)
                )
                val effect = ModulesListEffect.LoadNextPage(
                    newModel.course,
                    newModel.pageData,
                    newModel.scrollToItemId
                )
                return Next.next(newModel, setOf(effect))
            }
            is ModulesListEvent.ModuleItemClicked -> {
                return Next.dispatch(setOf(ModulesListEffect.ShowModuleItemDetailView(event.moduleItem)))
            }
            is ModulesListEvent.PageLoaded -> {
                val effects = mutableSetOf<ModulesListEffect>()
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
                        effects += ModulesListEffect.ScrollToItem(model.scrollToItemId)
                    }
                }

                return Next.next(newModel, effects)
            }
            ModulesListEvent.NextPageRequested -> {
                return if (model.isLoading) {
                    // Do nothing if we're already loading
                    Next.noChange()
                } else {
                    val newModel = model.copy(isLoading = true)
                    val effect = ModulesListEffect.LoadNextPage(
                        model.course,
                        model.pageData,
                        model.scrollToItemId
                    )
                    Next.next(newModel, setOf(effect))
                }
            }
        }
    }

}
