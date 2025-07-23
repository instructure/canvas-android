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
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.orDefault
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    private val fileUploadManager: FileUploadManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        try {
            val action = inputData.getString(Const.ACTION) ?: ""

            lateinit var submission: CreateSubmissionEntity

            if (inputData.hasKeyWithValueOfType<Long>(Const.SUBMISSION_ID)) {
                val dbSubmissionId = inputData.getLong(Const.SUBMISSION_ID, 0)
                submission = createSubmissionDao.findSubmissionById(dbSubmissionId)
                    ?: return Result.failure()// Return early if deleted, means it was canceled
            }

            createSubmissionDao.updateProgress(1.0, submission.id)

            return when (SubmissionWorkerAction.valueOf(action)) {
                SubmissionWorkerAction.TEXT_ENTRY -> uploadText(submission)
                SubmissionWorkerAction.FILE_ENTRY -> uploadFileSubmission(submission)
                else -> {
                    Result.failure()
                }
            }
        } catch (e: IllegalArgumentException) {
            return Result.failure()
        }
    }

    private suspend fun uploadText(submission: CreateSubmissionEntity): Result {
        val textToSubmit = try {
            withContext(Dispatchers.IO) {
                URLEncoder.encode(submission.submissionEntry, "UTF-8")
            }
        } catch (e: UnsupportedEncodingException) {
            submission.submissionEntry!!
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

    private suspend fun uploadFileSubmission(submission: CreateSubmissionEntity): Result {
        val (completed, pending) = createFileSubmissionDao
            .findFilesForSubmissionId(submission.id).partition { it.attachmentId != null }
        val uploadedAttachmentIds = uploadFiles(submission, pending)
            ?: return Result.failure() // Cancel submitting if any of the files failed to upload

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
            createSubmissionDao.updateProgress(0.0, submission.id)
            createSubmissionDao.deleteSubmissionById(submission.id)
            Result.success()
        } ?: run {
            createSubmissionDao.updateProgress(0.0, submission.id)
            createSubmissionDao.setSubmissionError(true, submission.id)
            createSubmissionDao.setDraft(submission.id, true)
            Result.failure()
        }
    }

    private suspend fun uploadFiles(
        submission: CreateSubmissionEntity,
        attachments: List<CreateFileSubmissionEntity>
    ): List<Long>? {
        val attachmentIds = ArrayList<Long>(attachments.size)

        attachments.forEachIndexed { index, pendingAttachment ->
            if (pendingAttachment.name == null || pendingAttachment.size == null || pendingAttachment.contentType == null || pendingAttachment.fullPath == null) {
                return null
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

            // Perform upload
            fileUploadManager.uploadFile(config, object : ProgressRequestUpdateListener {
                override fun onProgressUpdated(progressPercent: Float, length: Long): Boolean {
                    runBlocking { createSubmissionDao.findSubmissionById(submission.id) }
                        ?: return false // Stop uploading and don't show error if submission was deleted,

                    return true
                }
            }, RestParams(shouldIgnoreToken = true, disableFileVerifiers = false)).onSuccess { attachment ->
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
            return // Not an error if the submission was deleted, it was canceled
        }

        createSubmissionDao.setSubmissionError(true, submission.id)

        // Set all files that haven't been completed yet to error
        attachments.forEachIndexed { index, file ->
            if (index >= completedCount) {
                createFileSubmissionDao.setFileError(true, errorMessage, file.id)
            }
        }
    }
}