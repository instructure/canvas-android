/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.submission.annotation.comments

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.SubmissionDateFormatter
import com.instructure.ngc.features.submission.annotation.CommentThreadRequest
import com.instructure.ngc.features.submission.annotation.CommentThreadResult
import com.instructure.ngc.features.submission.domain.AddAnnotationCommentReplyUseCase
import com.instructure.ngc.features.submission.domain.DeleteAnnotationCommentReplyUseCase
import com.instructure.ngc.features.submission.domain.EditAnnotationCommentReplyUseCase
import com.instructure.ngc.features.submission.domain.GetAnnotationCommentRepliesUseCase
import com.instructure.ngc.features.submission.domain.parseCanvaDocsTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads and mutates the reply thread for a single head annotation (see [CommentThreadRequest] /
 * [AnnotationCommentsBottomSheet]). Depends only on the canvadocs reply use cases (never a Manager/Repository
 * directly) — mirrors classic `AnnotationCommentListFragment`'s add/edit/delete flows, but keeps its own
 * authoritative [replies] list so [buildResult] can hand the full, current thread back to
 * [com.instructure.ngc.features.submission.annotation.PdfAnnotationSubmissionView.onCommentThreadResult]
 * on exit — no second `loadAnnotations()`/EventBus round trip.
 */
