/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.mobius.common.ui

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.submission.BaseSubmissionHelper
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.utils.Const
import java.util.concurrent.TimeUnit

class SubmissionHelper(
    studentDb: StudentDb,
    apiPrefs: ApiPrefs,
    private val workManager: WorkManager
) : BaseSubmissionHelper(studentDb, apiPrefs) {

    override fun startSubmissionWorker(action: SubmissionWorkerAction, submissionId: Long?, commentId: Long?) {
        val data = Data.Builder()
        data.putString(Const.ACTION, action.name)

        submissionId?.let {
            data.putLong(Const.SUBMISSION_ID, it)
        }
        commentId?.let {
            data.putLong(Const.ID, it)
        }
        val submissionWork = OneTimeWorkRequest.Builder(SubmissionWorker::class.java)
            .setInputData(data.build())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag("SubmissionWorker")
            .build()
        workManager.enqueue(submissionWork)
    }

}