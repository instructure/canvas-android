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
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.FileUploadWorkerData
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadEvent
import com.instructure.pandautils.features.file.upload.FileUploadEventHandler
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
import com.instructure.pandautils.utils.debounce
import com.instructure.pandautils.views.RecordingMediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
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
    private val speedGraderSelectedAttemptHolder: SpeedGraderSelectedAttemptHolder,
    private val fileUploadEventHandler: FileUploadEventHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedGraderCommentsUiState())
    val uiState = _uiState.asStateFlow()

    private val assignmentId: Long = savedStateHandle.get<Long>(ASSIGNMENT_ID_KEY) ?: -1L
    private val studentId: Long = savedStateHandle.get<Long>(SUBMISSION_ID_KEY) ?: -1L
    private val courseId: Long = savedStateHandle.get<Long>(Const.COURSE_ID) ?: -1L

    private var pageId: String = ""
    private var selectedAttemptId: Long? = null
    private var submissionId: Long? = null

    private var fetchedComments: MutableList<SpeedGraderComment> = mutableListOf()
    private var pendingComments: List<SpeedGraderComment> = emptyList()
    private var selectedFilePaths: List<String>? = null
    private var assignmentEnhancementsEnabled = false

    init {
        viewModelScope.launch {
            speedGraderSelectedAttemptHolder.selectedAttemptIdFlowFor(studentId).collectLatest { attemptId ->
                selectedAttemptId = attemptId
                fetchData()
            }
        }
    }

    private suspend fun fetchData() {
        _uiState.update {
            it.copy(isLoading = true)
        }

        assignmentEnhancementsEnabled = speedGraderCommentsRepository.getCourseFeatures(courseId).contains("assignments_2_student")
        val response = speedGraderCommentsRepository.getSubmissionComments(studentId, assignmentId)
        pageId = "${apiPrefs.domain}-$courseId-$assignmentId-$studentId"
        submissionId = response.data.submission?._id?.toLongOrNull()
        subscribeToPendingComments()
        subscribeToFileUploadEvents()
        val isAnonymousGrading = response.data.submission?.assignment?.anonymousGrading ?: false
        fetchedComments = response.comments
            .filter { (it.attempt.toLong() == 0L && selectedAttemptId == 1L) || it.attempt.toLong() == selectedAttemptId || !assignmentEnhancementsEnabled }
            .map { node ->
                node.let {
                    val isOwnComment = apiPrefs.user?.id?.toString() == it.author?._id
                    SpeedGraderComment(
                        id = it.mediaCommentId.orEmpty(),
                        authorName = getAuthorName(
                            authorName = it.author?.name,
                            anonymousGrading = isAnonymousGrading,
                            isOwnComment = isOwnComment
                        ),
                        authorId = it.author?._id.orEmpty(),
                        authorAvatarUrl = it.author?.avatarUrl.takeIf {
                            isOwnComment || !isAnonymousGrading
                        },
                        content = it.comment.orEmpty(),
                        createdAt = it.createdAt,
                        isOwnComment = isOwnComment,
                        attachments = getAttachments(it.attachments.orEmpty()),
                        mediaObject = it.mediaObject?.let { mediaObject ->
                            val mediaSource = mediaObject.mediaSources?.firstOrNull()
                            SpeedGraderMediaObject(
                                id = mediaObject._id,
                                mediaDownloadUrl = mediaSource?.url,
                                title = mediaObject.title,
                                mediaType = if (mediaObject.title?.endsWith(".mp4") == true) { // TODO Check mediaType field if the query is fixed in ticket EVAL-5640
                                    MediaType.VIDEO
                                } else {
                                    MediaType.AUDIO
                                },
                                thumbnailUrl = mediaObject.thumbnailUrl,
                                contentType = mediaSource?.contentType
                            )
                        }
                    )
                }
            }.toMutableList()

        _uiState.update { state ->
            state.copy(
                comments = fetchedComments + pendingComments,
                isEmpty = fetchedComments.isEmpty() && pendingComments.isEmpty(),
                isLoading = false,
            )
        }
    }

    private fun getAuthorName(
        authorName: String?,
        anonymousGrading: Boolean,
        isOwnComment: Boolean
    ) = when {
        isOwnComment || !anonymousGrading -> authorName.orEmpty()
        else -> context.getString(R.string.anonymousGradingUserLabel)
    }

    private suspend fun silentRefresh() {
        try {
            speedGraderCommentsRepository.getSubmissionComments(studentId, assignmentId, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // TODO remove; We need this now, because the GraphQL query doesn't return file verifiers.
    private suspend fun getAttachments(attachments: List<SubmissionCommentsQuery.Attachment>) = attachments.map {
        val submission = speedGraderCommentsRepository.getSingleSubmission(courseId, assignmentId, studentId)
        val attachmentWithVerifier = submission?.submissionComments
            ?.flatMap { attachmentsWithVerifier -> attachmentsWithVerifier.attachments }
            ?.find { attachmentWithVerifier -> attachmentWithVerifier.id == it._id.toLongOrNull() }

        SpeedGraderCommentAttachment(
            id = it.id,
            url = attachmentWithVerifier?.url ?: it.url ?: "",
            thumbnailUrl = it.thumbnailUrl,
            createdAt = it.createdAt,
            title = it.title ?: "",
            displayName = it.displayName ?: "",
            contentType = it.contentType ?: "",
            size = it.size ?: "",
        )
    }

    private fun subscribeToFileUploadEvents() {
        viewModelScope.launch {
            fileUploadEventHandler.events.collect { event ->
                Log.d("ASDF", "FileUploadEvent: $event")
                when (event) {
                    is FileUploadEvent.UploadStarted -> {
                        onFileUploadStarted(event.workInfoLiveData)
                    }
                    is FileUploadEvent.FileSelected -> {
                        selectedFilePaths = event.filePaths
                    }
                    is FileUploadEvent.DialogDismissed -> {
                        _uiState.update { state ->
                            state.copy(fileSelectorDialogData = null)
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToPendingComments() {
        viewModelScope.launch(Dispatchers.IO) {
            pendingSubmissionCommentDao.findByPageIdFlow(pageId)
                .collect { pendingCommentsEntities ->
                    val (drafts, pending) = pendingCommentsEntities
                        .orEmpty()
                        .map { it.pendingSubmissionCommentEntity.toApiModel() }
                        .partition { it.status == CommentSendStatus.DRAFT }

                    drafts.firstOrNull()?.let { draft ->
                        if (draft.comment != _uiState.value.commentText) {
                            _uiState.update {
                                it.copy(commentText = draft.comment.orEmpty())
                            }
                        }
                    }

                    pendingComments = pending.map { pendingComment ->
                        SpeedGraderComment(
                            id = pendingComment.id.toString(),
                            authorName = apiPrefs.user?.name.orEmpty(),
                            authorId = apiPrefs.user?.id?.toString().orEmpty(),
                            authorAvatarUrl = apiPrefs.user?.avatarUrl.orEmpty(),
                            content = pendingComment.comment.orEmpty(),
                            createdAt = pendingComment.date,
                            isOwnComment = true,
                            attachments = emptyList(),
                            isPending = pendingComment.status == CommentSendStatus.SENDING,
                            isFailed = pendingComment.status == CommentSendStatus.ERROR,
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
                debouncedSaveDraft(action.commentText)
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

            is SpeedGraderCommentsAction.RetryCommentUpload -> {
                retryCommentUpload(action.comment)
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
                            userId = studentId,
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
                userId = studentId,
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
                    studentId
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
                        createdAt = it.submissionComment.createdAt,
                        isOwnComment = true,
                        attachments = it.attachments?.map { attachment ->
                            SpeedGraderCommentAttachment(
                                id = attachment.id.toString(),
                                url = attachment.url ?: "",
                                thumbnailUrl = attachment.thumbnailUrl,
                                createdAt = attachment.createdAt,
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
                        commentText = ""
                    )
                }
                silentRefresh()
            }
        }
    }

    private suspend fun handleFileUploadFailure(workInfo: WorkInfo) {
        pendingSubmissionCommentDao.findByWorkerIdWithInputData(workInfo.id.toString())?.apply {
            this.pendingSubmissionCommentEntity.status = CommentSendStatus.ERROR.toString()
        }?.let {
            pendingSubmissionCommentDao.update(it.pendingSubmissionCommentEntity)
        }
    }

    private suspend fun dbCleanUp(submissionComment: SubmissionCommentWithAttachments) {
        submissionComment.author?.let { authorDao.delete(it) }
        submissionComment.mediaComment?.let { mediaCommentDao.delete(it) }
        submissionComment.attachments?.let { attachmentDao.deleteAll(it) }
        submissionCommentDao.delete(submissionComment.submissionComment)
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
            when (result.state) {
                WorkInfo.State.SUCCEEDED -> {
                    fetchedComments.add(
                        SpeedGraderComment(
                            id = id.toString(),
                            authorName = apiPrefs.user?.name.orEmpty(),
                            authorId = apiPrefs.user?.id?.toString().orEmpty(),
                            authorAvatarUrl = apiPrefs.user?.avatarUrl.orEmpty(),
                            createdAt = Date(),
                            isOwnComment = true,
                            mediaObject = SpeedGraderMediaObject(
                                id = id.toString(),
                                mediaDownloadUrl = file.path,
                                title = null,
                                mediaType = if (file.extension == "mp4") {
                                    MediaType.VIDEO
                                } else {
                                    MediaType.AUDIO
                                },
                                thumbnailUrl = null,
                                contentType = MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(file.extension)
                            ),
                            isPending = false
                        )
                    )
                    _uiState.update { state ->
                        state.copy(
                            comments = fetchedComments + pendingComments
                        )
                    }
                    silentRefresh()
                }

                WorkInfo.State.FAILED -> {
                    // Handled in SGPendingMediaCommentReceiver
                }

                else -> {
                    // Do nothing for other states
                }
            }
        }

    }

    private fun handleMediaRecording(file: File) {
        viewModelScope.launch {
            val id = createPendingMediaComment(file.path)
            enqueueMediaRecording(file, id)
        }
    }

    private suspend fun createPendingMediaComment(filePath: String): Long {
        val newComment = PendingSubmissionComment(pageId).apply {
            this.filePath = filePath
            status = CommentSendStatus.SENDING
            attemptId = selectedAttemptId?.takeIf { assignmentEnhancementsEnabled }
        }
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
            val newComment = speedGraderCommentsRepository.createSubmissionComment(
                submissionId ?: throw IllegalStateException("Submission ID is null"),
                comment,
                selectedAttemptId?.toInt()
            )
            pendingSubmissionCommentDao.deleteById(id)
            fetchedComments.add(
                SpeedGraderComment(
                    id = newComment.createSubmissionComment?.submissionComment?._id ?: "",
                    authorName = apiPrefs.user?.name ?: "",
                    authorId = apiPrefs.user?.id?.toString() ?: "",
                    authorAvatarUrl = apiPrefs.user?.avatarUrl ?: "",
                    content = comment,
                    createdAt = newComment.createSubmissionComment?.submissionComment?.createdAt,
                    isOwnComment = true,
                    attachments = emptyList(),
                    isPending = false
                )
            )
            _uiState.update { state ->
                state.copy(
                    comments = fetchedComments + pendingComments,
                    commentText = ""
                )
            }
            silentRefresh()
        } catch (e: Throwable) {
            pendingSubmissionCommentDao.findById(id)?.let {
                it.status = CommentSendStatus.ERROR.toString()
                pendingSubmissionCommentDao.update(it)
            }
            viewModelScope.ensureActive()
        }
    }

    private fun retryCommentUpload(comment: SpeedGraderComment) {
        viewModelScope.launch(Dispatchers.IO) {
            val pending = pendingSubmissionCommentDao.findById(comment.id.toLong())
            pending?.let {
                if (!pending.comment.isNullOrEmpty()) {
                    // Text comment
                    sendComment(comment.id.toLong(), comment.content)
                } else if (pending.workerId != null && pending.workerId != "null") {
                    // File upload comment
                    val fileUploadInputEntity = fileUploadInputDao.findByWorkerId(pending.workerId.toString())
                    if (fileUploadInputEntity != null) {
                        fileUploadInputDao.delete(fileUploadInputEntity)
                    }
                    pendingSubmissionCommentDao.delete(pending)
                    fileUploadInputEntity?.filePaths?.let {
                        restartWorker(it)
                    }
                } else if (!pending.filePath.isNullOrEmpty()) {
                    // Media recording comment
                    pending.filePath?.let {
                        pending.status = CommentSendStatus.SENDING.toString()
                        pendingSubmissionCommentDao.update(pending)
                        enqueueMediaRecording(
                            file = File(it),
                            id = comment.id.toLong()
                        )
                    }
                }
            }
        }
    }

    private fun restartWorker(filePaths: List<String>) {
        viewModelScope.launch {
            val worker = OneTimeWorkRequestBuilder<FileUploadWorker>()
                .build()

            val inputData = FileUploadInputEntity(
                workerId = worker.id.toString(),
                filePaths = filePaths,
                courseId = courseId,
                assignmentId = assignmentId,
                userId = studentId,
                action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT,
                attemptId = selectedAttemptId
            )

            fileUploadInputDao.insert(inputData)

            WorkManager.getInstance(context).apply {
                onFileUploadStarted(getWorkInfoByIdLiveData(worker.id))
                enqueue(worker)
            }
        }
    }

    private fun onSendCommentClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value.commentText.isNotEmpty()) {
                val comment = _uiState.value.commentText
                _uiState.update { state ->
                    state.copy(
                        commentText = ""
                    )
                }

                val id = createPendingComment(comment)
                sendComment(id, comment)
            }
        }
    }

    private val debouncedSaveDraft = debounce<String>(
        waitMs = 300,
        coroutineScope = viewModelScope
    ) {
        val commentText = _uiState.value.commentText

        val currentDrafts = pendingSubmissionCommentDao.findByPageId(pageId)
            .orEmpty()
            .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }

        pendingSubmissionCommentDao.deleteAll(currentDrafts.map { it.pendingSubmissionCommentEntity })

        val newComment = PendingSubmissionComment(pageId, commentText).apply {
            attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
        }

        if (commentText.isNotBlank()) {
            pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        }
    }

    companion object {
        const val ASSIGNMENT_ID_KEY = "assignmentId"
        const val SUBMISSION_ID_KEY = "submissionId"
    }
}
