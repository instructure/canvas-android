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

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.InboxEvent
import com.instructure.horizon.features.inbox.InboxEventHandler
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachment
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HorizonInboxDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HorizonInboxDetailsRepository,
    private val workManager: WorkManager,
    private val fileDownloadProgressDao: FileDownloadProgressDao,
    savedStateHandle: SavedStateHandle,
    private val inboxEventHandler: InboxEventHandler
): ViewModel() {
    private val courseId: String? = savedStateHandle[HorizonInboxRoute.InboxDetails.COURSE_ID]
    private val typeStringValue: String? = savedStateHandle[HorizonInboxRoute.InboxDetails.TYPE]
    private val type = HorizonInboxItemType.entries.firstOrNull { it.navigationValue == typeStringValue }
    private val id: Long? = savedStateHandle[HorizonInboxRoute.InboxDetails.ID]

    private var recipientIds: List<String> = emptyList()
    private var messageIds: List<Long> = emptyList()

    private var fileIdToWorkerIdMap = mutableMapOf<Long, String>()

    private val _uiState = MutableStateFlow(
        HorizonInboxDetailsUiState(
            loadingState = LoadingState(
                onSnackbarDismiss = ::dismissSnackbar,
                onRefresh = ::refresh,
            ),
        )
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
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false, snackbarMessage = context.getString(
                        R.string.inboxFailedtoLoadErrorMessage
                    ))
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
                recipientIds = conversation.participants.map { it.id.toString() }
                messageIds = conversation.messages.map { it.id }
                uiState.value.copy(
                    title = conversation.subject.orEmpty(),
                    titleIcon = null,
                    items = conversation.messages.map { message ->
                        HorizonInboxDetailsItem(
                            author = conversation.participants.firstOrNull { it.id == message.authorId }?.name.orEmpty(),
                            date = message.createdAt.toDate() ?: Date(),
                            isHtmlContent = false,
                            content = message.body.orEmpty(),
                            attachments = message.attachments.map { attachment ->
                                attachment.toAttachmentUiState()
                            }
                        )
                    },
                    replyState = getReplyState(),
                    bottomLayout = true
                )
            }
            HorizonInboxItemType.AccountNotification -> {
                val accountNotification = repository.getAccountAnnouncement(id, forceRefresh)

                if (!accountNotification.closed) {
                    viewModelScope.async {
                        val result = repository.deleteAccountAnnouncement(id)
                        if (result.isSuccess) {
                            _uiState.update { it.copy(announcementMarkedAsRead = true) }
                        }

                        // We need to refresh the announcement in the background, so the next time we open and it's opened from the cache it wouldn't show as unread
                        repository.getAccountAnnouncement(id, true)
                    }
                }

                uiState.value.copy(
                    title = accountNotification.subject,
                    titleIcon = R.drawable.campaign,
                    items = listOf(
                            HorizonInboxDetailsItem(
                            author = context.getString(R.string.inboxGlobalAnnouncementAuthorLabel),
                            date = accountNotification.endDate ?: Date(),
                            isHtmlContent = true,
                            content = accountNotification.message,
                            attachments = emptyList()
                        )
                    ),
                    replyState = getReplyState(),
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

                if (announcement.status == DiscussionTopicHeader.ReadState.UNREAD) {
                    viewModelScope.async {
                        val result = repository.markAnnouncementAsRead(
                            courseId = courseId.toLong(),
                            announcementId = id,
                            entries = topic.views.map { it.id }.toSet()
                        )

                        if (result.isSuccess) {
                            inboxEventHandler.postEvent(InboxEvent.AnnouncementRead)
                        }

                        // We need to refresh the announcement in the background, so the next time we open and it's opened from the cache it wouldn't show as unread
                        repository.getAnnouncement(id, courseId.toLong(), true)
                    }
                }

                uiState.value.copy(
                    title = announcement.title.orEmpty(),
                    titleIcon = R.drawable.campaign,
                    items = listOf(
                        HorizonInboxDetailsItem(
                            author = announcement.author?.displayName.orEmpty(),
                            date = announcement.postedDate ?: Date(),
                            isHtmlContent = true,
                            content = announcement.message.orEmpty(),
                            attachments = emptyList()
                        )
                    ) + topic.views.map { message ->
                        HorizonInboxDetailsItem(
                            author = message.author?.displayName.orEmpty(),
                            date = message.createdAt.toDate() ?: Date(),
                            isHtmlContent = true,
                            content = message.message.orEmpty(),
                            attachments = emptyList()
                        )
                    },
                    replyState = getReplyState(),
                    bottomLayout = false
                )
            }
        }

        _uiState.update { newState }
    }

    private fun getReplyState(): HorizonInboxReplyState? {
        return when (type) {
            HorizonInboxItemType.Inbox -> HorizonInboxReplyState(
                onReplyTextValueChange = ::replyValueChanged,
                onSendReply = ::sendReply,
                onShowAttachmentPickerChanged = ::onShowAttachmentPickerChanged,
                onAttachmentsChanged = ::onAttachmentsChanged,
                updateShowExitConfirmationDialog = ::updateShowExitConfirmationDialog,
            )
            HorizonInboxItemType.AccountNotification, HorizonInboxItemType.CourseNotification -> null
            else -> null
        }
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
                        snackbarMessage = context.getString(R.string.inboxFailedToRefreshLabel)
                    )
                )
            }
        }
    }

    private fun downloadFile(attachment: HorizonInboxDetailsAttachment) {
        updateFileItem(attachment.copy(downloadState = FileDownloadProgressState.STARTING, downloadProgress = 0f))
        val workRequest = FileDownloadWorker.createOneTimeWorkRequest(attachment.name, attachment.url)
        workManager.enqueue(workRequest)
        val workerId = workRequest.id.toString()
        fileIdToWorkerIdMap[attachment.id] = workerId
        viewModelScope.tryLaunch {
            fileDownloadProgressDao.findByWorkerIdFlow(workerId)
                .collect { progress ->
                    updateProgress(attachment, progress)
                }
        } catch {
            updateFileItem(attachment.copy(downloadState = FileDownloadProgressState.COMPLETED, downloadProgress = 0f))
        }
    }

    private fun updateProgress(attachment: HorizonInboxDetailsAttachment, progressEntity: FileDownloadProgressEntity?) {
        updateFileItem(
            attachment.copy(
                downloadState = progressEntity?.progressState ?: FileDownloadProgressState.COMPLETED,
                downloadProgress = (progressEntity?.progress ?: 0) / 100f
            )
        )
        if (progressEntity?.progressState?.isCompleted() == true) {
            viewModelScope.launch {
                fileDownloadProgressDao.deleteByWorkerId(progressEntity.workerId)
            }
        }
    }

    private fun cancelDownload(fileId: Long) {
        val workerId = fileIdToWorkerIdMap[fileId]
        if (workerId != null) {
            workManager.cancelWorkById(UUID.fromString(workerId))
            fileIdToWorkerIdMap.remove(fileId)
            viewModelScope.tryLaunch {
                fileDownloadProgressDao.deleteByWorkerId(workerId)
            } catch {}
        }
    }

    private fun updateFileItem(attachment: HorizonInboxDetailsAttachment) {
        _uiState.update {
            it.copy(
                items = it.items.map { message ->
                    message.copy(
                        attachments = message.attachments.map { attachmentState ->
                            if (attachmentState.id == attachment.id) {
                                attachment
                            } else {
                                attachmentState
                            }
                        }
                    )
                }
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(
                loadingState = it.loadingState.copy(snackbarMessage = null)
            )
        }
    }

    private fun replyValueChanged(value: TextFieldValue) {
        _uiState.update { currentState ->
            currentState.copy(
                replyState = currentState.replyState?.copy(replyTextValue = value)
            )
        }
    }

    private fun Attachment.toAttachmentUiState(): HorizonInboxDetailsAttachment {
        return HorizonInboxDetailsAttachment(
            id = this.id,
            name = this.displayName.orEmpty(),
            url = this.url.orEmpty(),
            contentType = this.contentType,
            thumbnailUrl = this.thumbnailUrl,
            onDownloadClick = { attachmentState -> downloadFile(attachmentState) },
            onCancelDownloadClick = { id -> cancelDownload(id) },
            downloadState = FileDownloadProgressState.COMPLETED,
            downloadProgress = 0f
        )
    }

    private fun sendReply() {
        if (courseId == null || id == null || uiState.value.replyState == null) {
            loadErrorState()
            return
        }

        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    replyState = it.replyState?.copy(isLoading = true)
                )
            }

            val conversation = repository.addMessageToConversation(
                Course(id = courseId.toLong()).contextId,
                conversationId = id,
                recipientIds = recipientIds,
                body = uiState.value.replyState!!.replyTextValue.text,
                includedMessageIds = messageIds,
                attachmentIds = uiState.value.replyState!!.attachments.map { it.id }
            )
            repository.invalidateConversationDetailsCachedResponse(id)

            _uiState.update {
                it.copy(
                    items = conversation.messages.map { message ->
                        HorizonInboxDetailsItem(
                            author = conversation.participants.firstOrNull { it.id == message.authorId }?.name.orEmpty(),
                            date = message.createdAt.toDate() ?: Date(),
                            isHtmlContent = true,
                            content = message.body.orEmpty(),
                            attachments = message.attachments.map { attachment ->
                                attachment.toAttachmentUiState()
                            }
                        )
                    } + it.items,
                    replyState = it.replyState?.copy(
                        replyTextValue = TextFieldValue(""),
                        isLoading = false,
                        attachments = emptyList(),
                    )
                )
            }
        } catch {
            _uiState.update { currentState ->
                currentState.copy(
                    replyState = currentState.replyState?.copy(
                        isLoading = false,
                    ),
                    loadingState = currentState.loadingState.copy(
                        snackbarMessage = context.getString(R.string.inboxFailedToSendReplyLabel)
                    )
                )
            }
        }
    }

    private fun onShowAttachmentPickerChanged(show: Boolean) {
        _uiState.update {
            it.copy(replyState = it.replyState?.copy(showAttachmentPicker = show))
        }
    }

    private fun onAttachmentsChanged(attachments: List<HorizonInboxAttachment>) {
        _uiState.update {
            it.copy(replyState = it.replyState?.copy(attachments = attachments))
        }
    }

    private fun updateShowExitConfirmationDialog(show: Boolean) {
        _uiState.update {
            it.copy(replyState = it.replyState?.copy(showExitConfirmationDialog = show))
        }
    }
}