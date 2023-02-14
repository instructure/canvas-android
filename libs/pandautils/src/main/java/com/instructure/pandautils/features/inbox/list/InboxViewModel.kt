/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.list.itemviewmodels.InboxEntryItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val inboxRepository: InboxRepository,
    private val resources: Resources,
    private val inboxEntryItemCreator: InboxEntryItemCreator
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<InboxViewData>
        get() = _data
    private val _data = MutableLiveData(InboxViewData(getTextForScope(InboxApi.Scope.INBOX), filterText = resources.getString(R.string.allCourses)))

    val itemViewModels: LiveData<List<InboxEntryItemViewModel>>
        get() = _itemViewModels
    private val _itemViewModels = MutableLiveData<List<InboxEntryItemViewModel>>(emptyList())

    val events: LiveData<Event<InboxAction>>
        get() = _events
    private val _events = MutableLiveData<Event<InboxAction>>()

    private var scope = InboxApi.Scope.INBOX

    private var contextFilter: CanvasContext? = null

    private var nextPageLink: String? = null

    private var canvasContexts: List<CanvasContext> = emptyList()

    private var lastRequestedPageLink: String? = null
    private var silentRefreshJob: Job? = null

    val bottomReachedCallback: () -> Unit = {
        viewModelScope.launch {
            if (_state.value?.isInLoadingState() != true  && nextPageLink != null) {
                loadNextPage()
            }
        }
    }

    private suspend fun loadNextPage() {
        _state.postValue(ViewState.LoadingNextPage)
        val dataResult = inboxRepository.getConversations(scope, true, contextFilter, nextPageLink)
        if (dataResult is DataResult.Success) {
            lastRequestedPageLink = nextPageLink
            nextPageLink = dataResult.linkHeaders.nextUrl
            val conversations = dataResult.data
            val itemViewModels = createInboxEntriesFromResponse(conversations)
            _itemViewModels.postValue(_itemViewModels.value?.plus(itemViewModels))
        } else {
            _events.postValue(Event(InboxAction.FailedToLoadNextPage))
        }
        _state.postValue(ViewState.Success) // We always need to finish with success state because we already have data and we send only an error event.
    }

    init {
        _state.postValue(ViewState.Loading)
        fetchData()
    }

    private fun fetchData(forceNetwork: Boolean = false, refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                silentRefreshJob?.cancel()
                exitSelectionMode()
                lastRequestedPageLink = null
                val dataResult = inboxRepository.getConversations(scope, forceNetwork, contextFilter)
                val conversations = dataResult.dataOrThrow
                if (dataResult is DataResult.Success) {
                    // Throw an error if the refreshed data comes from cache, this can happen when there is something with the network.
                    // Refresh should always give fresh data
                    if (refresh && dataResult.apiType == ApiType.CACHE) throw IllegalStateException()
                    nextPageLink = dataResult.linkHeaders.nextUrl
                }

                val itemViewModels = createInboxEntriesFromResponse(conversations)
                _itemViewModels.postValue(itemViewModels)
                if (itemViewModels.isEmpty()) {
                    postEmptyState()
                } else {
                    _state.postValue(ViewState.Success)
                }

                if (forceNetwork) _events.postValue(Event(InboxAction.UpdateUnreadCount))

                inboxRepository.getCanvasContexts().dataOrNull?.let {
                    canvasContexts = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_itemViewModels.value?.isNotEmpty() == true) {
                    _events.postValue(Event(InboxAction.RefreshFailed))
                    _state.postValue(ViewState.Success)
                } else {
                    _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
                }
            }
        }
    }

    private fun createInboxEntriesFromResponse(conversations: List<Conversation>): List<InboxEntryItemViewModel> {
        return conversations.map {
            createItemViewModelFromConversation(it)
        }
    }

    private fun createItemViewModelFromConversation(conversation: Conversation): InboxEntryItemViewModel {
        return inboxEntryItemCreator.createInboxEntryItem(
            conversation,
            openConversationCallback = { starred ->
                _events.value = Event(InboxAction.OpenConversation(conversation.copy(isStarred = starred), scope))
            },
            selectionModeCallback = { view, selected ->
                _events.postValue(Event(InboxAction.ItemSelectionChanged(view, selected)))
                handleSelectionMode()
            },
            avatarClickedCallback = { starred ->
                _events.value = Event(InboxAction.AvatarClickedCallback(conversation.copy(isStarred = starred), scope))
            })
    }

    private fun handleSelectionMode() {
        val items = _itemViewModels.value ?: emptyList()
        val selectedItems = items.filter { it.selected }
        val selectedItemsCount = selectedItems.size
        val selectionModeActive = selectedItemsCount > 0
        items.forEach { it.selectionModeActive = selectionModeActive }

        val menuItems = createMenuItems(selectedItems, scope)

        _data.value = _data.value?.copy(selectedItemsCount = selectedItemsCount.toString(), selectionMode = selectionModeActive, editMenuItems = menuItems)
    }

    private fun createMenuItems(selectedItems: List<InboxEntryItemViewModel>, scope: InboxApi.Scope): Set<InboxMenuItem> {
        val menuItems = mutableSetOf<InboxMenuItem>(InboxMenuItem.DELETE)
        when {
            scope == InboxApi.Scope.ARCHIVED -> menuItems.add(InboxMenuItem.UNARCHIVE)
            scope != InboxApi.Scope.SENT -> menuItems.add(InboxMenuItem.ARCHIVE)
        }

        if (selectedItems.any { it.data.unread }) {
            menuItems.add(InboxMenuItem.MARK_AS_READ)
        } else {
            menuItems.add(InboxMenuItem.MARK_AS_UNREAD)
        }

        if (selectedItems.any { !it.data.starred }) {
            menuItems.add(InboxMenuItem.STAR)
        } else {
            menuItems.add(InboxMenuItem.UNSTAR)
        }

        return menuItems
    }

    private fun getTextForScope(scope: InboxApi.Scope): String {
        return when (scope) {
            InboxApi.Scope.INBOX -> resources.getString(R.string.inboxScopeInbox)
            InboxApi.Scope.UNREAD -> resources.getString(R.string.inbox_unread)
            InboxApi.Scope.STARRED -> resources.getString(R.string.inbox_starred)
            InboxApi.Scope.SENT -> resources.getString(R.string.inbox_sent)
            InboxApi.Scope.ARCHIVED -> resources.getString(R.string.inbox_archived)
            else -> resources.getString(R.string.inboxScopeInbox)
        }
    }

    private fun postEmptyState() {
        val emptyState = when (scope) {
            InboxApi.Scope.INBOX -> ViewState.Empty(R.string.nothingUnread, R.string.nothingUnreadSubtext, R.drawable.ic_panda_inboxzero)
            InboxApi.Scope.UNREAD -> ViewState.Empty(R.string.nothingUnread, R.string.nothingUnreadSubtext, R.drawable.ic_panda_inboxzero)
            InboxApi.Scope.ARCHIVED -> ViewState.Empty(R.string.nothingArchived, R.string.nothingArchivedSubtext, R.drawable.ic_panda_inboxarchived)
            InboxApi.Scope.STARRED -> ViewState.Empty(R.string.nothingStarred, R.string.nothingStarredSubtext, R.drawable.ic_panda_inboxstarred)
            InboxApi.Scope.SENT -> ViewState.Empty(R.string.nothingSent, R.string.nothingSentSubtext, R.drawable.ic_panda_inboxsent)
        }

        _state.postValue(emptyState)
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        fetchData(true, true)
    }

    fun openScopeSelector() {
        _events.postValue(Event(InboxAction.OpenScopeSelector))
    }

    fun scopeChanged(newScope: InboxApi.Scope) {
        if (newScope != scope) {
            scope = newScope
            _state.postValue(ViewState.Loading)
            _data.value = _data.value?.copy(scope = getTextForScope(scope))
            _itemViewModels.postValue(emptyList())
            fetchData()
        }
    }

    fun starSelected() {
        performBatchOperation("star") { ids, _ ->
            _itemViewModels.value?.forEach {
                if (ids.contains(it.data.id)) it.data = it.data.copy(starred = true)
                it.notifyChange()
                _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxStarredConfirmation, ids.size)))
            }
        }
    }

    fun unstarSelected() {
        performBatchOperation("unstar") { ids, progress ->
            if (scope == InboxApi.Scope.STARRED) {
                removeItemsAndSilentUpdate(ids, progress)
            } else {
                _itemViewModels.value?.forEach {
                    if (ids.contains(it.data.id)) it.data = it.data.copy(starred = false)
                    it.notifyChange()
                    _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxUnstarredConfirmation, ids.size)))
                }
            }
        }
    }

    fun markAsReadSelected() {
        performBatchOperation("mark_as_read") { ids, progress ->
            if (scope == InboxApi.Scope.UNREAD) {
                removeItemsAndSilentUpdate(ids, progress)
            } else {
                _itemViewModels.value?.forEach {
                    if (ids.contains(it.data.id)) {
                        it.data = it.data.copy(unread = false)
                        it.notifyChange()
                    }
                }
                _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxMarkAsReadConfirmation, ids.size)))
                _events.value = Event(InboxAction.UpdateUnreadCount)
            }
        }
    }

    fun markAsUnreadSelected() {
        performBatchOperation("mark_as_unread") { ids, progress ->
            _itemViewModels.value?.forEach {
                if (ids.contains(it.data.id)) {
                    it.data = it.data.copy(unread = true)
                    it.notifyChange()
                }
            }
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxMarkAsUnreadConfirmation, ids.size)))
            _events.value = Event(InboxAction.UpdateUnreadCount)
        }
    }

    fun deleteSelected() {
        performBatchOperation("destroy") { ids, progress ->
            removeItemsAndSilentUpdate(ids, progress)
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxDeletedConfirmation, ids.size)))
            _events.value = Event(InboxAction.UpdateUnreadCount)
        }
    }

    fun archiveSelected() {
        performBatchOperation("archive") { ids, progress ->
            if (scope != InboxApi.Scope.STARRED) {
                removeItemsAndSilentUpdate(ids, progress)
            }
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxArchivedConfirmation, ids.size)))
            _events.value = Event(InboxAction.UpdateUnreadCount)
        }
    }

    fun unarchiveSelected() {
        performBatchOperation("mark_as_read") { ids, progress ->
            removeItemsAndSilentUpdate(ids, progress)
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxUnarchivedConfirmation, ids.size)))
        }
    }

    private fun performBatchOperation(operation: String, onSuccess: (Set<Long>, Progress) -> Unit) {
        viewModelScope.launch {
            try {
                val ids = _itemViewModels.value
                    ?.filter { it.selected }
                    ?.map { it.data.id } ?: emptyList()

                val dataResult = inboxRepository.batchUpdateConversations(ids, operation)
                if (dataResult.isSuccess) {
                    inboxRepository.invalidateCachedResponses()
                    onSuccess(ids.toSet(), dataResult.dataOrThrow)
                    handleSelectionMode()
                } else {
                    _events.postValue(Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxOperationFailed))))
                }
            } catch (e: Exception) {
                _events.postValue(Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxOperationFailed))))
            }
        }
    }

    private fun removeItemsAndSilentUpdate(ids: Set<Long>, progress: Progress) {
        val newMessages = _itemViewModels.value?.filterNot { ids.contains(it.data.id) } ?: emptyList()
        _itemViewModels.value = newMessages
        pollProgress(progress)
    }

    fun handleBackPressed(): Boolean {
        if (_data.value?.selectionMode == true) {
            exitSelectionMode()
            return true
        } else {
            return false
        }
    }

    private fun exitSelectionMode() {
        _itemViewModels.value?.forEach {
            it.selected = false
            it.selectionModeActive = false
            it.notifyChange()
        }
        _data.value = _data.value?.copy(selectedItemsCount = "", selectionMode = false)
    }

    fun invalidateCache() {
        inboxRepository.invalidateCachedResponses()
    }

    fun createNewMessage() {
        _events.postValue(Event(InboxAction.CreateNewMessage))
    }

    fun coursesFilterClicked() {
        if (canvasContexts.isNullOrEmpty()) {
            viewModelScope.launch {
                inboxRepository.getCanvasContexts().dataOrNull?.let {
                    canvasContexts = it
                }
                _events.postValue(Event(InboxAction.OpenContextFilterSelector(canvasContexts)))
            }
        } else {
            _events.postValue(Event(InboxAction.OpenContextFilterSelector(canvasContexts)))
        }
    }

    fun canvasContextFilterSelected(id: Long) {
        if (id == contextFilter?.id) return

        contextFilter = canvasContexts.find { it.id == id }
        filterChanged(contextFilter?.name ?: "")
    }

    fun allCoursesSelected() {
        if (contextFilter == null) return

        contextFilter = null
        filterChanged(resources.getString(R.string.allCourses))
    }

    private fun filterChanged(filterTitle: String) {
        _data.value = _data.value?.copy(filterText = filterTitle)
        _state.postValue(ViewState.Loading)
        _itemViewModels.postValue(emptyList())
        fetchData()
    }

    private fun pollProgress(progress: Progress) {
        silentRefreshJob = viewModelScope.launch {
            val progressResult = inboxRepository.pollProgress(progress)
            if (progressResult.isSuccess && (progressResult as DataResult.Success).data.isCompleted) {
                silentRefresh()
            } else {
                // The data we are showing is not valid so we should refresh as a fallback
                refresh()
            }
        }
    }

    private suspend fun silentRefresh() {
        _state.postValue(ViewState.LoadingNextPage)
        val dataResult = inboxRepository.getConversations(scope, true, contextFilter, lastRequestedPageLink)

        if (dataResult is DataResult.Success) {
            nextPageLink = dataResult.linkHeaders.nextUrl
            val alreadyFetchedIds = _itemViewModels.value?.map { it.data.id } ?: emptyList()
            val newConversations = dataResult.data.filter { !alreadyFetchedIds.contains(it.id) }
            val itemViewModels = createInboxEntriesFromResponse(newConversations)
            _itemViewModels.value = _itemViewModels.value?.plus(itemViewModels)
        } else {
            _events.postValue(Event(InboxAction.FailedToLoadNextPage))
        }

        if (_itemViewModels.value.isNullOrEmpty()) {
            postEmptyState()
        } else {
            _state.postValue(ViewState.Success)
        }
    }

    fun confirmDelete() {
        val selectedCount = _itemViewModels.value?.count { it.selected } ?: 1
        _events.value = Event(InboxAction.ConfirmDelete(selectedCount))
    }
}