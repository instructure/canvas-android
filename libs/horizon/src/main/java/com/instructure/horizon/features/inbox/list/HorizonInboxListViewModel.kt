/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.inbox.list

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.InboxEvent
import com.instructure.horizon.features.inbox.InboxEventHandler
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HorizonInboxListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HorizonInboxListRepository,
    private val inboxEventHandler: InboxEventHandler
): ViewModel() {
    private val _uiState = MutableStateFlow(
        HorizonInboxListUiState(
            loadingState = LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::dismissSnackbar,
            ),
            updateRecipientSearchQuery = ::updateRecipientSearchQuery,
            updateScopeFilter = ::updateScopeFilter,
            onRecipientSelected = ::onRecipientSelected,
            onRecipientRemoved = ::onRecipientRemoved,
            showSnackbar = ::showSnackbar,
            setItemAsRead = ::setItemAsRead
        )
    )
    val uiState = _uiState.asStateFlow()

    private val recipientSearchQueryFlow = MutableStateFlow("")

    init {
        loadData()

        viewModelScope.tryLaunch {
            recipientSearchQueryFlow
                .debounce(200)
                .filter { it.length >= uiState.value.minQueryLength }
                .collectLatest { query ->
                    _uiState.update { it.copy(isOptionListLoading = true) }
                    fetchRecipients(query)
                    _uiState.update { it.copy(isOptionListLoading = false) }
                }
        } catch {
            showErrorState()
        }

        viewModelScope.launch {
            inboxEventHandler.events.collect { event ->
                when (event) {
                    is InboxEvent.RefreshRequested -> refresh()
                    is InboxEvent.AnnouncementRead -> refresh()
                    is InboxEvent.ConversationCreated -> {
                        refresh()
                        showSnackbar(event.message)
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = true))
            }

            fetchData()

            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false))
            }
        } catch {
            showErrorState()
        }
    }

    private suspend fun fetchRecipients(query: String) {
        val recipients = repository.getRecipients(query, false)
        _uiState.update {
            it.copy(allRecipients = recipients)
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false) {
        val conversationScope = when (uiState.value.selectedScope) {
            HorizonInboxScope.All -> InboxApi.Scope.INBOX
            HorizonInboxScope.Announcements -> null
            HorizonInboxScope.Unread -> InboxApi.Scope.UNREAD
            HorizonInboxScope.Sent -> InboxApi.Scope.SENT
        }
        val conversations = if (conversationScope == null) {
            emptyList()
        } else {
            repository.getConversations(conversationScope, forceRefresh)
                .filter { conversation ->
                    uiState.value.selectedRecipients.isEmpty()
                            || conversation.audience?.map { it.toString() }?.containsAll(
                        uiState.value.selectedRecipients.map { it.stringId }
                    ).orDefault()
                }
        }
        val accountAnnouncements = if (uiState.value.selectedScope != HorizonInboxScope.Sent) {
             repository.getAccountAnnouncements(forceRefresh)
                 .filter {
                     uiState.value.selectedRecipients.isEmpty()
                 }
        } else {
            emptyList()
        }
        val courseAnnouncements = if (uiState.value.selectedScope != HorizonInboxScope.Sent) {
            repository.getCourseAnnouncements(forceRefresh)
                .filter {
                    uiState.value.selectedRecipients.isEmpty()
                            || listOf(it.second.author?.id?.toString()) == uiState.value.selectedRecipients.map { recipient -> recipient.stringId }
                }
                .filter { uiState.value.selectedScope != HorizonInboxScope.Unread || it.second.status == DiscussionTopicHeader.ReadState.UNREAD }
        } else {
            emptyList()
        }

        val items = buildList {
            addAll(
                conversations
                    .map { conversation ->
                        HorizonInboxListItemState(
                            id = conversation.id,
                            type = HorizonInboxItemType.Inbox,
                            title = conversation.subject.orEmpty(),
                            description = conversation.participants?.map { it.name }?.joinToString(", ").orEmpty(),
                            courseId = conversation.contextCode?.substringAfter("course_")?.toLongOrNull(),
                            date = listOf(
                                conversation.lastAuthoredMessageAt?.toDate(),
                                conversation.lastMessageAt?.toDate()
                            ).maxByOrNull { it?.time ?: 0 },
                            isUnread = conversation.workflowState == Conversation.WorkflowState.UNREAD
                        )
                    }
            )
            addAll(
                accountAnnouncements
                    .map {
                        HorizonInboxListItemState(
                            id = it.id,
                            type = HorizonInboxItemType.AccountNotification,
                            title = context.getString(R.string.inboxAnnouncementTitle),
                            description = it.subject,
                            date = it.startDate,
                            isUnread = true
                        )
                    }
            )
            addAll(
                courseAnnouncements
                    .map { courseAnnouncementPair ->
                        val course = courseAnnouncementPair.first
                        val announcement = courseAnnouncementPair.second
                        HorizonInboxListItemState(
                            id = announcement.id,
                            type = HorizonInboxItemType.CourseNotification,
                            title = context.getString(
                                R.string.inboxCourseAnnouncementTitle,
                                course.name
                            ),
                            description = announcement.title.orEmpty(),
                            courseId = course.id,
                            date = announcement.createdDate,
                            isUnread = announcement.status == DiscussionTopicHeader.ReadState.UNREAD
                        )
                    }
            )
        }
        _uiState.update {
            it.copy(
                items = items.sortedByDescending { item -> item.date },
            )
        }
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isRefreshing = true))
            }

            fetchData(true)

            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isRefreshing = false))
            }
        } catch {
            showErrorState()
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(snackbarMessage = null))
        }
    }

    private fun updateRecipientSearchQuery(value: TextFieldValue) {
        _uiState.update {
            it.copy(recipientSearchQuery = value)
        }
        recipientSearchQueryFlow.tryEmit(value.text)
    }

    private fun updateScopeFilter(scope: HorizonInboxScope) {
        _uiState.update {
            it.copy(selectedScope = scope)
        }
        loadData()
    }

    private fun onRecipientSelected(value: Recipient) {
        _uiState.update {
            it.copy(
                selectedRecipients = it.selectedRecipients + value,
                recipientSearchQuery = TextFieldValue(""),
            )
        }
        loadData()
    }

    private fun onRecipientRemoved(value: Recipient) {
        _uiState.update {
            it.copy(
                selectedRecipients = it.selectedRecipients - value,
            )
        }
        loadData()
    }

    private fun showSnackbar(message: String) {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(snackbarMessage = message))
        }
    }

    private fun showErrorState() {
        _uiState.update { currentState ->
            currentState.copy(
                loadingState = currentState.loadingState.copy(
                    isRefreshing = false,
                    isLoading = false,
                    snackbarMessage = context.getString(R.string.failedToLoadInbox)
                )
            )
        }
    }

    private fun setItemAsRead(itemId: Long) {
        _uiState.update {
            it.copy(
                items = it.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(isUnread = false)
                    } else {
                        item
                    }
                }
            )
        }

        repository.invalidateConversationListCachedResponse()
    }
}