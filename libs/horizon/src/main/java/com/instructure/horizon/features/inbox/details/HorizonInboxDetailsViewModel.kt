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
package com.instructure.horizon.features.inbox.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HorizonInboxDetailsViewModel @Inject constructor(
    private val repository: HorizonInboxDetailsRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val courseId: String? = savedStateHandle[HorizonInboxRoute.InboxDetails.COURSE_ID]
    private val typeStringValue: String? = savedStateHandle[HorizonInboxRoute.InboxDetails.TYPE]
    private val type = HorizonInboxItemType.entries.firstOrNull { it.navigationValue == typeStringValue }
    private val id: Long? = savedStateHandle[HorizonInboxRoute.InboxDetails.ID]

    private val _uiState = MutableStateFlow(
        HorizonInboxDetailsUiState()
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = true)
                )
            }

            fetchData()

            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false)
                )
            }
        } catch {
            Log.d("HorizonInboxDetailsViewModel", "Error loading data", it)
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false, snackbarMessage = "Error")
                )
            }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false) {
        if (type == null || id == null) {
            loadErrorState()
            return
        }
        val newState = when(type) {
            HorizonInboxItemType.Inbox -> {
                val conversation = repository.getConversation(id, forceRefresh = forceRefresh)
                    uiState.value.copy(
                        title = conversation.subject.orEmpty(),
                        titleIcon = null,
                        items = conversation.messages.map { message ->
                            HorizonInboxDetailsItem(
                                author = conversation.participants.firstOrNull { it.id == message.authorId }?.name.orEmpty(),
                                date = message.createdAt.toDate() ?: Date(),
                                content = message.body.orEmpty(),
                                attachments = message.attachments
                            )
                        },
                        replyState = if (conversation.cannotReply) {
                            null
                        } else {
                            HorizonInboxReplyState()
                        },
                        bottomLayout = true
                    )

            }
            HorizonInboxItemType.AccountNotification -> {
                val accountNotification = repository.getAccountAnnouncement(id, forceRefresh)
                uiState.value.copy(
                    title = accountNotification.subject,
                    titleIcon = R.drawable.ic_announcement,
                    items = listOf(
                            HorizonInboxDetailsItem(
                            author = "Global Announcement",
                            date = accountNotification.endDate ?: Date(),
                            content = accountNotification.message,
                            attachments = emptyList()
                        )
                    ),
                    replyState = null,
                    bottomLayout = false
                )
            }
            HorizonInboxItemType.CourseNotification -> {
                if (courseId == null) {
                    loadErrorState()
                    return
                }
                val announcement = repository.getAnnouncement(id, courseId.toLong(), forceRefresh)
                val topic = repository.getAnnouncementTopic(id, courseId.toLong(), forceRefresh)
                uiState.value.copy(
                    title = announcement.title.orEmpty(),
                    titleIcon = R.drawable.ic_announcement,
                    items = listOf(
                        HorizonInboxDetailsItem(
                            author = announcement.author?.displayName.orEmpty(),
                            date = announcement.postedDate ?: Date(),
                            content = announcement.message.orEmpty(),
                            attachments = emptyList()
                        )
                    ) + topic.views.map { message ->
                        HorizonInboxDetailsItem(
                            author = message.author?.displayName.orEmpty(),
                            date = message.createdAt.toDate() ?: Date(),
                            content = message.message.orEmpty(),
                            attachments = emptyList()
                        )
                    },
                    replyState = null,
                    bottomLayout = false
                )
            }
        }

        Log.d("HorizonInboxDetailsViewModel", "New state: $newState")
        _uiState.update { newState }
    }

    private fun loadErrorState() {
        _uiState.update {
            it.copy(
                loadingState = it.loadingState.copy(
                    isLoading = false,
                    isError = true
                )
            )
        }
    }
}