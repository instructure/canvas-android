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
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItem.Type
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.isLocked
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.database.entity.EntitySyncType
import com.instructure.horizon.domain.usecase.GetAssignmentDetailsUseCase
import com.instructure.horizon.domain.usecase.GetEntityLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemSequenceUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemUseCase
import com.instructure.horizon.domain.usecase.GetModulesWithItemsUseCase
import com.instructure.horizon.domain.usecase.HasUnreadCommentsUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsDoneUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsNotDoneUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsReadUseCase
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContextSource
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressPageItem
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressPageUiState
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressScreenUiState
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardStateMapper
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.horizon.offline.HorizonOfflineState
import androidx.lifecycle.ViewModel
import com.instructure.horizon.util.formatScore
import com.instructure.pandautils.utils.formatIsoDuration
import com.instructure.pandautils.utils.localisedFormatMonthDay
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModuleItemSequenceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getModuleItemSequenceUseCase: GetModuleItemSequenceUseCase,
    private val getModulesWithItemsUseCase: GetModulesWithItemsUseCase,
    private val getModuleItemUseCase: GetModuleItemUseCase,
    private val markModuleItemAsDoneUseCase: MarkModuleItemAsDoneUseCase,
    private val markModuleItemAsNotDoneUseCase: MarkModuleItemAsNotDoneUseCase,
    private val markModuleItemAsReadUseCase: MarkModuleItemAsReadUseCase,
    private val hasUnreadCommentsUseCase: HasUnreadCommentsUseCase,
    private val getAssignmentDetailsUseCase: GetAssignmentDetailsUseCase,
    private val getEntityLastSyncedAtUseCase: GetEntityLastSyncedAtUseCase,
    private val moduleItemCardStateMapper: ModuleItemCardStateMapper,
    private val aiAssistContextProvider: AiAssistContextProvider,
    private val dashboardEventHandler: DashboardEventHandler,
    private val learnEventHandler: LearnEventHandler,
    savedStateHandle: SavedStateHandle,
    private val offlineState: HorizonOfflineState,
) : ViewModel() {
    private val courseId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().courseId
    private val moduleItemId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemId
    private val moduleItemAssetType = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemAssetType
    private val moduleItemAssetId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().moduleItemAssetId
    private val scrollToNoteId = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().scrollToNoteId
    private val showMyProgressButton = savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>().showMyProgressButton

    private var courseProgressChanged = false

    private val _uiState =
        MutableStateFlow(
            ModuleItemSequenceUiState(
                courseId = courseId,
                scrollToNoteId = scrollToNoteId,
                showMyProgressButton = showMyProgressButton,
                loadingState = LoadingState(onRefresh = ::refresh),
                onPreviousClick = ::previousClicked,
                onNextClick = ::nextClicked,
                onProgressClick = ::progressClicked,
                progressScreenState = ProgressScreenUiState(
                    onCloseClick = ::progressCloseClicked,
                    onPreviousClick = ::progressPreviousClicked,
                    onNextClick = ::progressNextClicked,
                ),
                onAssignmentToolsClick = ::onAssignmentToolsClicked,
                assignmentToolsOpened = ::assignmentToolsOpened,
                updateShowAiAssist = ::updateShowAiAssist,
                updateObjectTypeAndId = ::updateNotebookObjectTypeAndId,
                updateAiAssistContext = ::updateAiAssistContext,
            )
        )
    val uiState = _uiState.asStateFlow()

    private var modules = emptyList<ModuleObject>()
    private var moduleItems = emptyList<ModuleItem>()

    private var currentModuleItem: ModuleItem? = null

    init {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = true))
            }
            loadData()
            if (offlineState.isOffline.value) {
                applyOfflineState()
            }
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true))
            }
        }

        offlineState.isOffline
            .drop(1)
            .onEach { isOffline ->
                if (isOffline) applyOfflineState()
                else _uiState.update { it.copy(isOffline = false, lastSyncedAtMs = null) }
            }
            .launchIn(viewModelScope)
    }

    private fun applyOfflineState() {
        viewModelScope.tryLaunch {
            val currentModuleItemId = _uiState.value.currentItem?.moduleItemId
            val lastSyncedAt = if (currentModuleItemId != null) {
                getEntityLastSyncedAtUseCase(EntitySyncType.MODULE_ITEM, currentModuleItemId)
            } else null
            val updatedItems = _uiState.value.items.map { item ->
                val itemSyncedAt = getEntityLastSyncedAtUseCase(EntitySyncType.MODULE_ITEM, item.moduleItemId)
                item.copy(isAvailableOffline = itemSyncedAt != null)
            }
            _uiState.update { state ->
                state.copy(
                    isOffline = true,
                    lastSyncedAtMs = lastSyncedAt,
                    items = updatedItems,
                    currentItem = updatedItems.getOrNull(state.currentPosition),
                    aiAssistButtonEnabled = false,
                    notebookButtonEnabled = false,
                )
            }
        } catch { }
    }

    private suspend fun loadData() {
        val moduleItemId = if (moduleItemId != null) {
            moduleItemId
        } else {
            val assetType = moduleItemAssetType
                ?: throw IllegalArgumentException("Module item sequence shouldn't be opened without moduleItemId or moduleItemAsset")
            val assetId = moduleItemAssetId
                ?: throw IllegalArgumentException("Module item sequence shouldn't be opened without moduleItemId or moduleItemAsset")
            val moduleItemSequence = getModuleItemSequenceUseCase(
                GetModuleItemSequenceUseCase.Params(courseId, assetType, assetId)
            )
            moduleItemSequence.items?.firstOrNull()?.current?.id ?: -1L
        }

        modules = getModulesWithItemsUseCase(GetModulesWithItemsUseCase.Params(courseId, forceRefresh = true))
        moduleItems = modules.flatMap { it.items }

        currentModuleItem = moduleItems.find { it.id == moduleItemId }

        val assignment = getAssignment(currentModuleItem, forceNetwork = true)
        val attempts = getAttemptCount(assignment)
        val hasUnreadComments = hasUnreadCommentsUseCase(HasUnreadCommentsUseCase.Params(assignment?.id, forceNetwork = true))

        val items = modules
            .flatMap { it.items }.mapNotNull {
                val isCurrent = it.id == moduleItemId
                val currentAttempts = if (isCurrent) attempts else null
                val currentAssignment = if (isCurrent) assignment else null
                createModuleItemUiState(it, modules, currentAttempts, currentAssignment)
            }

        val progressPages = modules.map { createProgressPage(it) }

        val initialPosition = items.indexOfFirst { it.moduleItemId == moduleItemId }.coerceAtLeast(0)

        currentModuleItem?.let {
            markItemAsRead(it)
        }

        _uiState.update {
            it.copy(
                items = items,
                currentPosition = initialPosition,
                currentItem = getCurrentItem(initialPosition, items),
                progressScreenState = it.progressScreenState.copy(
                    pages = progressPages,
                    currentPosition = getProgressPosition(moduleItemId, progressPages)
                ),
                hasUnreadComments = hasUnreadComments
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

    private fun createModuleItemUiState(
        item: ModuleItem,
        modules: List<ModuleObject>,
        attempts: String?,
        assignment: Assignment?
    ): ModuleItemUiState? {
        val moduleItemContent: ModuleItemContent? = when {
            item.isLocked() -> ModuleItemContent.Locked(
                item.moduleDetails?.lockExplanation ?: context.getString(R.string.modulePager_locked)
            )

            item.type == Type.Page.name -> ModuleItemContent.Page(courseId, item.pageUrl.orEmpty())
            item.type == Type.Assignment.name -> {
                val completed = item.completionRequirement?.completed ?: false
                val mustSubmit = item.completionRequirement?.type == ModuleItem.MUST_SUBMIT
                if (item.quizLti) {
                    ModuleItemContent.Assessment(courseId, item.contentId) {
                        if (mustSubmit && !completed) {
                            courseProgressChanged()
                        }
                    }
                } else {
                    ModuleItemContent.Assignment(courseId, item.contentId) {
                        if (mustSubmit && !completed) {
                            courseProgressChanged()
                        }
                    }
                }
            }

            item.type == Type.Quiz.name -> ModuleItemContent.Assessment(courseId, item.contentId)
            item.type == Type.ExternalUrl.name -> ModuleItemContent.ExternalLink(item.title.orEmpty(), item.externalUrl.orEmpty())
            item.type == Type.ExternalTool.name -> ModuleItemContent.ExternalTool(
                courseId,
                item.htmlUrl.orEmpty(),
                item.externalUrl.orEmpty()
            )

            item.type == Type.File.name -> ModuleItemContent.File(courseId, item.moduleId, item.id, item.url.orEmpty())
            else -> null
        }

        if (moduleItemContent == null) {
            return null
        }

        val completionRequirement = item.completionRequirement
        val markDoneUiState =
            if (completionRequirement != null && ModuleItem.MUST_MARK_DONE == completionRequirement.type && !item.isLocked()) {
                MarkAsDoneUiState(isDone = completionRequirement.completed, onMarkAsDoneClick = {
                    markItemAsDone(item)
                }, onMarkAsNotDoneClick = {
                    markItemAsNotDone(item)
                })
            } else null

        return ModuleItemUiState(
            moduleName = modules.find { item.moduleId == it.id }?.name.orEmpty(),
            moduleItemName = item.title.orEmpty(),
            moduleItemId = item.id,
            moduleItemContent = moduleItemContent,
            detailTags = createDetailTags(item, attempts, assignment),
            pillText = createPillText(item),
            markAsDoneUiState = markDoneUiState
        )
    }

    private fun createDetailTags(item: ModuleItem, attempts: String?, assignment: Assignment?): List<String> {
        val detailTags = mutableListOf<String>()
        item.estimatedDuration?.let {
            detailTags.add(it.formatIsoDuration(context))
        }
        item.moduleDetails?.dueDate?.let {
            detailTags.add(context.getString(R.string.modulePager_dueDate, it.localisedFormatMonthDay()))
        }
        if (assignment != null && assignment.isGraded()) {
            detailTags.add(
                context.getString(
                    R.string.attemptCard_score,
                    formatScore(assignment.submission?.score.orDefault()),
                    formatScore(assignment.pointsPossible)
                )
            )
        } else {
            item.moduleDetails?.pointsPossible?.let {
                val points = it.toDoubleOrNull()?.toInt() ?: 0
                detailTags.add(context.resources.getQuantityString(R.plurals.modulePager_pointsPossible, points, points))
            }
        }
        attempts?.let {
            detailTags.add(it)
        }
        return detailTags
    }

    private fun createPillText(item: ModuleItem): String? {
        return if (item.overDue) context.getString(R.string.modulePager_overdue) else null
    }

    private fun previousClicked() {
        if (_uiState.value.currentPosition > 0) {
            val newPosition = _uiState.value.currentPosition - 1
            pagePositionChanged(newPosition)
        }
    }

    private fun nextClicked() {
        if (_uiState.value.currentPosition < _uiState.value.items.size - 1) {
            val newPosition = _uiState.value.currentPosition + 1
            pagePositionChanged(newPosition)
        }
    }

    private fun pagePositionChanged(newPosition: Int) {
        val newItems = _uiState.value.items.map {
            it.copy(isLoading = it.moduleItemId == _uiState.value.items[newPosition].moduleItemId)
        }
        val currentItem = getCurrentItem(newPosition, newItems)
        _uiState.update { it.copy(currentPosition = newPosition, currentItem = currentItem, items = newItems) }
        currentModuleItem = moduleItems.find { it.id == currentItem?.moduleItemId }
        loadModuleItem(newPosition, currentItem!!.moduleItemId)
    }

    private fun loadModuleItem(position: Int, moduleItemId: Long) {
        _uiState.update {
            it.copy(
                loadingState = it.loadingState.copy(isError = false),
                notebookButtonEnabled = false,
                aiAssistButtonEnabled = false
            )
        }

        viewModelScope.tryLaunch {
            val moduleItem = getModuleItemUseCase(
                GetModuleItemUseCase.Params(
                    courseId = courseId,
                    moduleId = moduleItems.find { it.id == moduleItemId }?.moduleId.orDefault(),
                    moduleItemId = moduleItemId,
                )
            )

            val assignment = getAssignment(currentModuleItem, forceNetwork = true)
            val attempts = getAttemptCount(assignment)
            val hasUnreadComments = hasUnreadCommentsUseCase(HasUnreadCommentsUseCase.Params(assignment?.id, forceNetwork = true))

            markItemAsRead(moduleItem)
            val newItems = _uiState.value.items.mapNotNull {
                if (it.moduleItemId == _uiState.value.items[position].moduleItemId) createModuleItemUiState(
                    moduleItem,
                    modules,
                    attempts,
                    assignment
                ) else it
            }
            val currentItem = getCurrentItem(items = newItems)
            val lastSyncedAtMs = if (_uiState.value.isOffline) {
                getEntityLastSyncedAtUseCase(EntitySyncType.MODULE_ITEM, moduleItemId)
            } else _uiState.value.lastSyncedAtMs
            _uiState.update {
                it.copy(
                    items = newItems,
                    currentItem = currentItem,
                    hasUnreadComments = hasUnreadComments,
                    lastSyncedAtMs = lastSyncedAtMs,
                )
            }
        } catch {
            // TODO Handle error
            val currentItem = getCurrentItem()
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isError = true), currentItem = currentItem)
            }
        }
    }

    private fun getCurrentItem(
        position: Int = _uiState.value.currentPosition,
        items: List<ModuleItemUiState> = _uiState.value.items
    ): ModuleItemUiState? {
        return items.getOrNull(position)
    }

    private fun progressClicked() {
        val currentModuleItemId = _uiState.value.currentItem?.moduleItemId ?: -1L
        val progressPosition = getProgressPosition(currentModuleItemId)
        _uiState.update {
            it.copy(
                loadingState = it.loadingState.copy(isLoading = courseProgressChanged),
                progressScreenState = it.progressScreenState.copy(
                    visible = true,
                    currentPosition = progressPosition,
                    movingDirection = 0,
                    selectedModuleItemId = currentModuleItemId
                ),
            )
        }
        if (courseProgressChanged) {
            reloadData()
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

    private fun refresh() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
        reloadData()
    }

    private fun reloadData() {
        viewModelScope.tryLaunch {
            modules = getModulesWithItemsUseCase(GetModulesWithItemsUseCase.Params(courseId, forceRefresh = true))
            moduleItems = modules.flatMap { it.items }

            val assignment = getAssignment(currentModuleItem, forceNetwork = true)
            val attempts = getAttemptCount(assignment)
            val hasUnreadComments = hasUnreadCommentsUseCase(HasUnreadCommentsUseCase.Params(assignment?.id, forceNetwork = true))

            val currentReloadModuleItemId = currentModuleItem?.id
            val items = modules
                .flatMap { it.items }.mapNotNull {
                    val isCurrent = it.id == currentReloadModuleItemId
                    val currentAttempts = if (isCurrent) attempts else null
                    val currentAssignment = if (isCurrent) assignment else null
                    createModuleItemUiState(it, modules, currentAttempts, currentAssignment)
                }

            val progressPages = modules.map { createProgressPage(it) }

            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false, isRefreshing = false),
                    items = items,
                    currentPosition = _uiState.value.currentPosition,
                    currentItem = getCurrentItem(_uiState.value.currentPosition, items),
                    progressScreenState = it.progressScreenState.copy(
                        pages = progressPages
                    ),
                    hasUnreadComments = hasUnreadComments
                )
            }

            courseProgressChanged = false
        } catch {
            // TODO Handle error
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false, snackbarMessage = "Failed to reload items"))
            }
        }
    }

    //region Progress Screen callbacks
    private fun progressCloseClicked() {
        _uiState.update { it.copy(progressScreenState = it.progressScreenState.copy(visible = false)) }
    }

    private fun progressPreviousClicked() {
        if (_uiState.value.progressScreenState.currentPosition > 0) {
            val newPosition = _uiState.value.progressScreenState.currentPosition - 1
            _uiState.update {
                it.copy(
                    progressScreenState = it.progressScreenState.copy(
                        currentPosition = newPosition,
                        movingDirection = -1
                    )
                )
            }
        }
    }

    private fun progressNextClicked() {
        if (_uiState.value.progressScreenState.currentPosition < _uiState.value.progressScreenState.pages.size - 1) {
            val newPosition = _uiState.value.progressScreenState.currentPosition + 1
            _uiState.update {
                it.copy(
                    progressScreenState = it.progressScreenState.copy(
                        currentPosition = newPosition,
                        movingDirection = 1
                    )
                )
            }
        }
    }

    private fun moduleItemSelected(itemId: Long) {
        val moduleItem = moduleItems.find { it.id == itemId }
        currentModuleItem = moduleItem
        if (moduleItem != null) {
            val newPosition = _uiState.value.items.indexOfFirst { it.moduleItemId == itemId }
            _uiState.update {
                it.copy(
                    currentPosition = newPosition,
                    currentItem = getCurrentItem(newPosition),
                    progressScreenState = it.progressScreenState.copy(visible = false)
                )
            }
            loadModuleItem(newPosition, itemId)
        }
    }
    //endregion

    private fun markItemAsDone(item: ModuleItem) {
        changeDoneState(item, true)
    }

    private fun markItemAsNotDone(item: ModuleItem) {
        changeDoneState(item, false)
    }

    private fun changeDoneState(item: ModuleItem, markDone: Boolean) {
        viewModelScope.launch {
            updateMarkAsDoneStateForItem(item, loading = true)
            val result = if (markDone) {
                markModuleItemAsDoneUseCase(MarkModuleItemAsDoneUseCase.Params(courseId, item))
            } else {
                markModuleItemAsNotDoneUseCase(MarkModuleItemAsNotDoneUseCase.Params(courseId, item))
            }
            if (result.isSuccess) {
                updateMarkAsDoneStateForItem(item, loading = false, done = markDone)
                courseProgressChanged()
            } else {
                updateMarkAsDoneStateForItem(item, loading = false)
            }
        }
    }

    private fun updateMarkAsDoneStateForItem(item: ModuleItem, loading: Boolean, done: Boolean? = null) {
        val currentItem = getCurrentItem()
        val updatedCurrentItem = if (currentItem?.moduleItemId == item.id) {
            currentItem.copy(
                markAsDoneUiState = currentItem.markAsDoneUiState?.copy(
                    isLoading = loading,
                    isDone = done ?: currentItem.markAsDoneUiState.isDone
                )
            )
        } else currentItem
        _uiState.update {
            it.copy(
                items = it.items.map { moduleItemUiState ->
                    if (moduleItemUiState.moduleItemId == item.id) {
                        moduleItemUiState.copy(
                            markAsDoneUiState = moduleItemUiState.markAsDoneUiState?.copy(
                                isLoading = loading,
                                isDone = done ?: moduleItemUiState.markAsDoneUiState.isDone
                            )
                        )
                    } else {
                        moduleItemUiState
                    }
                },
                currentItem = updatedCurrentItem,
            )
        }
    }

    private fun markItemAsRead(item: ModuleItem) {
        val completionRequirement = item.completionRequirement
        if (completionRequirement?.type == ModuleItem.MUST_VIEW && !completionRequirement.completed && !item.isLocked()) {
            viewModelScope.launch {
                markModuleItemAsReadUseCase(MarkModuleItemAsReadUseCase.Params(courseId, item.moduleId, item.id))
                courseProgressChanged()
            }
        }
    }

    private suspend fun getAssignment(item: ModuleItem?, forceNetwork: Boolean): Assignment? {
        if (item?.type != Type.Assignment.name) return null

        return getAssignmentDetailsUseCase(GetAssignmentDetailsUseCase.Params(courseId, item.contentId, forceNetwork))
    }

    private fun getAttemptCount(assignment: Assignment?): String? {
        if (assignment == null) return null

        return if (assignment.allowedAttempts > 0) {
            context.resources.getQuantityString(
                R.plurals.modulePager_numberOfAttemtps,
                assignment.allowedAttempts.toInt(),
                assignment.allowedAttempts
            )
        } else {
            context.getString(R.string.modulePager_unlimitedAttempts)
        }
    }

    private fun onAssignmentToolsClicked() {
        currentModuleItem?.contentId?.let { contentId ->
            _uiState.update { it.copy(showAssignmentToolsForId = contentId) }
        }
    }

    private fun assignmentToolsOpened() {
        _uiState.update { it.copy(showAssignmentToolsForId = null) }
    }

    private fun updateShowAiAssist(show: Boolean) {
        _uiState.update { it.copy(showAiAssist = show) }
    }

    private fun updateNotebookObjectTypeAndId(objectTypeAndId: Pair<String, String>) {
        _uiState.update {
            it.copy(
                objectTypeAndId = objectTypeAndId,
                notebookButtonEnabled = true
            )
        }
    }

    private fun updateAiAssistContext(source: AiAssistContextSource, content: String) {
        aiAssistContextProvider.aiAssistContext = AiAssistContext(
            contextSources = listOf(
                AiAssistContextSource.Course(courseId.toString()),
                AiAssistContextSource.Module(currentModuleItem?.moduleId.toString()),
                AiAssistContextSource.ModuleItem(currentModuleItem?.id.toString()),
                source
            ),
            contextString = content
        )

        _uiState.update {
            it.copy(
                aiAssistButtonEnabled = true
            )
        }
    }

    private fun courseProgressChanged() {
        courseProgressChanged = true
        viewModelScope.launch {
            dashboardEventHandler.postEvent(DashboardEvent.ProgressRefresh)
            learnEventHandler.postEvent(LearnEvent.RefreshCourseProgress)
        }
    }
}