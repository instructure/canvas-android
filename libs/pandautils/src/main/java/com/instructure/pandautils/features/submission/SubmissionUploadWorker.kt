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
package com.instructure.pandautils.features.submission

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.hasKeyWithValueOfType
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
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.NotoriousUploader
import com.instructure.pandautils.utils.orDefault
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * A clean, shared submission upload worker for use by NGC and other modern features.
 *
 * Design goals:
 * - No EventBus, no notifications, no Mobius callbacks.
 * - Results are surfaced purely through Room state (CreateSubmissionDao).
 * - Text, file, URL, and student-annotation entry are all implemented; MEDIA/STUDIO
 *   are clear extension points below.
 *
 * This class is intentionally named [SubmissionUploadWorker] to avoid clashing with
 * the legacy student-app `SubmissionWorker`.
 */
@HiltWorker
class SubmissionUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val apiPrefs: ApiPrefs,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val fileUploadManager: FileUploadManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        try {
            val action = inputData.getString(Const.ACTION) ?: return Result.failure()

            val submission: CreateSubmissionEntity = if (inputData.hasKeyWithValueOfType<Long>(Const.SUBMISSION_ID)) {
                val dbSubmissionId = inputData.getLong(Const.SUBMISSION_ID, 0)
                createSubmissionDao.findSubmissionById(dbSubmissionId)
                    ?: return Result.failure() // Submission was deleted (cancelled)
            } else {
                return Result.failure()
            }

            createSubmissionDao.updateProgress(1.0, submission.id)

