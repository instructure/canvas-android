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
import com.instructure.canvasapi2.managers.InboxManager
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ApiPrefs
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
    private val inboxManager: InboxManager,
    private val apiPrefs: ApiPrefs,
    @ApplicationContext private val context: Context,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<InboxViewData>
        get() = _data
    private val _data = MutableLiveData<InboxViewData>()

    val events: LiveData<Event<InboxAction>>
        get() = _events
    private val _events = MutableLiveData<Event<InboxAction>>()

    init {
        _state.postValue(ViewState.Loading)
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                val conversations = inboxManager.getConversationsAsync(InboxApi.Scope.ALL, true).await().dataOrThrow
                val itemViewModels = createInboxEntriesFromResponse(conversations)
                _state.postValue(ViewState.Success)
                _data.postValue(InboxViewData(itemViewModels))
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
            createAvatarUrl(conversation),
            createMessageUsername(conversation),
            conversation.subject ?: "",
            conversation.lastMessagePreview ?: "",
            createDateText(conversation),
            conversation.workflowState == Conversation.WorkflowState.UNREAD,
            conversation.isStarred,
            conversation.hasAttachments() || conversation.hasMedia()
        )

        return InboxEntryItemViewModel(viewData)
    }

    private fun createAvatarUrl(conversation: Conversation): String {
        return conversation.avatarUrl ?: ""
    }

    private fun createDateText(conversation: Conversation): String {
        val date = conversation.lastAuthoredMessageSent ?: conversation.lastMessageSent
        return DateHelper.dateToDayMonthYearString(context, date) ?: ""
    }

    private fun createMessageUsername(conversation: Conversation): String {
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

    fun refresh() {
        fetchData()
    }
}