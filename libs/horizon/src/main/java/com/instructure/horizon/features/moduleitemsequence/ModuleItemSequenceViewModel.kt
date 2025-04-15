/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItem.Type
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.isLocked
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressPageItem
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressPageUiState
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressScreenUiState
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardStateMapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.formatDayMonth
import com.instructure.pandautils.utils.formatIsoDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ModuleItemSequenceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ModuleItemSequenceRepository,
    private val moduleItemCardStateMapper: ModuleItemCardStateMapper,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ModuleItemSequenceUiState(
                onPreviousClick = ::previousClicked,
                onNextClick = ::nextClicked,
                onProgressClick = ::progressClicked,
                progressScreenState = ProgressScreenUiState(
                    onCloseClick = ::progressCloseClicked,
                    onPreviousClick = ::progressPreviousClicked,
                    onNextClick = ::progressNextClicked,
                )
            )
        )
    val uiState = _uiState.asStateFlow()

    private val courseId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().courseId
    private val moduleItemId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemId
    private val moduleItemAssetType = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemAssetType
    private val moduleItemAssetId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemAssetType

    private var modules = emptyList<ModuleObject>()
    private var moduleItems = emptyList<ModuleItem>()

    init {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = true))
            }
            loadData()
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true))
            }
        }
    }

    private suspend fun loadData() {
        val moduleItemId = if (moduleItemId != null) {
            moduleItemId
        } else {
            val assetType = moduleItemAssetType ?: throw IllegalArgumentException("Module item sequence shouldn't be opened without moduleItemId or moduleItemAsset")
            val assetId = moduleItemAssetId ?: throw IllegalArgumentException("Module item sequence shouldn't be opened without moduleItemId or moduleItemAsset")
            val moduleItemSequence = repository.getModuleItemSequence(courseId, assetType, assetId)
            moduleItemSequence.items?.firstOrNull()?.current?.id ?: -1L
        }

        modules = repository.getModulesWithItems(courseId)
        moduleItems = modules.flatMap { it.items }

        val items = modules
            .flatMap { it.items }.mapNotNull { createModuleItemUiState(it, modules) }

        val progressPages = modules.map { createProgressPage(it) }

        val initialPosition = items.indexOfFirst { it.moduleItemId == moduleItemId }.coerceAtLeast(0)

        _uiState.update {
            it.copy(
                items = items,
                currentPosition = initialPosition,
                currentItem = getCurrentItem(initialPosition, items),
                progressScreenState = it.progressScreenState.copy(
                    pages = progressPages,
                    currentPosition = getProgressPosition(moduleItemId, progressPages)
                ),
            )
        }
    }

    private fun createProgressPage(module: ModuleObject): ProgressPageUiState {
        val progressPageItems = module.items.mapNotNull {
            createProgressPageItem(it)
        }

        return ProgressPageUiState(moduleName = module.name.orEmpty(), moduleId = module.id, items = progressPageItems)
    }

    private fun createProgressPageItem(moduleItem: ModuleItem): ProgressPageItem? {
        return if (moduleItem.type == Type.SubHeader.name) {
            ProgressPageItem.SubHeader(moduleItem.title.orEmpty())
        } else {
            val moduleItemCardState = moduleItemCardStateMapper.mapModuleItemToCardState(moduleItem, ::moduleItemSelected)
            moduleItemCardState?.let {
                ProgressPageItem.ModuleItem(moduleItemId = moduleItem.id, moduleItemCardState = it)
            }
        }
    }

    private fun createModuleItemUiState(item: ModuleItem, modules: List<ModuleObject>): ModuleItemUiState? {
        val moduleItemContent: ModuleItemContent? = when {
            item.isLocked() -> ModuleItemContent.Locked(
                item.moduleDetails?.lockExplanation ?: context.getString(R.string.modulePager_locked)
            )

            item.type == Type.Page.name -> ModuleItemContent.Page(item.pageUrl.orEmpty())
            item.type == Type.Assignment.name -> ModuleItemContent.Assignment(item.contentId)
            item.type == Type.Quiz.name -> ModuleItemContent.Assessment(item.contentId)
            item.type == Type.ExternalUrl.name -> ModuleItemContent.ExternalLink(item.htmlUrl.orEmpty())
            item.type == Type.ExternalTool.name -> ModuleItemContent.ExternalTool(item.htmlUrl.orEmpty())
            item.type == Type.File.name -> ModuleItemContent.File(item.url.orEmpty())
            else -> null
        }

        if (moduleItemContent == null) {
            return null
        }

        return ModuleItemUiState(
            moduleName = modules.find { item.moduleId == it.id }?.name.orEmpty(),
            moduleItemName = item.title.orEmpty(),
            moduleItemId = item.id,
            moduleItemContent = moduleItemContent,
            detailTags = createDetailTags(item),
            pillText = createPillText(item)
        )
    }

    private fun createDetailTags(item: ModuleItem): List<String> {
        val detailTags = mutableListOf<String>()
        item.estimatedDuration?.let {
            detailTags.add(it.formatIsoDuration(context))
        }
        item.moduleDetails?.dueDate?.let {
            detailTags.add(context.getString(R.string.modulePager_dueDate, it.formatDayMonth()))
        }
        item.moduleDetails?.pointsPossible?.let {
            val points = it.toDoubleOrNull()?.toInt() ?: 0
            detailTags.add(context.resources.getQuantityString(R.plurals.modulePager_pointsPossible, points, points))
        }
        // TODO Handle attempts in the assignment details ticket
        return detailTags
    }

    private fun createPillText(item: ModuleItem): String? {
        return if (item.overDue) context.getString(R.string.modulePager_overdue) else null
    }

    private fun previousClicked() {
        if (_uiState.value.currentPosition > 0) {
            val newPosition = _uiState.value.currentPosition - 1
            _uiState.update { it.copy(currentPosition = newPosition, currentItem = getCurrentItem(newPosition)) }
        }
    }

    private fun nextClicked() {
        if (_uiState.value.currentPosition < _uiState.value.items.size - 1) {
            val newPosition = _uiState.value.currentPosition + 1
            _uiState.update { it.copy(currentPosition = newPosition, currentItem = getCurrentItem(newPosition)) }
        }
    }

    private fun getCurrentItem(position: Int, items: List<ModuleItemUiState> = _uiState.value.items): ModuleItemUiState? {
        return items.getOrNull(position)
    }

    private fun progressClicked() {
        val currentModuleItemId = _uiState.value.currentItem?.moduleItemId ?: -1L
        val progressPosition = getProgressPosition(currentModuleItemId)
        _uiState.update {
            it.copy(
                progressScreenState = it.progressScreenState.copy(
                    visible = true,
                    currentPosition = progressPosition,
                    selectedModuleItemId = currentModuleItemId
                )
            )
        }
    }

    private fun getProgressPosition(
        moduleItemId: Long,
        progressPages: List<ProgressPageUiState> = _uiState.value.progressScreenState.pages
    ): Int {
        val moduleId = moduleItems.find { moduleItemId == it.id }?.moduleId
        val position = progressPages.indexOfFirst { it.moduleId == moduleId }
        return if (position != -1) position else 0
    }

    //region Progress Screen callbacks
    private fun progressCloseClicked() {
        _uiState.update { it.copy(progressScreenState = it.progressScreenState.copy(visible = false)) }
    }

    private fun progressPreviousClicked() {
        if (_uiState.value.progressScreenState.currentPosition > 0) {
            val newPosition = _uiState.value.progressScreenState.currentPosition - 1
            _uiState.update { it.copy(progressScreenState = it.progressScreenState.copy(currentPosition = newPosition)) }
        }
    }

    private fun progressNextClicked() {
        if (_uiState.value.progressScreenState.currentPosition < _uiState.value.progressScreenState.pages.size - 1) {
            val newPosition = _uiState.value.progressScreenState.currentPosition + 1
            _uiState.update { it.copy(progressScreenState = it.progressScreenState.copy(currentPosition = newPosition)) }
        }
    }

    private fun moduleItemSelected(itemId: Long) {
        val moduleItem = moduleItems.find { it.id == itemId }
        if (moduleItem != null) {
            val newPosition = _uiState.value.items.indexOfFirst { it.moduleItemId == itemId }
            _uiState.update {
                it.copy(
                    currentPosition = newPosition,
                    currentItem = getCurrentItem(newPosition),
                    progressScreenState = it.progressScreenState.copy(visible = false)
                )
            }
        }
    }
    //endregion
}