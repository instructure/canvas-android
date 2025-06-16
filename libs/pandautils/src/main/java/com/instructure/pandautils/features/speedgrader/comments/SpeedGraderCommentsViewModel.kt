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
package com.instructure.pandautils.features.speedgrader.comments

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderCommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val speedGraderCommentsRepository: SpeedGraderCommentsRepository,
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedGraderCommentsUiState())
    val uiState = _uiState.asStateFlow()

    private val assignmentId: Long = savedStateHandle.get<Long>(ASSIGNMENT_ID_KEY) ?: -1L
    private val submissionId: Long = savedStateHandle.get<Long>(SUBMISSION_ID_KEY) ?: -1L

    var pageId: String? = null
        private set

    var attempt: Int = -1
        private set

    // TODO use actual value from feature flag
    val assignmentEnhancementsEnabled = true

    init {
        viewModelScope.launch {
            fetchData()
        }
    }

    private suspend fun fetchData() {
        val comments = speedGraderCommentsRepository.getSubmissionComments(submissionId)
        pageId =
            "${apiPrefs.domain}-${comments.submission?.assignment?.course?._id}-$assignmentId-${comments.submission?.userId}"
        attempt = comments.submission?.attempt ?: -1
        _uiState.update { state ->
            state.copy(
                comments = comments.submission?.commentsConnection?.edges
                    ?.mapNotNull { edge ->
                        edge?.node?.let {
                            SpeedGraderComment(
                                id = it.mediaCommentId ?: "",
                                authorName = it.author?.name ?: "Unknown",
                                authorId = it.author?._id ?: "",
                                authorAvatarUrl = it.author?.avatarUrl ?: "",
                                content = it.comment ?: "",
                                createdAt = it.createdAt.toString(),
                                isOwnComment = apiPrefs.user?.id?.toString() == it.author?._id,
                                attachments = it.attachments?.map { attachment ->
                                    SpeedGraderCommentAttachment(
                                        id = attachment.id,
                                        url = attachment.url ?: "",
                                        thumbnailUrl = attachment.thumbnailUrl,
                                        createdAt = attachment.createdAt.toString(),
                                        title = attachment.title ?: "",
                                        displayName = attachment.displayName ?: "",
                                        contentType = attachment.contentType ?: "",
                                        size = attachment.size ?: "",

                                        )
                                } ?: emptyList()
                            )
                        }

                    } ?: emptyList(),
                isLoading = false,
            )
        }
    }

    fun handleAction(action: SpeedGraderCommentsAction) {
        when (action) {
            is SpeedGraderCommentsAction.CommentFieldChanged -> {
                _uiState.update { state ->
                    state.copy(commentText = action.commentText)
                }
            }

            SpeedGraderCommentsAction.AddCommentLibraryClicked -> {
                // Handle adding comment from library
            }

            SpeedGraderCommentsAction.AddAttachmentClicked -> {
                // Handle adding attachment
            }

            SpeedGraderCommentsAction.SendCommentClicked -> {
                onSendCommentClicked()
            }
        }
    }

    private suspend fun createPendingComment(commentText: String): SpeedGraderComment? {
        pageId?.let { pageId ->
            val drafts = pendingSubmissionCommentDao.findByPageId(pageId)
                .orEmpty()
                .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }
            pendingSubmissionCommentDao.deleteAll(drafts.map { it.pendingSubmissionCommentEntity })
            val newComment = PendingSubmissionComment(pageId, commentText).apply {
                attemptId = attempt.toLong().takeIf { assignmentEnhancementsEnabled }
            }
            pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
            val pendingComment = SpeedGraderComment(
                id = newComment.id.toString(),
                authorName = apiPrefs.user?.name ?: "",
                authorId = apiPrefs.user?.id?.toString() ?: "",
                authorAvatarUrl = apiPrefs.user?.avatarUrl ?: "",
                content = commentText,
                createdAt = DateHelper.longToSpeedGraderDateString(System.currentTimeMillis())
                    ?: "",
                isOwnComment = true,
                attachments = emptyList(),
                isPending = true
            )

            return pendingComment
        }
        return null
    }

    private suspend fun sendComment(comment: SpeedGraderComment) {
        pendingSubmissionCommentDao.findById(comment.id.toLong())?.let {
            it.status = CommentSendStatus.SENDING.toString()
            pendingSubmissionCommentDao.update(it)
        }

        try {
            val newComment = speedGraderCommentsRepository.createSubmissionComment(
                submissionId,
                comment.content
            )
            pendingSubmissionCommentDao.findById(comment.id.toLong())?.let {
                pendingSubmissionCommentDao.delete(it)
            }
            val comments = _uiState.value.comments.filterNot { it.id == comment.id } + SpeedGraderComment(
                id = newComment.createSubmissionComment?.submissionComment?._id ?: "",
                authorName = apiPrefs.user?.name ?: "",
                authorId = apiPrefs.user?.id?.toString() ?: "",
                authorAvatarUrl = apiPrefs.user?.avatarUrl ?: "",
                content = comment.content,
                createdAt = newComment.createSubmissionComment?.submissionComment?.createdAt.toString(),
                isOwnComment = true,
                attachments = emptyList(),
                isPending = false
            )
            _uiState.update { state ->
                state.copy(
                    comments = comments,
                    commentText = TextFieldValue("")
                )
            }
        } catch (e: Throwable) {
            pendingSubmissionCommentDao.findById(comment.id.toLong())?.let {
                it.status = CommentSendStatus.ERROR.toString()
                pendingSubmissionCommentDao.update(it)
            }

            // TODO implement error state in UI and in SpeedGraderComment class, use here
            viewModelScope.ensureActive()
        }
    }

    private fun onSendCommentClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value.commentText.text.isNotEmpty()) {
                val pendingComment = createPendingComment(_uiState.value.commentText.text)

                pendingComment ?: return@launch
                _uiState.update { state ->
                    state.copy(
                        comments = state.comments + pendingComment,
                        commentText = TextFieldValue("")
                    )
                }

                delay(10000)
                sendComment(pendingComment)
            }
        }
    }

    companion object {
        const val ASSIGNMENT_ID_KEY = "assignmentId"
        const val SUBMISSION_ID_KEY = "submissionId"
    }
}