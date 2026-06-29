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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.Const
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * A clean, shared submission upload worker for use by NGC and other modern features.
 *
 * Design goals:
 * - No EventBus, no notifications, no Mobius callbacks.
 * - Results are surfaced purely through Room state (CreateSubmissionDao).
 * - Text entry is implemented now; FILE/URL/MEDIA are clear extension points below.
 *
 * This class is intentionally named [SubmissionUploadWorker] to avoid clashing with
 * the legacy student-app `SubmissionWorker`.
 */
@HiltWorker
class SubmissionUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val createSubmissionDao: CreateSubmissionDao,
    private val apiPrefs: ApiPrefs,
    private val submissionApi: SubmissionAPI.SubmissionInterface
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
                // TODO: add FILE_ENTRY, URL_ENTRY, MEDIA_ENTRY, STUDIO_ENTRY, STUDENT_ANNOTATION
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
