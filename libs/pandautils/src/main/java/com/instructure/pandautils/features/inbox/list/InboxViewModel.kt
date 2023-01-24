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

import android.content.Context
import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.localized
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.list.itemviewmodels.InboxEntryItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val inboxRepository: InboxRepository,
    private val apiPrefs: ApiPrefs,
    @ApplicationContext private val context: Context,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<InboxViewData>
        get() = _data
    private val _data = MutableLiveData<InboxViewData>(InboxViewData(getTextForScope(InboxApi.Scope.INBOX), filterText = resources.getString(R.string.allCourses)))

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

    val bottomReachedCallback: () -> Unit = {
        viewModelScope.launch {
            if (_state.value != ViewState.Loading && _state.value != ViewState.LoadingNextPage && nextPageLink != null) {
                loadNextPage()
            }
        }
    }

    private suspend fun loadNextPage() {
        _state.postValue(ViewState.LoadingNextPage)
        val dataResult = inboxRepository.getConversations(scope, true, contextFilter, nextPageLink)
        if (dataResult is DataResult.Success) {
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

    private fun fetchData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            try {
                val dataResult = inboxRepository.getConversations(scope, forceNetwork, contextFilter)
                val conversations = dataResult.dataOrThrow
                if (dataResult is DataResult.Success) {
                    nextPageLink = dataResult.linkHeaders.nextUrl
                }

                val itemViewModels = createInboxEntriesFromResponse(conversations)
                _itemViewModels.postValue(itemViewModels)
                if (itemViewModels.isEmpty()) {
                    postEmptyState()
                } else {
                    _state.postValue(ViewState.Success)
                }

                inboxRepository.getCanvasContexts(forceNetwork).dataOrNull?.let {
                    canvasContexts = it
                }

                if (forceNetwork) _events.postValue(Event(InboxAction.UpdateUnreadCount))
            } catch (e: Exception) {
                e.printStackTrace()
                _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
            }
        }
    }

    private fun createInboxEntriesFromResponse(conversations: List<Conversation>): List<InboxEntryItemViewModel> {
        return conversations.map {
            createItemViewModelFromConversation(it)
        }
    }

    private fun createItemViewModelFromConversation(conversation: Conversation): InboxEntryItemViewModel {
        val viewData = InboxEntryViewData(
            conversation.id,
            createAvatarData(conversation),
            createMessageTitle(conversation),
            conversation.subject ?: "",
            conversation.lastMessagePreview ?: "",
            createDateText(conversation),
            conversation.workflowState == Conversation.WorkflowState.UNREAD,
            conversation.isStarred,
            conversation.hasAttachments() || conversation.hasMedia()
        )

        return InboxEntryItemViewModel(viewData, { starred ->
            _events.postValue(Event(InboxAction.OpenConversation(conversation.copy(isStarred = starred), scope)))
        }, { view, selected ->
            _events.postValue(Event(InboxAction.ItemSelectionChanged(view, selected)))
            handleSelectionMode()
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
        if (scope == InboxApi.Scope.ARCHIVED) {
            menuItems.add(InboxMenuItem.UNARCHIVE)
        } else {
            menuItems.add(InboxMenuItem.ARCHIVE)
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

    private fun createAvatarData(conversation: Conversation): AvatarViewData {
        return AvatarViewData(
            conversation.avatarUrl ?: "",
            conversation.participants[0].name ?: "",
            conversation.participants.size > 2
        )
    }

    private fun createDateText(conversation: Conversation): String {
        val date = conversation.lastAuthoredMessageSent ?: conversation.lastMessageSent
        return DateHelper.dateToDayMonthYearString(context, date) ?: ""
    }

    private fun createMessageTitle(conversation: Conversation): String {
        if (conversation.isMonologue(apiPrefs.user?.id ?: 0)) return resources.getString(R.string.monologue)

        val users = conversation.participants

        return when (users.size) {
            0 -> ""
            1, 2 -> users.joinTo(SpannableStringBuilder()) { Pronouns.span(it.name, it.pronouns) }.toString()
            else -> TextUtils.concat(
                Pronouns.span(users[0].name, users[0].pronouns),
                ", +${(users.size - 1).localized}"
            ).toString()
        }
    }

    private fun getTextForScope(scope: InboxApi.Scope): String {
        return when (scope) {
            InboxApi.Scope.INBOX -> resources.getString(R.string.inboxScopeInbox)
            InboxApi.Scope.UNREAD -> resources.getString(R.string.inbox_unread)
            InboxApi.Scope.STARRED -> resources.getString(R.string.inbox_starred)
            InboxApi.Scope.SENT -> resources.getString(R.string.inbox_sent)
            InboxApi.Scope.ARCHIVED -> resources.getString(R.string.inbox_archived)
            else -> resources.getString(R.string.inboxAllMessages)
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
        fetchData(true)
    }

    fun openScopeSelector() {
        _events.postValue(Event(InboxAction.OpenScopeSelector))
    }

    fun scopeChanged(newScope: InboxApi.Scope) {
        if (newScope != scope) {
            scope = newScope
            _state.postValue(ViewState.Loading)
            _data.postValue(_data.value?.copy(scope = getTextForScope(scope)))
            _itemViewModels.postValue(emptyList())
            fetchData()
        }
    }

    fun starSelected() {
        performBatchOperation("star", { ids ->
            _itemViewModels.value?.forEach {
                if (ids.contains(it.data.id)) it.data = it.data.copy(starred = true)
                it.notifyChange()
                _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxStarredConfirmation, ids.size)))
            }
        })
    }

    fun unstarSelected() {
        performBatchOperation("unstar", { ids ->
            _itemViewModels.value?.forEach {
                if (ids.contains(it.data.id)) it.data = it.data.copy(starred = false)
                it.notifyChange()
                _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxUnstarredConfirmation, ids.size)))
            }
        })
    }

    fun markAsReadSelected() {
        performBatchOperation("mark_as_read", { ids ->
            _itemViewModels.value?.forEach {
                if (ids.contains(it.data.id)) it.data = it.data.copy(unread = false)
                it.notifyChange()
                _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxMarkAsReadConfirmation, ids.size)))
                _events.value = Event(InboxAction.UpdateUnreadCount)
            }
        })
    }

    fun markAsUnreadSelected() {
        performBatchOperation("mark_as_unread", { ids ->
            _itemViewModels.value?.forEach {
                if (ids.contains(it.data.id)) it.data = it.data.copy(unread = true)
                it.notifyChange()
                _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxMarkAsUnreadConfirmation, ids.size)))
                _events.value = Event(InboxAction.UpdateUnreadCount)
            }
        })
    }

    fun deleteSelected() {
        performBatchOperation("destroy", { ids ->
            val newMessages = _itemViewModels.value?.filterNot { ids.contains(it.data.id) } ?: emptyList()
            _itemViewModels.value = newMessages
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxDeletedConfirmation, ids.size)))
            _events.value = Event(InboxAction.UpdateUnreadCount)
        })
    }

    fun archiveSelected() {
        performBatchOperation("archive", { ids ->
            val newMessages = _itemViewModels.value?.filterNot { ids.contains(it.data.id) } ?: emptyList()
            _itemViewModels.value = newMessages
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxArchivedConfirmation, ids.size)))
            _events.value = Event(InboxAction.UpdateUnreadCount)
        })
    }

    fun unarchiveSelected() {
        performBatchOperation("mark_as_read", { ids ->
            val newMessages = _itemViewModels.value?.filterNot { ids.contains(it.data.id) } ?: emptyList()
            _itemViewModels.value = newMessages
            _events.value = Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxUnarchivedConfirmation, ids.size)))
        })
    }

    private fun performBatchOperation(operation: String, onSuccess: (Set<Long>) -> Unit) {
        viewModelScope.launch {
            try {
                val ids = _itemViewModels.value
                    ?.filter { it.selected }
                    ?.map { it.data.id } ?: emptyList()

                val dataResult = inboxRepository.batchUpdateConversations(ids, operation)
                if (dataResult.isSuccess) {
                    inboxRepository.invalidateCachedResponses()
                    onSuccess(ids.toSet())
                    handleSelectionMode()
                } else {
                    _events.postValue(Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxOperationFailed))))
                }
            } catch (e: Exception) {
                _events.postValue(Event(InboxAction.ShowConfirmationSnackbar(resources.getString(R.string.inboxOperationFailed))))
            }
        }
    }

    fun handleBackPressed(): Boolean {
        if (_data.value?.selectionMode == true) {
            _itemViewModels.value?.forEach {
                it.selected = false
                it.selectionModeActive = false
                it.notifyChange()
            }
            _data.value = _data.value?.copy(selectedItemsCount = "", selectionMode = false)
            return true
        } else {
            return false
        }
    }

    fun invalidateCache() {
        inboxRepository.invalidateCachedResponses()
    }

    fun createNewMessage() {
        _events.postValue(Event(InboxAction.CreateNewMessage))
    }

    // TODO Temporary until design is ready
    var courseFilterIndex = -1

    fun coursesFilterClicked() {
        courseFilterIndex++

        if (courseFilterIndex >= canvasContexts.size) {
            courseFilterIndex = -1
        }

        if (courseFilterIndex == -1) {
            allCoursesSelected()
        } else {
            canvasContextFilterSelected(canvasContexts[courseFilterIndex])
        }
    }

    fun canvasContextFilterSelected(canvasContext: CanvasContext) {
        contextFilter = canvasContext
        _data.value = _data.value?.copy(filterText = canvasContext.name ?: "")
        _state.postValue(ViewState.Loading)
        _itemViewModels.postValue(emptyList())
        fetchData()
    }

    fun allCoursesSelected() {
        contextFilter = null
        _data.value = _data.value?.copy(filterText = resources.getString(R.string.allCourses))
        _state.postValue(ViewState.Loading)
        _itemViewModels.postValue(emptyList())
        fetchData()
    }
}