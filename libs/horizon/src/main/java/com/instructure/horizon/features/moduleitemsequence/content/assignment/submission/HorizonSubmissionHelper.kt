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

import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.submission.BaseSubmissionHelper
import com.instructure.pandautils.features.submission.SubmissionWorkerAction
import com.instructure.pandautils.room.studentdb.StudentDb

class HorizonSubmissionHelper(
    studentDb: StudentDb,
    apiPrefs: ApiPrefs,
    private val workManager: WorkManager
) : BaseSubmissionHelper(studentDb, apiPrefs) {

    override fun startSubmissionWorker(action: SubmissionWorkerAction, submissionId: Long?, commentId: Long?) {

    }
}