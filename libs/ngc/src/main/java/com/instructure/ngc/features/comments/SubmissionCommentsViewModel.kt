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
package com.instructure.ngc.features.comments

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.ngc.features.comments.domain.CompleteFileCommentUseCase
import com.instructure.ngc.features.comments.domain.CompleteMediaCommentUseCase
import com.instructure.ngc.features.comments.domain.CreatePendingFileCommentUseCase
import com.instructure.ngc.features.comments.domain.CreatePendingMediaCommentUseCase
import com.instructure.ngc.features.comments.domain.CreatePendingTextCommentUseCase
import com.instructure.ngc.features.comments.domain.GetPendingCommentRetryUseCase
import com.instructure.ngc.features.comments.domain.GetSubmissionCommentsUseCase
import com.instructure.ngc.features.comments.domain.InsertFileUploadInputUseCase
import com.instructure.ngc.features.comments.domain.MarkFileCommentFailedUseCase
import com.instructure.ngc.features.comments.domain.MarkPendingCommentFailedUseCase
import com.instructure.ngc.features.comments.domain.MarkPendingCommentSendingUseCase
import com.instructure.ngc.features.comments.domain.MarkSubmissionCommentsReadUseCase
import com.instructure.ngc.features.comments.domain.ObservePendingSubmissionCommentsUseCase
import com.instructure.ngc.features.comments.domain.PrepareFileUploadRetryUseCase
import com.instructure.ngc.features.comments.domain.SaveCommentDraftUseCase
import com.instructure.ngc.features.comments.domain.SendTextCommentUseCase
import com.instructure.ngc.features.comments.domain.SubmissionCommentItem
import com.instructure.pandautils.features.file.upload.FileUploadEvent
import com.instructure.pandautils.features.file.upload.FileUploadEventHandler
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.services.NotoriousUploadWorker
import com.instructure.pandautils.utils.debounce
import com.instructure.pandautils.views.RecordingMediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SubmissionCommentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSubmissionCommentsUseCase: GetSubmissionCommentsUseCase,
    private val markSubmissionCommentsReadUseCase: MarkSubmissionCommentsReadUseCase,
    private val observePendingSubmissionCommentsUseCase: ObservePendingSubmissionCommentsUseCase,
    private val saveCommentDraftUseCase: SaveCommentDraftUseCase,
    private val createPendingTextCommentUseCase: CreatePendingTextCommentUseCase,
    private val sendTextCommentUseCase: SendTextCommentUseCase,
    private val createPendingFileCommentUseCase: CreatePendingFileCommentUseCase,
    private val completeFileCommentUseCase: CompleteFileCommentUseCase,
    private val markFileCommentFailedUseCase: MarkFileCommentFailedUseCase,
    private val createPendingMediaCommentUseCase: CreatePendingMediaCommentUseCase,
    private val completeMediaCommentUseCase: CompleteMediaCommentUseCase,
    private val markPendingCommentFailedUseCase: MarkPendingCommentFailedUseCase,
    private val getPendingCommentRetryUseCase: GetPendingCommentRetryUseCase,
    private val prepareFileUploadRetryUseCase: PrepareFileUploadRetryUseCase,
    private val markPendingCommentSendingUseCase: MarkPendingCommentSendingUseCase,
    private val insertFileUploadInputUseCase: InsertFileUploadInputUseCase,
    private val apiPrefs: ApiPrefs,
    private val fileUploadEventHandler: FileUploadEventHandler,
    private val crashlytics: FirebaseCrashlytics,
    @SubmissionCommentsIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SubmissionCommentsUiState(
            actions = SubmissionCommentsActions(
                onCommentChanged = ::onCommentChanged,
                onSendComment = ::onSendCommentClicked,
                onRetryComment = ::retryCommentUpload,
                onAddAttachment = ::onAddAttachment,
                onAttachmentTypeDialogDismiss = ::onAttachmentTypeDialogDismiss,
                onChooseFiles = ::onChooseFiles,
                onRecordAudio = ::onRecordAudio,
                onRecordVideo = ::onRecordVideo,
                onRecordDialogDismiss = ::onRecordDialogDismiss,
                onMediaRecorded = ::handleMediaRecording
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    private var courseId: Long = -1L
    private var assignmentId: Long = -1L
    private var studentId: Long = -1L
    private var selectedAttemptId: Long? = null

    private var pageId: String = ""
    private var submissionId: Long? = null
    private var loaded = false

    private var fetchedComments: MutableList<SubmissionCommentItem> = mutableListOf()
    private var pendingComments: List<SubmissionCommentItem> = emptyList()
    private var selectedFilePaths: List<String>? = null

    private var pendingCommentsJob: Job? = null
    private var fileUploadEventsJob: Job? = null

    /**
     * Entry point for the reusable component. Idempotent: the first call triggers loading; a later call
     * with a different selected attempt reloads for that attempt.
     */
    fun load(courseId: Long, assignmentId: Long, studentId: Long, attemptId: Long?) {
        val attemptChanged = loaded && attemptId != selectedAttemptId
        if (loaded && !attemptChanged) return
        this.courseId = courseId
        this.assignmentId = assignmentId
        this.studentId = studentId
        this.selectedAttemptId = attemptId
        loaded = true
        viewModelScope.launch { fetchData() }
    }

    private suspend fun fetchData() {
        _uiState.update { it.copy(isLoading = true) }
        pageId = "${apiPrefs.domain}-$courseId-$assignmentId-$studentId"

        try {
            val result = getSubmissionCommentsUseCase(
                GetSubmissionCommentsUseCase.Params(
                    courseId = courseId,
                    studentId = studentId,
                    assignmentId = assignmentId,
                    selectedAttemptId = selectedAttemptId
                )
            )
            submissionId = result.submissionId
            fetchedComments = result.comments.toMutableList()

            subscribeToPendingComments()
            subscribeToFileUploadEvents()
            markUnreadCommentsRead(result.comments)

            _uiState.update { state ->
                state.copy(
                    comments = fetchedComments + pendingComments,
                    isEmpty = fetchedComments.isEmpty() && pendingComments.isEmpty(),
                    isLoading = false,
                    errorMessage = null,
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    private fun markUnreadCommentsRead(comments: List<SubmissionCommentItem>) {
        val submissionId = submissionId ?: return
        val unreadIds = comments.filter { it.isUnread }.mapNotNull { it.id.toLongOrNull() }
        if (unreadIds.isEmpty()) return
        viewModelScope.launch {
            try {
                markSubmissionCommentsReadUseCase(
                    MarkSubmissionCommentsReadUseCase.Params(submissionId, unreadIds)
                )
            } catch (e: Exception) {
                crashlytics.recordException(e)
            }
        }
    }

    private suspend fun silentRefresh() {
        try {
            getSubmissionCommentsUseCase(
                GetSubmissionCommentsUseCase.Params(
                    courseId = courseId,
                    studentId = studentId,
                    assignmentId = assignmentId,
                    selectedAttemptId = selectedAttemptId,
                    forceNetwork = true
                )
            )
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }
    }

    private fun subscribeToFileUploadEvents() {
        fileUploadEventsJob?.cancel()
        fileUploadEventsJob = viewModelScope.launch {
            fileUploadEventHandler.events.collect { event ->
                when (event) {
                    is FileUploadEvent.UploadStarted -> onFileUploadStarted(event.workInfoLiveData, event.filePaths)
                    is FileUploadEvent.FileSelected -> selectedFilePaths = event.filePaths
                    is FileUploadEvent.DialogDismissed -> _uiState.update { it.copy(fileSelectorDialogData = null) }
                }
            }
        }
    }

    private fun subscribeToPendingComments() {
        pendingCommentsJob?.cancel()
        pendingCommentsJob = viewModelScope.launch(ioDispatcher) {
            observePendingSubmissionCommentsUseCase(ObservePendingSubmissionCommentsUseCase.Params(pageId))
                .collect { data ->
                    data.draftText?.let { draftText ->
                        if (draftText != _uiState.value.commentText) {
                            _uiState.update { it.copy(commentText = draftText) }
                        }
                    }

                    pendingComments = data.pendingComments

                    _uiState.update { state ->
                        state.copy(
                            comments = fetchedComments + pendingComments,
                            isEmpty = fetchedComments.isEmpty() && pendingComments.isEmpty()
                        )
                    }
                }
        }
    }

    private fun onCommentChanged(text: String) {
        debouncedSaveDraft(text)
        _uiState.update { it.copy(commentText = text) }
    }

    private fun onAddAttachment() {
        _uiState.update { it.copy(showAttachmentTypeDialog = true) }
    }

    private fun onAttachmentTypeDialogDismiss() {
        _uiState.update { it.copy(showAttachmentTypeDialog = false) }
    }

    private fun onChooseFiles() {
        _uiState.update {
            it.copy(
                showAttachmentTypeDialog = false,
                fileSelectorDialogData = FileSelectorDialogData(
                    assignmentId = assignmentId,
                    courseId = courseId,
                    userId = studentId,
                    attempt = selectedAttemptId
                )
            )
        }
    }

    private fun onRecordAudio() {
        _uiState.update { it.copy(showAttachmentTypeDialog = false, showRecordFloatingView = RecordingMediaType.Audio) }
    }

    private fun onRecordVideo() {
        _uiState.update { it.copy(showAttachmentTypeDialog = false, showRecordFloatingView = RecordingMediaType.Video) }
    }

    private fun onRecordDialogDismiss() {
        _uiState.update { it.copy(showRecordFloatingView = null) }
    }

    private fun onSendCommentClicked() {
        viewModelScope.launch(ioDispatcher) {
            if (_uiState.value.commentText.isNotEmpty()) {
                val comment = _uiState.value.commentText
                _uiState.update { it.copy(commentText = "") }
                val id = createPendingComment(comment)
                sendComment(id, comment)
            }
        }
    }

    private suspend fun createPendingComment(commentText: String): Long {
        return createPendingTextCommentUseCase(
            CreatePendingTextCommentUseCase.Params(
                pageId = pageId,
                comment = commentText,
                attemptId = selectedAttemptId
            )
        )
    }

    private suspend fun sendComment(id: Long, comment: String) {
        try {
            val sent = sendTextCommentUseCase(
                SendTextCommentUseCase.Params(
                    pendingId = id,
                    submissionId = submissionId,
                    comment = comment,
                    attemptId = selectedAttemptId
                )
            )
            fetchedComments.add(sent)
            _uiState.update { it.copy(comments = fetchedComments + pendingComments, commentText = "") }
            silentRefresh()
        } catch (e: Throwable) {
            viewModelScope.ensureActive()
        }
    }

    private fun retryCommentUpload(comment: SubmissionCommentItem) {
        viewModelScope.launch(ioDispatcher) {
            val commentId = comment.id.toLongOrNull() ?: return@launch
            val data = getPendingCommentRetryUseCase(GetPendingCommentRetryUseCase.Params(commentId)) ?: return@launch

            if (!data.comment.isNullOrEmpty()) {
                sendComment(commentId, comment.content)
            } else if (data.workerId != null) {
                prepareFileUploadRetryUseCase(
                    PrepareFileUploadRetryUseCase.Params(commentId = commentId, workerId = data.workerId)
                )
                data.filePaths?.let { restartWorker(it) }
            } else if (!data.filePath.isNullOrEmpty()) {
                markPendingCommentSendingUseCase(MarkPendingCommentSendingUseCase.Params(commentId))
                enqueueMediaRecording(file = File(data.filePath), id = commentId)
            }
        }
    }

    private fun onFileUploadStarted(workInfoLiveData: LiveData<WorkInfo?>, filePaths: List<String>) {
        _uiState.update { it.copy(fileSelectorDialogData = null, showAttachmentTypeDialog = false) }
        viewModelScope.launch {
            workInfoLiveData.asFlow().collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> createPendingFileComment(workInfo, filePaths)
                    WorkInfo.State.SUCCEEDED -> handleFileUploadSuccess(workInfo)
                    WorkInfo.State.FAILED -> handleFileUploadFailure(workInfo)
                    else -> {}
                }
            }
        }
    }

    private suspend fun createPendingFileComment(workInfo: WorkInfo, filePaths: List<String>) {
        createPendingFileCommentUseCase(
            CreatePendingFileCommentUseCase.Params(
                workerId = workInfo.id.toString(),
                courseId = courseId,
                assignmentId = assignmentId,
                studentId = studentId,
                filePaths = filePaths,
                pageId = pageId,
                attemptId = selectedAttemptId
            )
        )
    }

    private suspend fun handleFileUploadSuccess(workInfo: WorkInfo) {
        val submissionCommentId = workInfo.outputData.getLong(FileUploadWorker.RESULT_SUBMISSION_COMMENT, 0L)
        val sent = completeFileCommentUseCase(
            CompleteFileCommentUseCase.Params(
                workerId = workInfo.id.toString(),
                submissionCommentId = submissionCommentId
            )
        )
        sent?.let {
            fetchedComments.add(it)
            _uiState.update { state -> state.copy(comments = fetchedComments + pendingComments, commentText = "") }
            silentRefresh()
        }
    }

    private suspend fun handleFileUploadFailure(workInfo: WorkInfo) {
        markFileCommentFailedUseCase(MarkFileCommentFailedUseCase.Params(workerId = workInfo.id.toString()))
    }

    private fun handleMediaRecording(file: File) {
        viewModelScope.launch {
            val id = createPendingMediaCommentUseCase(
                CreatePendingMediaCommentUseCase.Params(
                    pageId = pageId,
                    filePath = file.path,
                    attemptId = selectedAttemptId
                )
            )
            enqueueMediaRecording(file, id)
        }
    }

    private suspend fun enqueueMediaRecording(file: File, id: Long) {
        NotoriousUploadWorker.enqueueUpload(
            context = context,
            mediaFilePath = Uri.fromFile(file).path,
            assignment = Assignment(id = assignmentId, courseId = courseId),
            studentId = studentId,
            isGroupComment = false,
            pageId = pageId,
            attemptId = selectedAttemptId,
            mediaCommentId = id
        ).collect { result ->
            when (result?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    val sent = completeMediaCommentUseCase(
                        CompleteMediaCommentUseCase.Params(pendingId = id, file = file)
                    )
                    fetchedComments.add(sent)
                    _uiState.update { state -> state.copy(comments = fetchedComments + pendingComments) }
                    silentRefresh()
                }

                WorkInfo.State.FAILED -> {
                    markPendingCommentFailedUseCase(MarkPendingCommentFailedUseCase.Params(commentId = id))
                }

                else -> {}
            }
        }
    }

    private fun restartWorker(filePaths: List<String>) {
        viewModelScope.launch {
            val worker = OneTimeWorkRequestBuilder<FileUploadWorker>().build()
            insertFileUploadInputUseCase(
                InsertFileUploadInputUseCase.Params(
                    workerId = worker.id.toString(),
                    filePaths = filePaths,
                    courseId = courseId,
                    assignmentId = assignmentId,
                    studentId = studentId,
                    attemptId = selectedAttemptId
                )
            )
            WorkManager.getInstance(context).apply {
                onFileUploadStarted(getWorkInfoByIdLiveData(worker.id), filePaths)
                enqueue(worker)
            }
        }
    }

    private val debouncedSaveDraft = debounce<String>(waitMs = 300, coroutineScope = viewModelScope) {
        saveCommentDraftUseCase(
            SaveCommentDraftUseCase.Params(
                pageId = pageId,
                text = _uiState.value.commentText,
                attemptId = selectedAttemptId
            )
        )
    }
}
