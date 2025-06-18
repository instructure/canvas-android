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

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HorizonInboxListViewModel @Inject constructor(
    private val repository: HorizonInboxListRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(
        HorizonInboxListUiState(
            loadingState = LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::dismissSnackbar,
            ),
            updateRecipientSearchQuery = ::updateRecipientSearchQuery,
            updateScopeFilter = ::updateScopeFilter,
            updateSelectedRecipients = ::updateSelectedRecipients
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
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
            _uiState.update { currentState ->
                currentState.copy(
                    loadingState = currentState.loadingState.copy(
                        isLoading = false,
                        snackbarMessage = "Failed to load Inbox"
                    )
                )
            }
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
        }
        val accountAnnouncements = repository.getAccountAnnouncements(forceRefresh)
        val courseAnnouncements = repository.getCourseAnnouncements(forceRefresh)
        val recipients = repository.getRecipients(uiState.value.recipientSearchQuery.text, forceRefresh)

        val items = buildList {
            addAll(
                conversations.map {
                    HorizonInboxListItemState(
                        id = it.id.toString(),
                        type = HorizonInboxListItemType.Inbox,
                        title = "Message",
                        description = it.subject.orEmpty(),
                        date = it.lastMessageSent,
                        isUnread = it.workflowState == Conversation.WorkflowState.UNREAD
                    )
                }
            )
            addAll(
                accountAnnouncements.map {
                    HorizonInboxListItemState(
                        id = it.id.toString(),
                        type = HorizonInboxListItemType.AccountNotification,
                        title = "Announcement",
                        description = it.subject,
                        date = it.endDate ?: Date(),
                        isUnread = true // TODO
                    )
                }
            )
            addAll(
                courseAnnouncements.map { courseAnnouncementPair ->
                    val course = courseAnnouncementPair.first
                    val announcement = courseAnnouncementPair.second
                    HorizonInboxListItemState(
                        id = announcement.id.toString(),
                        type = HorizonInboxListItemType.CourseNotification(course.id.toString()),
                        title = "Announcement For ${course.name}",
                        description = announcement.title.orEmpty(),
                        date = announcement.createdDate ?: Date(),
                        isUnread = announcement.status == DiscussionTopicHeader.ReadState.UNREAD
                    )
                }
            )
        }
        _uiState.update {
            it.copy(
                items = items.sortedByDescending { it.date },
                allRecipients = recipients
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
            _uiState.update { currentState ->
                currentState.copy(
                    loadingState = currentState.loadingState.copy(
                        isRefreshing = false,
                        snackbarMessage = "Failed to load Inbox"
                    )
                )
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(snackbarMessage = null))
        }
    }

    private fun updateRecipientSearchQuery(value: TextFieldValue) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(recipientSearchQuery = value)
            }
            val recipients = repository.getRecipients(value.text, true)
            _uiState.update {
                it.copy(allRecipients = recipients)
            }
        } catch {

        }
    }

    private fun updateScopeFilter(scope: HorizonInboxScope) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(selectedScope = scope)
            }
            loadData()
        } catch {

        }
    }

    private fun updateSelectedRecipients(value: List<Recipient>) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(selectedRecipients = value)
            }
            loadData()
        } catch {

        }
    }
}