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
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import javax.inject.Inject

/**
 * Returns per-file [SubmissionFileData] entries for the most recent ONLINE_UPLOAD submission
 * belonging to the current user, preserving each row's [errorFlag] and [error] message for the
 * ViewModel to map to the appropriate [SubmissionFileState].
 *
 * Used by [com.instructure.ngc.features.submission.NGCSubmissionViewModel] on terminal error so
 * only the specific files that actually failed are shown as Error while already-uploaded files
 * (those with a non-null attachmentId) remain Ready.
 */
class GetSubmissionFileStatesUseCase @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val apiPrefs: ApiPrefs
) : BaseUseCase<GetSubmissionFileStatesUseCase.Params, List<SubmissionFileData>>() {

    data class Params(val assignmentId: Long)

    override suspend fun execute(params: Params): List<SubmissionFileData> {
        val userId = apiPrefs.user?.id ?: return emptyList()
        val submission = createSubmissionDao.findSubmissionsByAssignmentIdAndType(
            params.assignmentId,
            userId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString
        ).firstOrNull() ?: return emptyList()
        return createFileSubmissionDao.findFilesForSubmissionId(submission.id).map { it.toSubmissionFileData() }
    }
}
