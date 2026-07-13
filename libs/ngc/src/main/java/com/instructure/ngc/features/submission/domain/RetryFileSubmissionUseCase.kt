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
package com.instructure.ngc.features.submission.domain

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import javax.inject.Inject

/**
 * Re-enqueues the existing (error-flagged) ONLINE_UPLOAD submission for the current user,
 * preserving all per-file [attachmentId]s so already-uploaded files are not re-uploaded.
 * Returns the existing submission's DB id, or null if no such submission was found.
 */
class RetryFileSubmissionUseCase @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val submissionUploadHelper: SubmissionUploadHelper,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<RetryFileSubmissionUseCase.Params, Long?>() {

    data class Params(val assignmentId: Long)

    override suspend fun execute(params: Params): Long? {
        val userId = apiPrefs.user?.id ?: return null
        val submission = createSubmissionDao.findSubmissionsByAssignmentIdAndType(
            params.assignmentId,
            userId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
        ).firstOrNull() ?: return null
        submissionUploadHelper.retryFileSubmission(submission.id)
        return submission.id
    }
}
