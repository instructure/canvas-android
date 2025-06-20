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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.comments

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.instructure.canvasapi2.managers.CommentAttachment
import com.instructure.canvasapi2.managers.CommentsData
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.cards.CommentAttachmentState
import com.instructure.horizon.horizonui.organisms.cards.CommentCardState
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import com.instructure.pandautils.utils.format
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commentsRepository: CommentsRepository,
    private val apiPrefs: ApiPrefs,
    private val workManager: WorkManager,
    private val fileDownloadProgressDao: FileDownloadProgressDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CommentsUiState(
            onCommentChanged = ::onCommentChanged,
            onPostClicked = ::postComment,
            onFileOpened = ::onFileOpened
        )
    )
    val uiState = _uiState.asStateFlow()

    private var assignmentId: Long = -1L
    private var attempt: Int = -1
    private var courseId: Long = -1L

    private var endCursor: String? = null
    private var startCursor: String? = null
    private var hasNextPage: Boolean = false
    private var hasPreviousPage: Boolean = false

    private var fileIdToWorkerIdMap = mutableMapOf<Long, String>()

    fun initWithAttempt(assignmentId: Long, attempt: Int, courseId: Long) {
        _uiState.update { it.copy(loading = true) }
        this.assignmentId = assignmentId
        this.attempt = attempt
        this.courseId = courseId

        endCursor = null
        startCursor = null
        hasNextPage = false
        hasPreviousPage = false

        viewModelScope.tryLaunch {
            val commentsData = commentsRepository.getComments(
                assignmentId = assignmentId,
                userId = apiPrefs.user?.id.orDefault(),
                attempt = attempt,
                forceNetwork = false
            )

            updateState(commentsData)
        } catch { _ ->
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun loadNextPage() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.tryLaunch {
            val commentsData = commentsRepository.getComments(
                assignmentId = assignmentId,
                userId = apiPrefs.user?.id.orDefault(),
                attempt = attempt,
                forceNetwork = false,
                endCursor = endCursor,
                nextPage = true
            )

            updateState(commentsData)
        } catch { _ ->
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun loadPreviousPage() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.tryLaunch {
            val commentsData = commentsRepository.getComments(
                assignmentId = assignmentId,
                userId = apiPrefs.user?.id.orDefault(),
                attempt = attempt,
                forceNetwork = false,
                startCursor = startCursor,
                nextPage = false
            )

            updateState(commentsData)
        } catch { _ ->
            _uiState.update { it.copy(loading = false) }
        }
    }

    private fun updateState(commentsData: CommentsData) {
        endCursor = commentsData.endCursor
        startCursor = commentsData.startCursor
        hasNextPage = commentsData.hasNextPage
        hasPreviousPage = commentsData.hasPreviousPage

        val subtitle = if (attempt == 0) {
            ""
        } else {
            context.getString(R.string.commentsBottomSheet_attempt, attempt)
        }
        val commentCards = commentsData.comments.map {
            CommentCardState(
                title = it.authorName,
                date = it.createdAt.format("dd/MM, h:mm a"),
                subtitle = subtitle,
                commentText = it.commentText,
                fromCurrentUser = it.authorId == apiPrefs.user?.id,
                read = it.read,
                files = mapAttachments(it.attachments)
            )
        }

        val showPagingControls = commentsData.hasNextPage || commentsData.hasPreviousPage
        val nextPageEnabled = commentsData.hasNextPage
        val previousPageEnabled = commentsData.hasPreviousPage

        _uiState.update {
            it.copy(
                comments = commentCards,
                showPagingControls = showPagingControls,
                nextPageEnabled = nextPageEnabled,
                previousPageEnabled = previousPageEnabled,
                onPreviousPageClicked = ::loadPreviousPage,
                onNextPageClicked = ::loadNextPage,
                loading = false
            )
        }
    }

    private fun mapAttachments(attachments: List<CommentAttachment>): List<CommentAttachmentState> {
        return attachments.map { attachment ->
            CommentAttachmentState(
                fileName = attachment.fileName,
                fileUrl = attachment.fileUrl,
                fileId = attachment.attachmentId,
                onDownloadClick = { downloadFile(it) },
            )
        }
    }

    private fun onCommentChanged(newComment: TextFieldValue) {
        _uiState.update { it.copy(comment = newComment) }
    }

    private fun postComment() {
        val commentText = _uiState.value.comment.text
        if (commentText.isBlank()) return

        _uiState.update {
            it.copy(postingComment = true)
        }

        viewModelScope.tryLaunch {
            commentsRepository.postComment(courseId, assignmentId, apiPrefs.user?.id.orDefault(), attempt, commentText).dataOrThrow
            reloadData()
            _uiState.update {
                it.copy(comment = TextFieldValue(""), postingComment = false)
            }
        } catch { _ ->
            _uiState.update { it.copy(postingComment = false) }
        }
    }

    private suspend fun reloadData() {
        endCursor = null
        startCursor = null
        hasNextPage = false
        hasPreviousPage = false

        val commentsData = commentsRepository.getComments(
            assignmentId = assignmentId,
            userId = apiPrefs.user?.id.orDefault(),
            attempt = attempt,
            forceNetwork = true
        )

        updateState(commentsData)
    }

    private fun downloadFile(attachment: CommentAttachmentState) {
        updateAttachmentState(attachment.copy(downloadState = FileDownloadProgressState.STARTING, downloadProgress = 0f))
        val workRequest = FileDownloadWorker.createOneTimeWorkRequest(attachment.fileName, attachment.fileUrl)
        workManager.enqueue(workRequest)
        val workerId = workRequest.id.toString()
        fileIdToWorkerIdMap[attachment.fileId] = workerId
        viewModelScope.tryLaunch {
            fileDownloadProgressDao.findByWorkerIdFlow(workerId)
                .collect { progress ->
                    updateProgress(attachment, progress)
                }
        } catch {
            updateAttachmentState(attachment.copy(downloadState = FileDownloadProgressState.COMPLETED, downloadProgress = 0f))
        }
    }

    private fun updateProgress(attachment: CommentAttachmentState, progressEntity: FileDownloadProgressEntity?) {
        updateAttachmentState(
            attachment.copy(
                downloadState = progressEntity?.progressState ?: FileDownloadProgressState.COMPLETED,
                downloadProgress = (progressEntity?.progress ?: 0) / 100f
            )
        )
        if (progressEntity?.progressState == FileDownloadProgressState.COMPLETED) {
            _uiState.update {
                it.copy(filePathToOpen = progressEntity.filePath, mimeTypeToOpen = attachment.fileType)
            }
        }
        if (progressEntity?.progressState?.isCompleted() == true) {
            viewModelScope.launch {
                fileDownloadProgressDao.deleteByWorkerId(progressEntity.workerId)
            }
        }
    }

    private fun updateAttachmentState(attachment: CommentAttachmentState) {
        _uiState.update {
            it.copy(comments = it.comments.map { comment ->
                if (comment.files.any { file -> file.fileId == attachment.fileId }) {
                    comment.copy(
                        files = comment.files.map { item ->
                            if (item.fileId == attachment.fileId) {
                                attachment
                            } else {
                                item
                            }
                        }
                    )
                } else {
                    comment
                }
            })
        }
    }

    private fun onFileOpened() {
        _uiState.update { it.copy(filePathToOpen = null, mimeTypeToOpen = null) }
    }
}