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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SubmissionDeadlineReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val createSubmissionDao: CreateSubmissionDao,
    private val notifier: HorizonSubmissionNotifier
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val submissionId = inputData.getLong(KEY_SUBMISSION_ID, -1L)
        if (submissionId == -1L) return Result.failure()

        val thresholdName = inputData.getString(KEY_THRESHOLD) ?: return Result.failure()
        val threshold = runCatching { SubmissionDeadlineThreshold.valueOf(thresholdName) }.getOrNull()
            ?: return Result.failure()

        // Row deleted (completed) or no longer active (e.g. FAILED) -> nothing to warn about.
        val submission = createSubmissionDao.findSubmissionById(submissionId)
        if (submission == null || !submission.submissionState.isActive) {
            return Result.success()
        }

        notifier.postDeadlineReminder(
            submissionId = submission.id,
            assignmentName = submission.assignmentName,
            assignmentId = submission.assignmentId,
            courseId = submission.canvasContext.id,
            threshold = threshold
        )
        return Result.success()
    }

    companion object {
        const val KEY_SUBMISSION_ID = "submissionId"
        const val KEY_THRESHOLD = "threshold"
    }
}
