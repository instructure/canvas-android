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
 * Room-only use case: returns the current ONLINE_UPLOAD draft's file rows for an assignment
 * as domain [SubmissionFileData] objects.
 *
 * Looks the draft up by assignmentId + type (the same reliable path the initial load uses) rather
 * than by a saved-submission id: saveDraftWithFiles inserts the file rows after the submission, so
 * its returned "last insert" id points at a file row — not the submission — and can't re-read them.
 */
class GetDraftFilesUseCase @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<GetDraftFilesUseCase.Params, List<SubmissionFileData>>() {

    data class Params(val assignmentId: Long)

    override suspend fun execute(params: Params): List<SubmissionFileData> {
        val userId = apiPrefs.user?.id ?: return emptyList()
        val submission = createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
            params.assignmentId,
            userId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
        ) ?: return emptyList()
        return createFileSubmissionDao.findFilesForSubmissionId(submission.id).map { it.toSubmissionFileData() }
    }
}
