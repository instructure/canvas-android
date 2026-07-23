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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.hasKeyWithValueOfType
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.notification.HorizonSubmissionNotificationScheduler
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.notification.HorizonSubmissionNotifier
import com.instructure.horizon.offline.HorizonPendingFileStaging
import com.instructure.horizon.offline.InlineRceMedia
import com.instructure.canvasapi2.utils.errorMessage
import com.instructure.canvasapi2.utils.isTransientFailure
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.orDefault
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import instructure.rceditor.HtmlSanitizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@HiltWorker
class HorizonSubmissionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val createSubmissionDao: CreateSubmissionDao,
    private val apiPrefs: ApiPrefs,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val fileUploadManager: FileUploadManager,
    private val pendingFileStaging: HorizonPendingFileStaging,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val notifier: HorizonSubmissionNotifier,
    private val notificationScheduler: HorizonSubmissionNotificationScheduler
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val action = inputData.getString(Const.ACTION) ?: return Result.failure()

        // Resolve the action up front so an unknown name fails fast instead of being caught below and
        // retried as a (falsely) transient failure.
        val workerAction = SubmissionWorkerAction.entries.find { it.name == action } ?: return Result.failure()

        if (!inputData.hasKeyWithValueOfType<Long>(Const.SUBMISSION_ID)) return Result.failure()
        val dbSubmissionId = inputData.getLong(Const.SUBMISSION_ID, 0)
        val submission = createSubmissionDao.findSubmissionById(dbSubmissionId)
            ?: return Result.failure() // Return early if deleted, means it was canceled

        return try {
            createSubmissionDao.updateProgress(1.0, submission.id)

            when (workerAction) {
                SubmissionWorkerAction.TEXT_ENTRY -> uploadText(submission)
                SubmissionWorkerAction.FILE_ENTRY -> uploadFileSubmission(submission)
                else -> onSubmissionFailed(null, submission)
            }
        } catch (e: CancellationException) {
            // The worker was stopped (e.g. constraint lost or the submission was cancelled). Revert any in-flight
            // state back to QUEUED so the UI shows "Scheduled" (waiting) rather than a stuck "Submitting…", then
            // let WorkManager reschedule it if appropriate.
            withContext(NonCancellable) {
                val current = createSubmissionDao.findSubmissionById(submission.id)
                if (current != null && current.submissionState.isActive && current.submissionState != SubmissionState.QUEUED) {
                    createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.QUEUED)
                    createSubmissionDao.updateProgress(0.0, submission.id)
                }
            }
            throw e
        } catch (e: Exception) {
            // Never leave the row stranded in a non-terminal "Submitting…" state on an unexpected error.
            onSubmissionFailed(null, submission, assumeTransient = true)
        }
    }

    private suspend fun uploadText(submission: CreateSubmissionEntity): Result {
        val entryToSubmit = when (val mediaResult = uploadInlineMedia(submission)) {
            is InlineMediaResult.ShortCircuit -> return mediaResult.result
            is InlineMediaResult.Ready -> mediaResult.entry
        }

        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.SUBMITTING)

        val sanitizedEntry = entryToSubmit?.let { HtmlSanitizer.sanitize(it) } ?: entryToSubmit
        val textToSubmit = try {
            withContext(Dispatchers.IO) {
                URLEncoder.encode(sanitizedEntry, "UTF-8")
            }
        } catch (e: UnsupportedEncodingException) {
            sanitizedEntry!!
        }
        val result = submissionApi.postTextSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            textToSubmit,
            tokenSafeParams(submission)
        )

        return handleSubmissionResult(result, submission)
    }

    private suspend fun uploadInlineMedia(submission: CreateSubmissionEntity): InlineMediaResult {
        var entry = submission.submissionEntry ?: return InlineMediaResult.Ready(null)
        val localSrcs = InlineRceMedia.findLocalImageSrcs(entry)
        if (localSrcs.isEmpty()) return InlineMediaResult.Ready(entry)

        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.UPLOADING_FILES)

        for (localSrc in localSrcs) {
            val file = File(localSrc.removePrefix("file://"))
            if (!file.exists()) {
                return InlineMediaResult.ShortCircuit(onSubmissionFailed(null, submission))
            }

            val fso = FileSubmitObject(file.name, file.length(), FileUtils.getMimeType(file.path), file.absolutePath)

            val uploadResult = fileUploadManager.uploadFile(
                FileUploadConfig.forUser(fso),
                fileUploadRestParams = fileUploadParams()
            )
            val attachment = uploadResult.dataOrNull
                ?: return InlineMediaResult.ShortCircuit(onSubmissionFailed(uploadResult, submission))

            val urlResult = fileFolderApi.getUserFile(attachment.id, fileUploadParams())
            val url = urlResult.dataOrNull?.url ?: attachment.url
            if (url.isNullOrEmpty()) {
                return InlineMediaResult.ShortCircuit(onSubmissionFailed(urlResult, submission))
            }

            entry = InlineRceMedia.rewriteLocalImageSrc(entry, localSrc, url)
            createSubmissionDao.updateSubmissionEntry(submission.id, entry)
            file.delete()
        }

        pendingFileStaging.deleteInline(inlineStagingKey(submission))
        return InlineMediaResult.Ready(entry)
    }

    private fun inlineStagingKey(submission: CreateSubmissionEntity) =
        "${submission.userId}_${submission.assignmentId}"

    private sealed class InlineMediaResult {
        data class Ready(val entry: String?) : InlineMediaResult()
        data class ShortCircuit(val result: Result) : InlineMediaResult()
    }

    private suspend fun uploadFileSubmission(submission: CreateSubmissionEntity): Result {
        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.UPLOADING_FILES)

        val (completed, pending) = createFileSubmissionDao
            .findFilesForSubmissionId(submission.id).partition { it.attachmentId != null }

        val uploadResult = uploadFiles(submission, pending)
        if (uploadResult is FileUploadResult.Failed) {
            // A file failed to upload; classify and either retry or fail permanently.
            return onSubmissionFailed(uploadResult.result, submission)
        }

        createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.SUBMITTING)

        val uploadedAttachmentIds = (uploadResult as FileUploadResult.Success).attachmentIds
        val attachmentIds = completed.mapNotNull { it.attachmentId } + uploadedAttachmentIds
        val result = submissionApi.postSubmissionAttachments(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
            attachmentIds,
            tokenSafeParams(submission)
        )

        return handleSubmissionResult(result, submission)
    }

    private suspend fun handleSubmissionResult(
        result: DataResult<Submission>,
        submission: CreateSubmissionEntity
    ): Result {
        return result.dataOrNull?.let {
            createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.COMPLETED)
            createSubmissionDao.updateProgress(100.0, submission.id)
            CanvasRestAdapter.clearCacheUrls("courses/${submission.canvasContext.id}/assignments/${submission.assignmentId}")
            delay(1000)
            deleteStagedFiles(submission.id)
            if (submission.notifyOnResult) {
                notificationScheduler.cancelDeadlineReminders(submission.id)
                notifier.postSuccess(
                    submissionId = submission.id,
                    assignmentName = submission.assignmentName,
                    assignmentId = submission.assignmentId,
                    courseId = submission.canvasContext.id
                )
            }
            createSubmissionDao.deleteSubmissionById(submission.id)
            Result.success()
        } ?: onSubmissionFailed(result, submission)
    }

    private suspend fun onSubmissionFailed(
        result: DataResult<*>?,
        submission: CreateSubmissionEntity,
        assumeTransient: Boolean = false
    ): Result {
        createSubmissionDao.updateProgress(0.0, submission.id)

        return if ((assumeTransient || result.isTransientFailure()) && runAttemptCount < MAX_ATTEMPTS) {
            createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.RETRYING)
            createSubmissionDao.incrementRetryCount(submission.id, result.errorMessage())
            Result.retry()
        } else {
            createSubmissionDao.updateSubmissionState(submission.id, SubmissionState.FAILED)
            createSubmissionDao.setSubmissionError(true, submission.id)
            createSubmissionDao.setDraft(submission.id, true)
            if (submission.notifyOnResult) {
                notificationScheduler.cancelDeadlineReminders(submission.id)
                notifier.postFailure(
                    submissionId = submission.id,
                    assignmentName = submission.assignmentName,
                    assignmentId = submission.assignmentId,
                    courseId = submission.canvasContext.id,
                    reason = result.errorMessage()
                )
            }
            Result.failure()
        }
    }

    private suspend fun uploadFiles(
        submission: CreateSubmissionEntity,
        attachments: List<CreateFileSubmissionEntity>
    ): FileUploadResult {
        val attachmentIds = ArrayList<Long>(attachments.size)

        attachments.forEachIndexed { index, pendingAttachment ->
            if (pendingAttachment.name == null || pendingAttachment.size == null || pendingAttachment.contentType == null || pendingAttachment.fullPath == null) {
                handleFileError(submission, index, attachments, null)
                return FileUploadResult.Failed(null)
            }

            // Upload config setup
            val fso = FileSubmitObject(
                pendingAttachment.name.orEmpty(),
                pendingAttachment.size.orDefault(),
                pendingAttachment.contentType.orEmpty(),
                pendingAttachment.fullPath.orEmpty()
            )
            val config = FileUploadConfig.forSubmission(
                fso,
                submission.canvasContext.id,
                submission.assignmentId
            )

            // Perform upload with the token-safe contract so a 401 is refreshed transparently.
            val uploadResult = fileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    runBlocking { createSubmissionDao.findSubmissionById(submission.id) }
                        ?: return false // Stop uploading and don't show error if submission was deleted,

                    return true
                }
            }, fileUploadParams())

            uploadResult.onSuccess { attachment ->
                createFileSubmissionDao
                    .setFileAttachmentIdAndError(
                        attachment.id,
                        false,
                        null,
                        pendingAttachment.id
                    )
                attachmentIds.add(attachment.id)
            }.onFailure {
                handleFileError(submission, index, attachments, it?.message)
            }

            if (uploadResult is DataResult.Fail) {
                return FileUploadResult.Failed(uploadResult)
            }
        }
        return FileUploadResult.Success(attachmentIds)
    }

    private suspend fun handleFileError(
        submission: CreateSubmissionEntity,
        completedCount: Int,
        attachments: List<CreateFileSubmissionEntity>,
        errorMessage: String?
    ) {
        if (createSubmissionDao.findSubmissionById(submission.id) == null) {
            return // Not an error if the submission was deleted, it was canceled
        }

        // Set all files that haven't been completed yet to error. The submission-level error/retry
        // decision is made in onSubmissionFailed based on whether the failure is transient.
        attachments.forEachIndexed { index, file ->
            if (index >= completedCount) {
                createFileSubmissionDao.setFileError(true, errorMessage, file.id)
            }
        }
    }

    private suspend fun deleteStagedFiles(submissionId: Long) {
        createFileSubmissionDao.findFilesForSubmissionId(submissionId).forEach { file ->
            file.fullPath?.let { FileUploadUtils.deleteTempFile(it) }
        }
    }

    private fun tokenSafeParams(submission: CreateSubmissionEntity) = RestParams(
        canvasContext = submission.canvasContext,
        domain = apiPrefs.overrideDomains[submission.canvasContext.id],
        shouldRefreshToken = true,
        shouldLoginOnTokenError = false
    )

    // The upload context path is fully specified by FileUploadConfig, so we must NOT set a
    // canvasContext here or CanvasRestAdapter would prepend "courses/" and corrupt the URL.
    private fun fileUploadParams() = RestParams(
        shouldRefreshToken = true,
        shouldLoginOnTokenError = false,
        disableFileVerifiers = false
    )

    private sealed class FileUploadResult {
        data class Success(val attachmentIds: List<Long>) : FileUploadResult()
        data class Failed(val result: DataResult<*>?) : FileUploadResult()
    }

    companion object {
        private const val MAX_ATTEMPTS = 5
    }
}
