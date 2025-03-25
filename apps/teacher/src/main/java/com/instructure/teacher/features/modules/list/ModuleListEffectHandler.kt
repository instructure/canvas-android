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
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.ModuleManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.pandautils.features.progress.ProgressPreferences
import com.instructure.pandautils.room.appdatabase.daos.ModuleBulkProgressDao
import com.instructure.pandautils.room.appdatabase.entities.ModuleBulkProgressEntity
import com.instructure.pandautils.utils.poll
import com.instructure.pandautils.utils.retry
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ui.ModuleListView
import com.instructure.teacher.mobius.common.ui.EffectHandler
import kotlinx.coroutines.launch
import retrofit2.Response

class ModuleListEffectHandler(
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val progressApi: ProgressAPI.ProgressInterface,
    private val progressPreferences: ProgressPreferences,
    private val moduleBulkProgressDao: ModuleBulkProgressDao
) : EffectHandler<ModuleListView, ModuleListEvent, ModuleListEffect>() {
    override fun accept(effect: ModuleListEffect) {
        when (effect) {
            is ModuleListEffect.ShowModuleItemDetailView -> {
                view?.routeToModuleItem(effect.moduleItem, effect.canvasContext)
            }

            is ModuleListEffect.LoadNextPage -> loadNextPage(
                effect.canvasContext,
                effect.pageData,
                effect.scrollToItemId
            )

            is ModuleListEffect.ScrollToItem -> view?.scrollToItem(effect.moduleItemId, effect.scrollToHeaderItem)
            is ModuleListEffect.MarkModuleExpanded -> {
                CollapsedModulesStore.markModuleCollapsed(effect.canvasContext, effect.moduleId, !effect.isExpanded)
            }

            is ModuleListEffect.UpdateModuleItems -> updateModuleItems(effect.canvasContext, effect.items)
            is ModuleListEffect.BulkUpdateModules -> bulkUpdateModules(
                effect.canvasContext,
                effect.moduleIds,
                effect.affectedIds,
                effect.action,
                effect.skipContentTags,
                allModules = effect.allModules
            )

            is ModuleListEffect.UpdateModuleItem -> updateModuleItem(
                effect.canvasContext,
                effect.moduleId,
                effect.itemId,
                effect.published
            )

            is ModuleListEffect.ShowSnackbar -> {
                view?.showSnackbar(effect.message, effect.params)
            }

            is ModuleListEffect.UpdateFileModuleItem -> {
                view?.showUpdateFileDialog(effect.fileId, effect.contentDetails)
            }

            is ModuleListEffect.BulkUpdateStarted -> {
                handleBulkUpdate(effect.progressId, effect.allModules, effect.skipContentTags, effect.action)
            }
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

    private fun bulkUpdateModules(
        canvasContext: CanvasContext,
        moduleIds: List<Long>,
        affectedIds: List<Long>,
        action: BulkModuleUpdateAction,
        skipContentTags: Boolean,
        async: Boolean = true,
        allModules: Boolean
    ) {
        launch {
            val restParams = RestParams(
                canvasContext = canvasContext,
                isForceReadFromNetwork = true
            )
            val progress = moduleApi.bulkUpdateModules(
                canvasContext.type.apiString,
                canvasContext.id,
                moduleIds,
                action.event,
                skipContentTags,
                async,
                restParams
            ).dataOrNull?.progress

            val bulkUpdateProgress = progress?.progress
            if (bulkUpdateProgress == null) {
                consumer.accept(ModuleListEvent.BulkUpdateFailed(skipContentTags))
            } else {
                moduleBulkProgressDao.insert(
                    ModuleBulkProgressEntity(
                        courseId = canvasContext.id,
                        progressId = bulkUpdateProgress.id,
                        action = action.toString(),
                        skipContentTags = skipContentTags,
                        allModules = allModules,
                        affectedIds = affectedIds
                    )
                )
                if (allModules || !skipContentTags) {
                    showProgressScreen(bulkUpdateProgress.id, skipContentTags, action, allModules)
                }
                consumer.accept(
                    ModuleListEvent.BulkUpdateStarted(
                        canvasContext,
                        bulkUpdateProgress.id,
                        allModules,
                        skipContentTags,
                        affectedIds,
                        action
                    )
                )
            }
        }
    }

    private fun handleBulkUpdate(
        progressId: Long,
        allModules: Boolean,
        skipContentTags: Boolean,
        action: BulkModuleUpdateAction
    ) {
        launch {
            val success = trackUpdateProgress(progressId)
            moduleBulkProgressDao.deleteById(progressId)

            if (success) {
                consumer.accept(ModuleListEvent.BulkUpdateSuccess(skipContentTags, action, allModules))
            } else {
                if (progressPreferences.cancelledProgressIds.contains(progressId)) {
                    consumer.accept(ModuleListEvent.BulkUpdateCancelled)
                    progressPreferences.cancelledProgressIds = progressPreferences.cancelledProgressIds - progressId
                } else {
                    consumer.accept(ModuleListEvent.BulkUpdateFailed(skipContentTags))
                }
            }
        }
    }

    private suspend fun trackUpdateProgress(progressId: Long): Boolean {
        val params = RestParams(isForceReadFromNetwork = true)

        val result = poll(500, maxAttempts = -1,
            validate = {
                it.hasRun
            },
            block = {
                var newProgress: Progress? = null
                retry(initialDelay = 500) {
                    newProgress = progressApi.getProgress(progressId.toString(), params).dataOrThrow
                }
                newProgress
            })

        return result?.hasRun == true && result.isCompleted
    }

    private fun updateModuleItem(canvasContext: CanvasContext, moduleId: Long, itemId: Long, published: Boolean) {
        launch {
            val restParams = RestParams(
                canvasContext = canvasContext,
                isForceReadFromNetwork = true
            )
            val moduleItem = moduleApi.publishModuleItem(
                canvasContext.type.apiString,
                canvasContext.id,
                moduleId,
                itemId,
                published,
                restParams
            ).dataOrNull

            moduleItem?.let {
                consumer.accept(ModuleListEvent.ModuleItemUpdateSuccess(it, published))
            } ?: consumer.accept(ModuleListEvent.ModuleItemUpdateFailed(itemId))
        }
    }

    private fun showProgressScreen(
        progressId: Long,
        skipContentTags: Boolean,
        action: BulkModuleUpdateAction,
        allModules: Boolean
    ) {
        val title = when {
            allModules && skipContentTags -> R.string.allModules
            allModules && !skipContentTags -> R.string.allModulesAndItems
            !allModules && !skipContentTags -> R.string.selectedModulesAndItems
            else -> R.string.selectedModules
        }

        val progressTitle = when (action) {
            BulkModuleUpdateAction.PUBLISH -> R.string.publishing
            BulkModuleUpdateAction.UNPUBLISH -> R.string.unpublishing
        }

        view?.showProgressDialog(progressId, title, progressTitle, R.string.moduleBulkUpdateNote)
    }
}
