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
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.ModuleManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.canvasapi2.utils.weave.awaitOrThrow
import com.instructure.teacher.features.modules.list.ui.ModuleListView
import com.instructure.teacher.mobius.common.ui.EffectHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

class ModuleListEffectHandler : EffectHandler<ModuleListView, ModuleListEvent, ModuleListEffect>() {

    private var fileInfoJob: Job? = null

    override fun accept(effect: ModuleListEffect) {
        when (effect) {
            is ModuleListEffect.ShowModuleItemDetailView -> {
                view?.routeToModuleItem(effect.moduleItem, effect.canvasContext)
            }
            is ModuleListEffect.LoadFileInfo -> {
                loadFileInfo(effect.item, effect.canvasContext)
            }
            is ModuleListEffect.LoadNextPage -> loadNextPage(
                effect.canvasContext,
                effect.pageData,
                effect.scrollToItemId
            )
            is ModuleListEffect.ScrollToItem -> view?.scrollToItem(effect.moduleItemId)
            is ModuleListEffect.MarkModuleExpanded -> {
                CollapsedModulesStore.markModuleCollapsed(effect.canvasContext, effect.moduleId, !effect.isExpanded)
            }
            is ModuleListEffect.UpdateModuleItems -> updateModuleItems(effect.canvasContext, effect.items)
        }.exhaustive
    }

    private fun updateModuleItems(canvasContext: CanvasContext, items: List<ModuleItem>) {
        launch {
            val ids = items.map { it.id }.toSet()
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(ids, true))
            tryOrNull {
                val updatedItems = items
                    .map { item -> ModuleManager.getModuleItemAsync(canvasContext, item.moduleId, item.id, true) }
                    .mapNotNull { it.await().dataOrNull }
                consumer.accept(ModuleListEvent.ReplaceModuleItems(updatedItems))
                CanvasRestAdapter.clearCacheUrls("""/modules""")
            }
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(ids, false))
        }
    }

    private fun loadFileInfo(item: ModuleItem, canvasContext: CanvasContext) {
        fileInfoJob?.cancel()
        fileInfoJob = launch {
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(setOf(item.id), true))
            tryOrNull {
                // Get the file
                val file: FileFolder = FileFolderManager.getFileFolderFromUrlAsync(item.url!!, false).awaitOrThrow()

                // Get usage rights and licenses, if applicable
                val features = FeaturesManager.getEnabledFeaturesForCourseAsync(canvasContext.id, false).awaitOrThrow()

                val requiresUsageRights = features.contains("usage_rights_required")
                val licenses = if (requiresUsageRights) {
                    FileFolderManager.getCourseFileLicensesAsync(canvasContext.id).awaitOrThrow()
                } else {
                    emptyList<License>()
                }
                view?.routeToFile(canvasContext, file, requiresUsageRights, licenses)
            }
            consumer.accept(ModuleListEvent.ModuleItemLoadStatusChanged(setOf(item.id), false))
        }
    }

    private fun loadNextPage(canvasContext: CanvasContext, lastPageData: ModuleListPageData, scrollToItemId: Long?) {
        launch {
            try {
                val newPageData = if (scrollToItemId != null) {
                    fetchDataUntilItem(lastPageData, canvasContext, scrollToItemId)
                } else {
                    fetchPageData(canvasContext, lastPageData)
                }
                consumer.accept(ModuleListEvent.PageLoaded(newPageData))
            } catch (e: Throwable) {
                e.printStackTrace()
                consumer.accept(
                    ModuleListEvent.PageLoaded(
                        lastPageData.copy(lastPageResult = DataResult.Fail(Failure.Network(e.message)))
                    )
                )
            }
        }
    }

    /**
     * Fetches module pages sequentially until a module item with the specified [targetItemId] is fetched.
     */
    private suspend fun fetchDataUntilItem(
        lastPageData: ModuleListPageData,
        canvasContext: CanvasContext,
        targetItemId: Long
    ): ModuleListPageData {
        val fetchedModules = mutableListOf<ModuleObject>()
        var latestData = lastPageData
        var targetModule: ModuleObject?

        do {
            val data = fetchPageData(canvasContext, latestData)
            val modules = data.lastPageResult!!.dataOrThrow
            fetchedModules += modules
            latestData = data
            targetModule = modules.find { module -> module.items.any { it.id == targetItemId } }
        } while (targetModule == null && latestData.nextPageUrl.isValid())

        targetModule?.let {
            // Mark the module containing the target item as expanded so the view can auto scroll to it
            CollapsedModulesStore.markModuleCollapsed(canvasContext, it.id, false)
        }

        return latestData.copy(lastPageResult = DataResult.Success(fetchedModules))
    }

    /**
     * Fetches a page of modules given existing [pageData]. If no pages have been fetched previously, this will
     * fetch the first page for the provided [canvasContext]. The endpoint used for this fetch does not guarantee that
     * all module items will be returned, so if any module in the resulting page is missing any items then additional
     * API calls will be made to a secondary endpoint to ensure that all items are returned.
     */
    private suspend fun fetchPageData(
        canvasContext: CanvasContext,
        pageData: ModuleListPageData
    ): ModuleListPageData {
        val response: Response<List<ModuleObject>> = when {
            pageData.isFirstPage -> awaitApiResponse {
                ModuleManager.getFirstPageModulesWithItems(canvasContext, it, pageData.forceNetwork)
            }
            pageData.nextPageUrl.isValid() -> awaitApiResponse {
                ModuleManager.getNextPageModuleObjects(pageData.nextPageUrl, it, pageData.forceNetwork)
            }
            else -> throw IllegalStateException("Unable to fetch page data; invalid nextPageUrl")
        }

        // Fetch any missing items
        val modules = response.body()!!.map { module ->
            if (module.itemCount == module.items.size) {
                module
            } else {
                module.copy(
                    items = awaitApi {
                        ModuleManager.getAllModuleItems(canvasContext, module.id, it, pageData.forceNetwork)
                    }
                )
            }
        }

        return pageData.copy(
            lastPageResult = DataResult.Success(modules),
            nextPageUrl = APIHelper.parseLinkHeaderResponse(response.headers()).nextUrl
        )
    }

}