            return when (SubmissionWorkerAction.valueOf(action)) {
                SubmissionWorkerAction.TEXT_ENTRY -> uploadText(submission)
                SubmissionWorkerAction.FILE_ENTRY -> uploadFileSubmission(submission)
                SubmissionWorkerAction.URL_ENTRY -> uploadUrl(submission)
                SubmissionWorkerAction.MEDIA_ENTRY -> uploadMedia(submission)
                SubmissionWorkerAction.STUDENT_ANNOTATION -> uploadStudentAnnotation(submission)
                // TODO: add STUDIO_ENTRY
                //  when those submission types are supported in the NGC experience.
                else -> Result.failure()
            }
        } catch (e: IllegalArgumentException) {
            return Result.failure()
        }
    }

    internal suspend fun uploadText(submission: CreateSubmissionEntity): Result {
        val textToSubmit = try {
            withContext(Dispatchers.IO) {
                URLEncoder.encode(submission.submissionEntry, "UTF-8")
            }
        } catch (e: UnsupportedEncodingException) {
            submission.submissionEntry.orEmpty()
        }

        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = apiPrefs.overrideDomains[submission.canvasContext.id],
            shouldLoginOnTokenError = false
        )

        val result = submissionApi.postTextSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            textToSubmit,
            params
        )

        return handleSubmissionResult(result, submission)
    }

    internal suspend fun uploadUrl(submission: CreateSubmissionEntity): Result {
        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = apiPrefs.overrideDomains[submission.canvasContext.id],
            shouldLoginOnTokenError = false
        )

        val result = submissionApi.postUrlSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.ONLINE_URL.apiString,
            submission.submissionEntry.orEmpty(),
            params
        )

        return handleSubmissionResult(result, submission)
    }

    internal suspend fun uploadMedia(submission: CreateSubmissionEntity): Result {
        val mediaFile = createFileSubmissionDao.findFileForSubmissionId(submission.id)
            ?: return Result.failure() // The file was deleted, so there's nothing to upload

        var lastReportedPercent = -1

        val uploadResult = NotoriousUploader.performUpload(
            mediaFile.fullPath ?: return failFileSubmission(submission),
            object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    runBlocking { createSubmissionDao.findSubmissionById(submission.id) }
                        ?: return false // Stop uploading; submission was deleted (cancelled)

                    val overallPercent = (progressPercent * 100f).toInt().coerceIn(1, 99)
                    if (overallPercent > lastReportedPercent) {
                        lastReportedPercent = overallPercent
                        runBlocking { createSubmissionDao.updateProgress(overallPercent.toDouble(), submission.id) }
                    }
                    return true
                }
            }
        )

        return uploadResult.dataOrNull?.let { result ->
            val mediaId = result.id ?: return failFileSubmission(submission)

            val params = RestParams(
                canvasContext = submission.canvasContext,
                domain = apiPrefs.overrideDomains[submission.canvasContext.id],
                shouldLoginOnTokenError = false
            )

            val submissionResult = submissionApi.postMediaSubmission(
                submission.canvasContext.id,
                submission.assignmentId,
                Assignment.SubmissionType.MEDIA_RECORDING.apiString,
                mediaId,
                FileUtils.mediaTypeFromNotoriousCode(result.mediaType),
                params
            )

            submissionResult.dataOrNull?.let {
                createSubmissionDao.updateProgress(100.0, submission.id)
                delay(1000)
                // Clean up the temp copy from internal cache before deleting the DB rows
                mediaFile.fullPath?.let { path -> FileUploadUtils.deleteTempFile(path) }
                createFileSubmissionDao.deleteFilesForSubmissionId(submission.id)
                createSubmissionDao.deleteSubmissionById(submission.id)
                Result.success()
            } ?: failFileSubmission(submission)
        } ?: failFileSubmission(submission)
    }

    internal suspend fun uploadFileSubmission(submission: CreateSubmissionEntity): Result {
        val (completed, pending) = createFileSubmissionDao
            .findFilesForSubmissionId(submission.id)
            .partition { it.attachmentId != null }

        val uploadedAttachmentIds = uploadFiles(submission, pending)
            ?: return failFileSubmission(submission) // Individual file upload failed

        val attachmentIds = completed.mapNotNull { it.attachmentId } + uploadedAttachmentIds
        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = apiPrefs.overrideDomains[submission.canvasContext.id],
            shouldLoginOnTokenError = false
        )
        val result = submissionApi.postSubmissionAttachments(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
            attachmentIds,
            params
        )

        return result.dataOrNull?.let {
            createSubmissionDao.updateProgress(100.0, submission.id)
            delay(1000)
            // Clean up temp copies from internal cache before deleting the DB row
            val allFiles = createFileSubmissionDao.findFilesForSubmissionId(submission.id)
            allFiles.forEach { file -> file.fullPath?.let { path -> FileUploadUtils.deleteTempFile(path) } }
            createSubmissionDao.deleteSubmissionById(submission.id)
            Result.success()
        } ?: run {
            failFileSubmission(submission)
        }
    }

    private suspend fun uploadFiles(
        submission: CreateSubmissionEntity,
        attachments: List<CreateFileSubmissionEntity>
    ): List<Long>? {
        val attachmentIds = ArrayList<Long>(attachments.size)
        val totalFileCount = attachments.size
        var lastReportedPercent = -1

        attachments.forEachIndexed { index, pendingAttachment ->
            if (pendingAttachment.name == null || pendingAttachment.size == null ||
                pendingAttachment.contentType == null || pendingAttachment.fullPath == null
            ) {
                handleFileError(submission, index, attachments, null)
                return null
            }

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

            fileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    // Write aggregated whole-submission progress, throttled to integer-percent changes.
                    val overall = ((index + progressPercent / 100f) / totalFileCount) * 100f
                    val overallInt = overall.toInt().coerceIn(1, 99)
                    if (overallInt > lastReportedPercent) {
                        // Check cancellation and write progress together, once per percent change.
                        runBlocking { createSubmissionDao.findSubmissionById(submission.id) }
                            ?: return false
                        lastReportedPercent = overallInt
                        runBlocking { createSubmissionDao.updateProgress(overallInt.toDouble(), submission.id) }
                    }
                    return true
                }
            }, RestParams(shouldIgnoreToken = true, disableFileVerifiers = false))
                .onSuccess { attachment ->
                    createFileSubmissionDao.setFileAttachmentIdAndError(
                        attachment.id,
                        false,
                        null,
                        pendingAttachment.id
                    )
                    attachmentIds.add(attachment.id)
                }
                .onFailure {
                    handleFileError(submission, index, attachments, it?.message)
                    return null
                }
        }
        return attachmentIds
    }

    private suspend fun handleFileError(
        submission: CreateSubmissionEntity,
        completedCount: Int,
        attachments: List<CreateFileSubmissionEntity>,
        errorMessage: String?
    ) {
        if (createSubmissionDao.findSubmissionById(submission.id) == null) {
            return // Not an error if the submission was deleted (cancelled)
        }

        createSubmissionDao.setSubmissionError(true, submission.id)

        attachments.forEachIndexed { index, file ->
            if (index >= completedCount) {
                createFileSubmissionDao.setFileError(true, errorMessage, file.id)
            }
        }
    }

    private suspend fun failFileSubmission(submission: CreateSubmissionEntity): Result {
        createSubmissionDao.updateProgress(0.0, submission.id)
        createSubmissionDao.setSubmissionError(true, submission.id)
        // Restore the draft flag so loadDraft(ONLINE_UPLOAD) can find this submission
        // after a failed upload, even across process death.
        createSubmissionDao.setDraft(submission.id, true)
        return Result.failure()
    }

    internal suspend fun uploadStudentAnnotation(submission: CreateSubmissionEntity): Result {
        val annotatableAttachmentId = submission.annotatableAttachmentId
            ?: return failStudentAnnotation(submission)

        val params = RestParams(
            canvasContext = submission.canvasContext,
            domain = apiPrefs.overrideDomains[submission.canvasContext.id],
            shouldLoginOnTokenError = false
        )

        val result = submissionApi.postStudentAnnotationSubmission(
            submission.canvasContext.id,
            submission.assignmentId,
            Assignment.SubmissionType.STUDENT_ANNOTATION.apiString,
            annotatableAttachmentId,
            params
        )

        return handleSubmissionResult(result, submission)
    }

    private suspend fun failStudentAnnotation(submission: CreateSubmissionEntity): Result {
        createSubmissionDao.updateProgress(0.0, submission.id)
        createSubmissionDao.setSubmissionError(true, submission.id)
        return Result.failure()
    }

    private suspend fun handleSubmissionResult(
        result: DataResult<Submission>,
        submission: CreateSubmissionEntity
    ): Result {
        return result.dataOrNull?.let {
            createSubmissionDao.updateProgress(100.0, submission.id)
            delay(1000)
            createSubmissionDao.deleteSubmissionById(submission.id)
            Result.success()
        } ?: run {
            createSubmissionDao.updateProgress(0.0, submission.id)
            delay(1000)
            createSubmissionDao.setSubmissionError(true, submission.id)
            Result.failure()
        }
    }
}
