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
package com.instructure.pandautils.features.speedgrader.grade.comments

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.FileUploadWorkerData
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.features.speedgrader.SpeedGraderSelectedAttemptHolder
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.SubmissionCommentWithAttachments
import com.instructure.pandautils.services.NotoriousUploadWorker
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.views.RecordingMediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SpeedGraderCommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val speedGraderCommentsRepository: SpeedGraderCommentsRepository,
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val fileUploadInputDao: FileUploadInputDao,
    private val submissionCommentDao: SubmissionCommentDao,
    private val attachmentDao: AttachmentDao,
    private val authorDao: AuthorDao,
    private val mediaCommentDao: MediaCommentDao,
    private val apiPrefs: ApiPrefs,
    private val speedGraderSelectedAttemptHolder: SpeedGraderSelectedAttemptHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedGraderCommentsUiState())
    val uiState = _uiState.asStateFlow()

    private val assignmentId: Long = savedStateHandle.get<Long>(ASSIGNMENT_ID_KEY) ?: -1L
    private val submissionId: Long = savedStateHandle.get<Long>(SUBMISSION_ID_KEY) ?: -1L
    private val courseId: Long = savedStateHandle.get<Long>(Const.COURSE_ID) ?: -1L

    private var userId = -1L
    private var pageId: String = ""
    private var selectedAttemptId: Long? = null

    private var fetchedComments: MutableList<SpeedGraderComment> = mutableListOf()
    private var pendingComments: List<SpeedGraderComment> = emptyList()
    private var selectedFilePaths: List<String>? = null
    private var assignmentEnhancementsEnabled = false

    init {
        viewModelScope.launch {
            speedGraderSelectedAttemptHolder.selectedAttemptIdFlowFor(submissionId).collectLatest { attemptId ->
                selectedAttemptId = attemptId
                fetchData()
            }
        }
    }

    private suspend fun fetchData() {
        assignmentEnhancementsEnabled = speedGraderCommentsRepository.getCourseFeatures(courseId).contains("assignments_2_student")
        val response = speedGraderCommentsRepository.getSubmissionComments(submissionId, assignmentId)
        userId = response.data.submission?.userId?.toLong() ?: -1L
        pageId = "${apiPrefs.domain}-$courseId-$assignmentId-$userId"
        collectPendingComments()
        fetchedComments = response.comments
            .filter { it.attempt.toLong() == selectedAttemptId || !assignmentEnhancementsEnabled }
            .map { node ->
                node.let {
                    SpeedGraderComment(
                        id = it.mediaCommentId ?: "",
                        authorName = it.author?.name ?: "Unknown",
                        authorId = it.author?._id ?: "",
                        authorAvatarUrl = it.author?.avatarUrl ?: "",
                        content = it.comment ?: "",
                        createdAt = it.createdAt.toString(),
                        isOwnComment = apiPrefs.user?.id?.toString() == it.author?._id,
                        attachments = getAttachments(it.attachments.orEmpty()),
                        mediaObject = it.mediaObject?.let { mediaObject ->
                            SpeedGraderMediaObject(
                                id = mediaObject._id,
                                mediaDownloadUrl = mediaObject.mediaDownloadUrl,
                                title = mediaObject.title,
                                mediaType = if (mediaObject.title?.endsWith(".mp4") == true) { // TODO Check mediaType field if the query is fixed in ticket EVAL-5640
                                    MediaType.VIDEO
                                } else {
                                    MediaType.AUDIO
                                },
                                thumbnailUrl = mediaObject.thumbnailUrl
                            )
                        }
                    )
                }
            }.toMutableList()

        _uiState.update { state ->
            state.copy(
                comments = fetchedComments + pendingComments,
                isLoading = false,
            )
        }
    }

    // TODO remove; We need this now, because the GraphQL query doesn't return file verifiers.
    private suspend fun getAttachments(attachments: List<SubmissionCommentsQuery.Attachment>) = attachments.map {
        val submission = speedGraderCommentsRepository.getSingleSubmission(courseId, assignmentId, userId)
        val attachmentWithVerifier = submission?.submissionComments
            ?.flatMap { attachmentsWithVerifier -> attachmentsWithVerifier.attachments }
            ?.find { attachmentWithVerifier -> attachmentWithVerifier.id == it._id.toLongOrNull() }

        SpeedGraderCommentAttachment(
            id = it.id,
            url = attachmentWithVerifier?.url ?: it.url ?: "",
            thumbnailUrl = it.thumbnailUrl,
            createdAt = it.createdAt.toString(),
            title = it.title ?: "",
            displayName = it.displayName ?: "",
            contentType = it.contentType ?: "",
            size = it.size ?: "",
        )
    }

    private fun collectPendingComments() {
        viewModelScope.launch(Dispatchers.IO) {
            pendingSubmissionCommentDao.findByPageIdFlow(pageId)
                .collect { pendingCommentsEntities ->
                    pendingComments = pendingCommentsEntities
                        .orEmpty()
                        .map { it.pendingSubmissionCommentEntity.toApiModel() }
                        .map { pendingComment ->
                            SpeedGraderComment(
                                id = pendingComment.id.toString(),
                                authorName = apiPrefs.user?.name ?: "",
                                authorId = apiPrefs.user?.id?.toString() ?: "",
                                authorAvatarUrl = apiPrefs.user?.avatarUrl ?: "",
                                content = pendingComment.comment ?: "",
                                createdAt = DateHelper.longToSpeedGraderDateString(pendingComment.date.time)
                                    ?: "",
                                isOwnComment = true,
                                attachments = emptyList(),
                                isPending = true
                            )
                        }
                    _uiState.update { state ->
                        state.copy(
                            comments = fetchedComments + pendingComments,
                            isEmpty = fetchedComments.isEmpty() && pendingComments.isEmpty()
                        )
                    }
                }
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
                _uiState.update { state ->
                    state.copy(showAttachmentTypeDialog = true)
                }
            }

            SpeedGraderCommentsAction.SendCommentClicked -> {
                onSendCommentClicked()
            }

            SpeedGraderCommentsAction.AttachmentTypeSelectorDialogClosed -> {
                _uiState.update { state ->
                    state.copy(showAttachmentTypeDialog = false)
                }
            }

            SpeedGraderCommentsAction.ChooseFilesClicked -> {
                _uiState.update { state ->
                    state.copy(
                        showAttachmentTypeDialog = false,
                        fileSelectorDialogData = SpeedGraderFileSelectorDialogData(
                            assignmentId = assignmentId,
                            courseId = courseId,
                            userId = userId,
                            attempt = selectedAttemptId
                        )
                    )
                }
            }

            SpeedGraderCommentsAction.RecordAudioClicked -> {
                _uiState.update { state ->
                    state.copy(
                        showAttachmentTypeDialog = false,
                        showRecordFloatingView = RecordingMediaType.Audio
                    )
                }
            }

            SpeedGraderCommentsAction.RecordVideoClicked -> {
                _uiState.update { state ->
                    state.copy(
                        showAttachmentTypeDialog = false,
                        showRecordFloatingView = RecordingMediaType.Video
                    )
                }
            }

            SpeedGraderCommentsAction.AttachmentRecordDialogClosed -> {
                _uiState.update { state ->
                    state.copy(showRecordFloatingView = null)
                }
            }

            is SpeedGraderCommentsAction.MediaRecorded -> {
                handleMediaRecording(action.file)
            }

            SpeedGraderCommentsAction.FileUploadDialogClosed -> {
                _uiState.update { state ->
                    state.copy(fileSelectorDialogData = null)
                }
            }

            is SpeedGraderCommentsAction.FileUploadStarted -> {
                onFileUploadStarted(action.workInfoLiveData)
            }

            is SpeedGraderCommentsAction.FilesSelected -> {
                selectedFilePaths = action.filePaths
            }
        }
    }

    private fun onFileUploadStarted(workInfoLiveData: LiveData<WorkInfo>) {
        _uiState.update { state ->
            state.copy(
                fileSelectorDialogData = null,
                showAttachmentTypeDialog = false
            )
        }
        // Subscribe to the worker's LiveData to observe its state
        viewModelScope.launch {
            workInfoLiveData.asFlow().collect { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> createPendingFileComment(workInfo)
                    WorkInfo.State.SUCCEEDED -> handleFileUploadSuccess(workInfo)
                    WorkInfo.State.FAILED -> handleFileUploadFailure(workInfo)
                    else -> {}
                }
            }
        }
    }

    private suspend fun createPendingFileComment(workInfo: WorkInfo) {
        var fileUploadInput = fileUploadInputDao.findByWorkerId(workInfo.id.toString())
        if (fileUploadInput == null) {
            fileUploadInput = FileUploadInputEntity(
                workerId = workInfo.id.toString(),
                courseId = courseId,
                assignmentId = assignmentId,
                userId = userId,
                filePaths = selectedFilePaths.orEmpty(),
                action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT,
                attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
            )
            fileUploadInputDao.insert(fileUploadInput)
        }

        val comment = pendingSubmissionCommentDao.findByPageId(pageId)?.find {
            it.fileUploadInput?.workerId == workInfo.id.toString()
        }
        if (comment == null) {
            val newComment = PendingSubmissionComment(pageId).apply {
                this.workerId = workInfo.id
                this.status = CommentSendStatus.SENDING
                this.workerInputData = FileUploadWorkerData(
                    selectedFilePaths.orEmpty(),
                    courseId,
                    assignmentId,
                    userId
                )
                this.attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
            }
            pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        }
    }

    private suspend fun handleFileUploadSuccess(workInfo: WorkInfo) {
        pendingSubmissionCommentDao.findByWorkerId(workInfo.id.toString())?.let { pending ->
            pendingSubmissionCommentDao.delete(pending)

            val submissionCommentId =
                workInfo.outputData.getLong(FileUploadWorker.RESULT_SUBMISSION_COMMENT, 0L)
            submissionCommentDao.findById(submissionCommentId)?.let {
                fetchedComments.add(
                    SpeedGraderComment(
                        id = it.submissionComment.id.toString(),
                        authorName = it.author?.displayName ?: apiPrefs.user?.name ?: "",
                        authorId = it.author?.id?.toString() ?: apiPrefs.user?.id?.toString() ?: "",
                        authorAvatarUrl = it.author?.avatarImageUrl ?: apiPrefs.user?.avatarUrl
                        ?: "",
                        content = it.submissionComment.comment ?: "",
                        createdAt = DateHelper.longToSpeedGraderDateString(it.submissionComment.createdAt?.time)
                            ?: "",
                        isOwnComment = true,
                        attachments = it.attachments?.map { attachment ->
                            SpeedGraderCommentAttachment(
                                id = attachment.id.toString(),
                                url = attachment.url ?: "",
                                thumbnailUrl = attachment.thumbnailUrl,
                                createdAt = attachment.createdAt.toString(),
                                title = attachment.displayName ?: attachment.filename ?: "",
                                displayName = attachment.displayName ?: "",
                                contentType = attachment.contentType ?: "",
                                size = attachment.size.toString()
                            )
                        } ?: emptyList(),
                        isPending = false
                    )
                )
                dbCleanUp(it)
                _uiState.update { state ->
                    state.copy(
                        comments = fetchedComments + pendingComments,
                        commentText = TextFieldValue("")
                    )
                }
                // TODO clear cache
            }
        }
    }

    private suspend fun handleFileUploadFailure(workInfo: WorkInfo) {
        // TODO Handle file upload failure
    }

    private suspend fun dbCleanUp(submissionComment: SubmissionCommentWithAttachments) {
        submissionComment.author?.let { authorDao.delete(it) }
        submissionComment.mediaComment?.let { mediaCommentDao.delete(it) }
        submissionComment.attachments?.let { attachmentDao.deleteAll(it) }
        submissionCommentDao.delete(submissionComment.submissionComment)
    }

    private fun handleMediaRecording(file: File) {
        viewModelScope.launch {
            val id = createPendingMediaComment(file.path)
            NotoriousUploadWorker.enqueueUpload(
                context = context,
                mediaFilePath = Uri.fromFile(file).path,
                assignment = Assignment(id = assignmentId, courseId = courseId),
                studentId = userId,
                isGroupComment = false, // TODO handle group comments
                pageId = pageId,
                attemptId = selectedAttemptId,
                mediaCommentId = id
            ).collect { result ->
                when (result.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        pendingSubmissionCommentDao.findById(id)?.let {
                            pendingSubmissionCommentDao.delete(it)
                        }
                        // TODO add the new comment to the UI state
                    }

                    WorkInfo.State.FAILED -> {
                        pendingSubmissionCommentDao.findById(id)?.let {
                            it.status = CommentSendStatus.ERROR.toString()
                            pendingSubmissionCommentDao.update(it)
                        }
                    }

                    else -> {
                        // Do nothing for other states
                    }
                }
            }
        }
    }

    private suspend fun createPendingMediaComment(filePath: String): Long {
        val newComment = PendingSubmissionComment(pageId).apply {
            attemptId = selectedAttemptId?.takeIf { assignmentEnhancementsEnabled }
        }
        newComment.filePath = filePath
        newComment.status = CommentSendStatus.SENDING
        // TODO fix this
        val id = pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        return id
    }

    private suspend fun createPendingComment(commentText: String): Long {
        val drafts = pendingSubmissionCommentDao.findByPageId(pageId)
            .orEmpty()
            .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }
        pendingSubmissionCommentDao.deleteAll(drafts.map { it.pendingSubmissionCommentEntity })
        val newComment = PendingSubmissionComment(pageId, commentText).apply {
            attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
        }
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        return newComment.id
    }

    private suspend fun sendComment(id: Long, comment: String) {
        pendingSubmissionCommentDao.findById(id)?.let {
            it.status = CommentSendStatus.SENDING.toString()
            pendingSubmissionCommentDao.update(it)
        }

        try {
            // TODO remove after testing, this is just to simulate a delay for the UI
            delay(3000)
            val newComment = speedGraderCommentsRepository.createSubmissionComment(
                submissionId,
                comment
            )
            pendingSubmissionCommentDao.findById(id)?.let {
                pendingSubmissionCommentDao.delete(it)
            }
            fetchedComments.add(
                SpeedGraderComment(
                    id = newComment.createSubmissionComment?.submissionComment?._id ?: "",
                    authorName = apiPrefs.user?.name ?: "",
                    authorId = apiPrefs.user?.id?.toString() ?: "",
                    authorAvatarUrl = apiPrefs.user?.avatarUrl ?: "",
                    content = comment,
                    createdAt = newComment.createSubmissionComment?.submissionComment?.createdAt.toString(),
                    isOwnComment = true,
                    attachments = emptyList(),
                    isPending = false
                )
            )
            _uiState.update { state ->
                state.copy(
                    comments = fetchedComments + pendingComments,
                    commentText = TextFieldValue("")
                )
            }
        } catch (e: Throwable) {
            pendingSubmissionCommentDao.findById(id)?.let {
                it.status = CommentSendStatus.ERROR.toString()
                pendingSubmissionCommentDao.update(it)
            }

            // TODO implement error state in UI and in SpeedGraderComment class
            viewModelScope.ensureActive()
        }
    }

    private fun onSendCommentClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value.commentText.text.isNotEmpty()) {
                val comment = _uiState.value.commentText.text
                _uiState.update { state ->
                    state.copy(
                        commentText = TextFieldValue("")
                    )
                }

                val id = createPendingComment(comment)
                sendComment(id, comment)
            }
        }
    }

    companion object {
        const val ASSIGNMENT_ID_KEY = "assignmentId"
        const val SUBMISSION_ID_KEY = "submissionId"
    }
}
