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
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ModuleItemSequenceUiState(onPreviousClick = ::previousClicked, onNextClick = ::nextClicked))
    val uiState = _uiState.asStateFlow()

    private val courseId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().courseId
    private val moduleItemId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemId

    init {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            loadData()
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private suspend fun loadData() {
        val moduleItemId = if (moduleItemId != null) {
            moduleItemId
        } else {
            // TODO Handle case when coming from a learning object link, not in the scope of this ticket, we always navigate from module items
            //val moduleItemSequence = repository.getModuleItemSequence(courseId, moduleItemId ?: 0L)
            //moduleItemSequence.items?.firstOrNull()?.id
            -1L
        }

        val modules = repository.getModulesWithItems(courseId)

        val items = modules
            .flatMap { it.items }.mapNotNull { createModuleItemUiState(it, modules) }

        val initialPosition = items.indexOfFirst { it.moduleItemId == moduleItemId }.coerceAtLeast(0)

        _uiState.update {
            it.copy(items = items, currentPosition = initialPosition, currentItem = getCurrentItem(initialPosition, items))
        }
    }

    private fun createModuleItemUiState(item: ModuleItem, modules: List<ModuleObject>): ModuleItemUiState? {
        val moduleItemContent: ModuleItemContent? = when (item.type) {
            Type.Page.name -> ModuleItemContent.Page(item.pageUrl.orEmpty())
            Type.Assignment.name -> ModuleItemContent.Assignment(item.contentId)
            Type.Quiz.name -> ModuleItemContent.Assessment(item.contentId)
            Type.ExternalUrl.name -> ModuleItemContent.ExternalLink(item.htmlUrl.orEmpty())
            Type.ExternalTool.name -> ModuleItemContent.ExternalTool(item.htmlUrl.orEmpty())
            Type.File.name -> ModuleItemContent.File(item.url.orEmpty())
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
}