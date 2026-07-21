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

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.submission.BaseSubmissionHelper
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.utils.Const
import kotlinx.coroutines.runBlocking

class HorizonSubmissionHelper(
    private val studentDb: StudentDb,
    apiPrefs: ApiPrefs,
    private val workManager: WorkManager
) : BaseSubmissionHelper(studentDb, apiPrefs) {

    fun retrySubmission(submission: CreateSubmissionEntity) {
        val action = submission.workerAction() ?: return
        runBlocking {
            studentDb.submissionDao().setSubmissionError(false, submission.id)
            studentDb.submissionDao().setDraft(submission.id, false)
            studentDb.submissionDao().updateSubmissionState(submission.id, SubmissionState.QUEUED)
        }
        startSubmissionWorker(action, submissionId = submission.id)
    }

    /**
     * Force a scheduled submission to upload immediately (used when the learner is back online and the worker
     * hasn't started yet). Re-enqueues the same submission as an expedited job.
     */
    fun submitScheduledSubmission(submission: CreateSubmissionEntity) {
        val action = submission.workerAction() ?: return
        runBlocking {
            studentDb.submissionDao().setSubmissionError(false, submission.id)
            studentDb.submissionDao().setDraft(submission.id, false)
            studentDb.submissionDao().updateSubmissionState(submission.id, SubmissionState.QUEUED)
        }
        enqueueSubmissionWork(action, submissionId = submission.id, expedited = true)
    }

    /**
     * Cancel a scheduled submission and keep its content as an editable draft.
     */
    fun cancelSubmission(submission: CreateSubmissionEntity) {
        workManager.cancelUniqueWork(workName(submission.id))
        runBlocking {
            studentDb.submissionDao().setSubmissionError(false, submission.id)
            studentDb.submissionDao().updateSubmissionState(submission.id, SubmissionState.QUEUED)
            studentDb.submissionDao().setDraft(submission.id, true)
        }
    }

    override fun startSubmissionWorker(action: SubmissionWorkerAction, submissionId: Long?, commentId: Long?) {
        enqueueSubmissionWork(action, submissionId, commentId, expedited = false)
    }

    private fun enqueueSubmissionWork(
        action: SubmissionWorkerAction,
        submissionId: Long? = null,
        commentId: Long? = null,
        expedited: Boolean = false
    ) {
        val data = Data.Builder()
        data.putString(Const.ACTION, action.name)

        submissionId?.let {
            data.putLong(Const.SUBMISSION_ID, it)
        }
        commentId?.let {
            data.putLong(Const.ID, it)
        }
        val builder = OneTimeWorkRequest.Builder(HorizonSubmissionWorker::class.java)
            .setInputData(data.build())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        if (expedited) {
            builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        val submissionWork = builder.build()

        // Uniquely name work per submission so it can be cancelled or replaced (force submit) individually.
        if (submissionId != null) {
            workManager.enqueueUniqueWork(workName(submissionId), ExistingWorkPolicy.REPLACE, submissionWork)
        } else {
            workManager.enqueue(submissionWork)
        }
    }

    private fun CreateSubmissionEntity.workerAction(): SubmissionWorkerAction? = when (submissionType) {
        Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString -> SubmissionWorkerAction.TEXT_ENTRY
        Assignment.SubmissionType.ONLINE_UPLOAD.apiString -> SubmissionWorkerAction.FILE_ENTRY
        else -> null
    }

    companion object {
        fun workName(submissionId: Long) = "horizon-submission-$submissionId"
    }
}