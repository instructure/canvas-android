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

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.date.DateTimeProvider
import java.util.concurrent.TimeUnit

class HorizonSubmissionNotificationScheduler(
    private val workManager: WorkManager,
    private val dateTimeProvider: DateTimeProvider,
    private val createSubmissionDao: CreateSubmissionDao
) {

    suspend fun onOfflineSubmissionQueued(submissionId: Long, assignment: Assignment?) {
        createSubmissionDao.setNotifyOnResult(submissionId, true)

        val dueMillis = assignment?.dueDate?.time ?: return
        val now = dateTimeProvider.getCalendar().timeInMillis
        if (dueMillis <= now) return

        SubmissionDeadlineThreshold.values().forEach { threshold ->
            val delay = dueMillis - now - TimeUnit.MINUTES.toMillis(threshold.minutesBefore)
            if (delay <= 0L) return@forEach

            val request = OneTimeWorkRequest.Builder(SubmissionDeadlineReminderWorker::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putLong(SubmissionDeadlineReminderWorker.KEY_SUBMISSION_ID, submissionId)
                        .putString(SubmissionDeadlineReminderWorker.KEY_THRESHOLD, threshold.name)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                deadlineWorkName(submissionId, threshold),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    fun cancelDeadlineReminders(submissionId: Long) {
        SubmissionDeadlineThreshold.values().forEach { threshold ->
            workManager.cancelUniqueWork(deadlineWorkName(submissionId, threshold))
        }
    }

    companion object {
        fun deadlineWorkName(submissionId: Long, threshold: SubmissionDeadlineThreshold) =
            "horizon-submission-deadline-$submissionId-${threshold.name}"
    }
}
