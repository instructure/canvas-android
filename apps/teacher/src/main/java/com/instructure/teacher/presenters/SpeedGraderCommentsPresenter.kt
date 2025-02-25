/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.presenters

import androidx.work.WorkInfo
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.models.postmodels.FileUploadWorkerData
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.room.appdatabase.daos.*
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import com.instructure.pandautils.room.appdatabase.model.SubmissionCommentWithAttachments
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.SubmissionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.models.CommentWrapper
import com.instructure.teacher.models.PendingCommentWrapper
import com.instructure.teacher.models.SubmissionCommentWrapper
import com.instructure.teacher.models.SubmissionWrapper
import com.instructure.teacher.viewinterface.SpeedGraderCommentsView
import com.instructure.pandautils.blueprint.ListPresenter
import kotlinx.coroutines.Job
import java.util.*

class SpeedGraderCommentsPresenter(
    rawComments: List<SubmissionComment>,
    val submissionHistory: List<Submission>,
    val assignee: Assignee,
    val courseId: Long,
    val assignmentId: Long,
    val groupMessage: Boolean,
    val submissionCommentDao: SubmissionCommentDao,
    val attachmentDao: AttachmentDao,
    val authorDao: AuthorDao,
    val mediaCommentDao: MediaCommentDao,
    val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    val fileUploadInputDao: FileUploadInputDao,
    var selectedAttemptId: Long?,
    val assignmentEnhancementsEnabled: Boolean
) : ListPresenter<SubmissionCommentWrapper, SpeedGraderCommentsView>(SubmissionCommentWrapper::class.java) {

    val mPageId = "${ApiPrefs.domain}-$courseId-$assignmentId-${assignee.id}"
    var selectedFilePaths: List<String>? = null

    private val comments = rawComments.map { CommentWrapper(it) }.toMutableList()
    private var fileUploadJob: Job? = null
    private var pendingSubmissionCommentFetchJob: Job? = null
    private var sendCommentJob: Job? = null
    private var updateCommentJob: Job? = null
    private var saveDraftJob: Job? = null
    private var saveFileCommentJob: Job? = null
    private var retryFileUploadJob: Job? = null
    private var fileUploadFailureJob: Job? = null
    private var removeJob: Job? = null
    private var subscribeJob: Job? = null

    override fun loadData(forceNetwork: Boolean) {

        if (forceNetwork) {
            // Grab new submission comments
            tryWeave {
                // Get updated submission
                val updatedSubmission: Submission = awaitApi {
                    SubmissionManager.getSingleSubmission(courseId, assignmentId, assignee.id, it, true)
                }

                // Add normal comments
                addNormalComments(updatedSubmission.submissionComments.filter { it.attempt == selectedAttemptId || !assignmentEnhancementsEnabled })

                // Add submission history as comments
                addSubmissionHistoryAsComments(updatedSubmission.submissionHistory
                    .mapNotNull { it }
                    .filter { it.attempt == selectedAttemptId || !assignmentEnhancementsEnabled })

                // Add pending comments
                addPendingComments()

                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            } catch {
                Logger.d("${it.stackTrace}")
            }
        } else {
            // Use cached comments

            // Add normal comments
            data.addOrUpdate(comments.filter { it.comment.attempt == selectedAttemptId || !assignmentEnhancementsEnabled })

            // Add submission history as comments
            addSubmissionHistoryAsComments(submissionHistory.filter { it.attempt == selectedAttemptId || !assignmentEnhancementsEnabled })

            // Add pending comments
            addPendingComments()

            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        }

        subscribePendingWorkers()
    }

    private fun addNormalComments(comments: List<SubmissionComment>) = data.addOrUpdate(comments.map { CommentWrapper(it) })

    private fun addSubmissionHistoryAsComments(submissionHistory: List<Submission>) =
        data.addOrUpdate(submissionHistory.map { SubmissionWrapper(it) })

    private fun addPendingComments() {
        // Add pending comments
        pendingSubmissionCommentFetchJob = weave {
            val (drafts, pending) =  pendingSubmissionCommentDao.findByPageId(mPageId)
                .orEmpty()
                .map { it.toApiModel() }
                .map { PendingCommentWrapper(it) }
                .partition { it.pendingComment.status == CommentSendStatus.DRAFT }

            drafts.firstOrNull()?.let { viewCallback?.setDraftText(it.pendingComment.comment) }
            data.addOrUpdate(pending.filter { it.pendingComment.attemptId == selectedAttemptId || !assignmentEnhancementsEnabled })
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        data.clear()
        loadData(forceNetwork)
    }

    override fun getItemId(item: SubmissionCommentWrapper) = item.id

    override fun compare(item1: SubmissionCommentWrapper, item2: SubmissionCommentWrapper): Int =
        item1.date.compareTo(item2.date)

    fun sendComment(text: String) {
        sendCommentJob = weave {
            viewCallback?.setDraftText("")
            val drafts = pendingSubmissionCommentDao.findByPageId(mPageId)
                .orEmpty()
                .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }
            pendingSubmissionCommentDao.deleteAll(drafts.map { it.pendingSubmissionCommentEntity })
            val newComment = PendingSubmissionComment(mPageId, text).apply {
                attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
            }
            pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
            val commentWrapper = PendingCommentWrapper(newComment)
            data.add(commentWrapper)
            viewCallback?.checkIfEmpty()
            viewCallback?.scrollToBottom()
            sendComment(commentWrapper)
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun sendComment(comment: PendingCommentWrapper) {
        sendCommentJob = weave {
            pendingSubmissionCommentDao.findById(comment.id)?.let {
                it.status = CommentSendStatus.SENDING.toString()
                pendingSubmissionCommentDao.update(it)
            }

            comment.pendingComment.status = CommentSendStatus.SENDING
            data.addOrUpdate(comment)

            try {
                val groupMessage = groupMessage
                val userId = when (assignee) {
                    is StudentAssignee -> assignee.student.id
                    is GroupAssignee -> assignee.students.firstOrNull()?.id ?: assignee.id
                }

                val submission = awaitApi<Submission> {
                    SubmissionManager.postSubmissionComment(
                        courseId,
                        assignmentId,
                        userId,
                        comment.pendingComment.comment ?: "",
                        groupMessage,
                        arrayListOf(),
                        it,
                        comment.pendingComment.attemptId.takeIf { assignmentEnhancementsEnabled }
                    )
                }

                submission.submissionComments.lastOrNull()?.let {
                    // Remove pending comment from sharedPrefs
                    pendingSubmissionCommentDao.findById(comment.id)?.let {
                        pendingSubmissionCommentDao.delete(it)
                    }

                    // Remove pending comment from list
                    data.remove(comment)

                    // Add new comment to list
                    val wrappedComment = CommentWrapper(it)
                    comments.add(wrappedComment)
                    data.add(wrappedComment)
                    viewCallback?.checkIfEmpty()
                }
                SubmissionUpdatedEvent(submission).post()
                SubmissionCommentsUpdated().post()
                viewCallback?.scrollToBottom()
            } catch (e: Throwable) {
                // Update status in SharedPrefs
                pendingSubmissionCommentDao.findById(comment.id)?.let {
                    it.status = CommentSendStatus.ERROR.toString()
                    pendingSubmissionCommentDao.update(it)
                }

                // Update status in list
                comment.pendingComment.status = CommentSendStatus.ERROR
                data.addOrUpdate(comment)
            }
        }
    }

    fun updatePendingComments(comments: MutableList<Pair<PendingSubmissionComment, SubmissionComment?>>) {
        updateCommentJob = weave {
            for ((pendingComment, submissionComment) in comments) {
                if (submissionComment != null) {
                    pendingSubmissionCommentDao.findById(pendingComment.id)?.let {
                        pendingSubmissionCommentDao.delete(it)
                    }
                    // Remove pending comment from list
                    data.remove(PendingCommentWrapper(pendingComment))

                    // Add new comment to list
                    val comment = CommentWrapper(submissionComment)
                    this@SpeedGraderCommentsPresenter.comments.add(comment)
                    data.add(comment)
                    viewCallback?.checkIfEmpty()
                } else {
                    // submissionComment is null if there was an error sending the pending intent
                    data.addOrUpdate(PendingCommentWrapper(pendingComment))
                }
            }
        }
    }

    suspend fun createPendingMediaComment(filePath: String): Long {
        val newComment = PendingSubmissionComment(mPageId).apply {
            attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
        }
        newComment.filePath = filePath
        newComment.status = CommentSendStatus.SENDING
        val id = pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        val commentWrapper = PendingCommentWrapper(newComment)

        //add to list
        data.addOrUpdate(commentWrapper)

        viewCallback?.checkIfEmpty()
        viewCallback?.scrollToBottom()

        return id
    }

    override fun onDestroyed() {
        fileUploadJob?.cancel()
        pendingSubmissionCommentFetchJob?.cancel()
        sendCommentJob?.cancel()
        updateCommentJob?.cancel()
        saveDraftJob?.cancel()
        saveFileCommentJob?.cancel()
        retryFileUploadJob?.cancel()
        fileUploadFailureJob?.cancel()
        subscribeJob?.cancel()
        removeJob?.cancel()
        super.onDestroyed()
    }

    fun saveDraft(text: String) {
        saveDraftJob = weave {
            val currentDrafts = pendingSubmissionCommentDao.findByPageId(mPageId)
                .orEmpty()
                .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }

            pendingSubmissionCommentDao.deleteAll(currentDrafts.map { it.pendingSubmissionCommentEntity })

            val newComment = PendingSubmissionComment(mPageId, text).apply {
                attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
            }
            if (text.isNotBlank()) pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))

        }
    }

    private fun createPendingFileComment(workInfo: WorkInfo) {
        saveFileCommentJob = weave {
            var fileUploadInput = fileUploadInputDao.findByWorkerId(workInfo.id.toString())
            if (fileUploadInput == null) {
                fileUploadInput = FileUploadInputEntity(
                    workerId = workInfo.id.toString(),
                    courseId = courseId,
                    assignmentId = assignmentId,
                    userId = assignee.id,
                    filePaths = selectedFilePaths.orEmpty(),
                    action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT,
                    attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
                )
                fileUploadInputDao.insert(fileUploadInput)
            }

            val comment = pendingSubmissionCommentDao.findByPageId(mPageId)?.find {
                it.fileUploadInput?.workerId == workInfo.id.toString()
            }
            val commentWrapper = if (comment == null) {
                val newComment = PendingSubmissionComment(mPageId).apply {
                    this.workerId = workInfo.id
                    this.status = CommentSendStatus.SENDING
                    this.workerInputData = FileUploadWorkerData(
                        selectedFilePaths.orEmpty(),
                        courseId,
                        assignmentId,
                        assignee.id
                    )
                    this.attemptId = selectedAttemptId.takeIf { assignmentEnhancementsEnabled }
                }
                pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
                PendingCommentWrapper(newComment)
            } else {
                PendingCommentWrapper(comment.toApiModel())
            }
            data.addOrUpdate(commentWrapper)
        }

    }

    fun retryFileUpload(pending: PendingSubmissionComment) {
        retryFileUploadJob = weave {
            val pendingEntity = pendingSubmissionCommentDao.findById(pending.id)
            val fileUploadInputEntity = pendingEntity?.workerId?.let {
                fileUploadInputDao.findByWorkerId(it)
            }
            if (fileUploadInputEntity != null) fileUploadInputDao.delete(fileUploadInputEntity)
            if (pendingEntity != null) pendingSubmissionCommentDao.delete(pendingEntity)

            data.remove(PendingCommentWrapper(pending))
            pending.workerInputData?.let { data ->
                selectedFilePaths = data.filePaths
                viewCallback?.restartWorker(data)
            }
        }

    }

    private fun handleFileUploadSuccess(workInfo: WorkInfo) {
        fileUploadJob = weave {
            pendingSubmissionCommentDao.findByWorkerId(workInfo.id.toString())?.let { pending ->
                val pendingSubmissionComment = pending.toApiModel()
                pendingSubmissionCommentDao.delete(pending)
                data.remove(PendingCommentWrapper(pendingSubmissionComment))

                val submissionCommentId = workInfo.outputData.getLong(FileUploadWorker.RESULT_SUBMISSION_COMMENT, 0L)
                submissionCommentDao.findById(submissionCommentId)?.let {
                    val submissionComment = it.toApiModel()
                    dbCleanUp(it)
                    val comment = CommentWrapper(submissionComment)
                    comments.add(comment)
                    data.add(comment)
                    viewCallback?.scrollToBottom()
                    SubmissionCommentsUpdated().post()
                }
            }
        }
    }

    private suspend fun dbCleanUp(submissionComment: SubmissionCommentWithAttachments) {
        submissionComment.author?.let { authorDao.delete(it) }
        submissionComment.mediaComment?.let { mediaCommentDao.delete(it) }
        submissionComment.attachments?.let { attachmentDao.deleteAll(it) }
        submissionCommentDao.delete(submissionComment.submissionComment)
    }

    private fun handleFileUploadFailure(workInfo: WorkInfo) {
        fileUploadFailureJob = weave {
            pendingSubmissionCommentDao.findByWorkerIdWithInputData(workInfo.id.toString())?.apply {
                this.pendingSubmissionCommentEntity.status = CommentSendStatus.ERROR.toString()
            }?.let {
                data.addOrUpdate(PendingCommentWrapper(it.toApiModel()))
                pendingSubmissionCommentDao.update(it.pendingSubmissionCommentEntity)
            }
        }
    }

    fun removeFailedFileUploads() {
        removeJob = weave {
            val failed = pendingSubmissionCommentDao.findByStatus(CommentSendStatus.ERROR.toString())
                .orEmpty()

            val failedInputs = failed.mapNotNull { it.fileUploadInput }
            fileUploadInputDao.deleteAll(failedInputs)

            val failedComments = failed.map { it.pendingSubmissionCommentEntity }
            pendingSubmissionCommentDao.deleteAll(failedComments)

        }
    }

    fun onFileUploadWorkInfoChanged(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.RUNNING -> createPendingFileComment(workInfo)
            WorkInfo.State.SUCCEEDED -> handleFileUploadSuccess(workInfo)
            WorkInfo.State.FAILED -> handleFileUploadFailure(workInfo)
            else -> {}
        }

        viewCallback?.checkIfEmpty()
        viewCallback?.scrollToBottom()
    }

    private fun subscribePendingWorkers() {
        subscribeJob = weave {
            val workerIds = pendingSubmissionCommentDao.findByPageId(mPageId).orEmpty()
                .filter { it.pendingSubmissionCommentEntity.workerId != null && it.pendingSubmissionCommentEntity.workerId != "null" }
                .mapNotNull { UUID.fromString(it.pendingSubmissionCommentEntity.workerId) }
            viewCallback?.subscribePendingWorkers(workerIds)
        }
    }
}