@HiltViewModel
class AnnotationCommentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAnnotationCommentRepliesUseCase: GetAnnotationCommentRepliesUseCase,
    private val addAnnotationCommentReplyUseCase: AddAnnotationCommentReplyUseCase,
    private val editAnnotationCommentReplyUseCase: EditAnnotationCommentReplyUseCase,
    private val deleteAnnotationCommentReplyUseCase: DeleteAnnotationCommentReplyUseCase,
    private val dateFormatter: SubmissionDateFormatter,
) : ViewModel() {

    // Supplied by [load] rather than read from a SavedStateHandle: the thread is presented as a Tray
    // over the still-composed submission pane, not as its own nav destination, so there are no nav args
    // to read. This mirrors SubmissionCommentsComponent/SubmissionCommentsViewModel's load(...) idiom and
    // lets one ViewModel instance serve successive threads without leaving the submission screen.
    private var headAnnotationId: String = ""
    private var sessionId: String = ""
    private var canvaDocsDomain: String = ""
    private var documentId: String = ""
    private var canWrite: Boolean = false
    private var canManage: Boolean = false

    // The canvadocs-session-scoped AnnotationMetadata.userId (see CommentThreadRequest.currentUserId) — the
    // canonical "own reply" key also used by classic (AnnotationCommentListRecyclerAdapter) and the base
    // (PdfSubmissionView.loadAnnotations), rather than ApiPrefs.user's local Canvas id.
    private var currentUserId: String = ""

    // Authoritative reply list — mutated in lockstep with _uiState.replies and handed back verbatim by
    // buildResult(). Kept separate from the UiState projection because add/edit need the raw
    // CanvaDocAnnotation (page, userId, contents) that the UI list doesn't carry.
    private var replies: List<CanvaDocAnnotation> = emptyList()

    private val _uiState = MutableStateFlow(AnnotationCommentsUiState())
    val uiState: StateFlow<AnnotationCommentsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                onInputTextChanged = ::onInputTextChanged,
                onSendClick = ::onSendClick,
                onEditClick = ::onEditClick,
                onCancelEditClick = ::onCancelEditClick,
                onDeleteClick = ::onDeleteClick,
                onErrorMessageConsumed = ::onErrorMessageConsumed,
                onExitEventConsumed = ::onExitEventConsumed,
            )
        }
    }

    /**
     * Binds this ViewModel to [request]'s thread and loads it. Called from a `LaunchedEffect(request)` so
     * re-opening the Tray for a different annotation rebinds and reloads; every per-thread field is reset
     * here (not just the reply list) so no composer text, edit target or one-shot event leaks across
     * threads.
     */
    fun load(request: CommentThreadRequest) {
        headAnnotationId = request.headAnnotationId
        sessionId = request.sessionId
        canvaDocsDomain = request.canvaDocsDomain
        documentId = request.documentId
        canWrite = request.canWrite
        canManage = request.canManage
        currentUserId = request.currentUserId
        replies = emptyList()

        _uiState.update {
            it.copy(
                canComment = request.canWrite && request.canComment,
                replies = emptyList(),
                inputText = "",
                editingReplyId = null,
                sending = false,
                errorMessage = null,
                exitEvent = false,
            )
        }
        loadReplies()
    }

    private fun loadReplies() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                replies = getAnnotationCommentRepliesUseCase(
                    GetAnnotationCommentRepliesUseCase.Params(
                        sessionId = sessionId,
                        canvaDocsDomain = canvaDocsDomain,
                        headAnnotationId = headAnnotationId,
                    )
                )
                _uiState.update { it.copy(loading = false, loadError = null, replies = replies.map(::toUiState)) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        loadError = context.getString(R.string.ngc_annotationComments_loadError),
                        onRetry = ::loadReplies,
                    )
                }
            }
        }
    }

    private fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    private fun onSendClick() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.sending) return
        val editingId = _uiState.value.editingReplyId

        _uiState.update { it.copy(sending = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                if (editingId != null) {
                    val target = replies.firstOrNull { it.annotationId == editingId }
                        ?: throw IllegalStateException("Reply $editingId no longer exists")
                    val updated = editAnnotationCommentReplyUseCase(
                        EditAnnotationCommentReplyUseCase.Params(
                            sessionId = sessionId,
                            canvaDocsDomain = canvaDocsDomain,
                            reply = target,
                            newContents = text,
                        )
                    )
                    replies = replies.map { if (it.annotationId == updated.annotationId) updated else it }
                } else {
                    val page = replies.firstOrNull()?.page ?: 0
                    val added = addAnnotationCommentReplyUseCase(
                        AddAnnotationCommentReplyUseCase.Params(
                            sessionId = sessionId,
                            canvaDocsDomain = canvaDocsDomain,
                            documentId = documentId,
                            headAnnotationId = headAnnotationId,
                            page = page,
                            contents = text,
                            // The same session-scoped id canEdit/canDelete gate on. Writing
                            // ApiPrefs.user.id here instead meant that on any tenant where the two
                            // diverge (global/cross-shard ids, act-as) the user got no edit/delete
                            // affordance on the reply they had just posted.
                            userId = currentUserId,
                        )
                    )
                    replies = replies + added
                }
                _uiState.update {
                    it.copy(
                        sending = false,
                        inputText = "",
                        editingReplyId = null,
                        replies = replies.map(::toUiState),
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(sending = false, errorMessage = context.getString(R.string.ngc_annotationComments_actionError))
                }
            }
        }
    }

    private fun onEditClick(replyId: String) {
        val reply = replies.firstOrNull { it.annotationId == replyId } ?: return
        if (!canEdit(reply)) return
        _uiState.update { it.copy(editingReplyId = replyId, inputText = reply.contents.orEmpty()) }
    }

    private fun onCancelEditClick() {
        _uiState.update { it.copy(editingReplyId = null, inputText = "") }
    }

    private fun onDeleteClick(replyId: String) {
        val reply = replies.firstOrNull { it.annotationId == replyId } ?: return
        // Reuses the same busy flag onSendClick guards with — only one mutating action (add/edit/delete)
        // should be in flight at a time, so rapid repeated taps (e.g. double-tapping the confirm dialog's
        // delete button) can't fire concurrent/duplicate operations.
        if (!canDelete(reply) || _uiState.value.sending) return

        _uiState.update { it.copy(sending = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                deleteAnnotationCommentReplyUseCase(
                    DeleteAnnotationCommentReplyUseCase.Params(
                        sessionId = sessionId,
                        canvaDocsDomain = canvaDocsDomain,
                        annotationId = replyId,
                    )
                )
                replies = replies.filterNot { it.annotationId == replyId }
                _uiState.update { state ->
                    val wasEditingDeletedReply = state.editingReplyId == replyId
                    state.copy(
                        sending = false,
                        replies = replies.map(::toUiState),
                        editingReplyId = if (wasEditingDeletedReply) null else state.editingReplyId,
                        inputText = if (wasEditingDeletedReply) "" else state.inputText,
                        // No replies left — nothing more to show, mirrors classic's auto-exit after the
                        // thread's last comment is deleted.
                        exitEvent = replies.isEmpty(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(sending = false, errorMessage = context.getString(R.string.ngc_annotationComments_actionError))
                }
            }
        }
    }

    private fun onErrorMessageConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun onExitEventConsumed() {
        _uiState.update { it.copy(exitEvent = false) }
    }

    /** The authoritative result to hand back to [com.instructure.ngc.features.submission.annotation.PdfAnnotationSubmissionView] on exit. */
    fun buildResult(): CommentThreadResult = CommentThreadResult(headAnnotationId, replies)

    private fun canEdit(reply: CanvaDocAnnotation): Boolean = canWrite && reply.userId == currentUserId

    private fun canDelete(reply: CanvaDocAnnotation): Boolean =
        canManage || (canWrite && reply.userId == currentUserId)

    private fun toUiState(reply: CanvaDocAnnotation): AnnotationCommentUiState = AnnotationCommentUiState(
        id = reply.annotationId,
        authorName = reply.userName.orEmpty(),
        // parseCanvaDocsTimestamp is pure java.time (safe off-device) and, unlike
        // DateHelper.stringToDateWithMillis, can actually parse canvadocs' createdAt — see its KDoc.
        // The locale-aware 12h/24h rendering goes through the injectable SubmissionDateFormatter (the
        // same one NGCSubmissionViewModel uses) rather than android.text.format.DateFormat directly, so
        // this stays unit-testable without Robolectric.
        timestampText = parseCanvaDocsTimestamp(reply.createdAt)?.let { dateFormatter.formatDateTime(context, it) }.orEmpty(),
        body = reply.contents.orEmpty(),
        canEdit = canEdit(reply),
        canDelete = canDelete(reply),
    )
}
