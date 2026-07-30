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
import java.util.Date
import javax.inject.Inject

/**
 * Room-only use case: loads the ONLINE_UPLOAD draft for an assignment without any network call.
 * Returns [FileDraftData] with the file rows and the draft's real [Date], or null if no draft exists.
 */
class GetFileDraftUseCase @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<GetFileDraftUseCase.Params, FileDraftData>() {

    data class Params(val assignmentId: Long)

    override suspend fun execute(params: Params): FileDraftData {
        val userId = apiPrefs.user?.id ?: return FileDraftData(emptyList(), null)
        val draft = createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
            params.assignmentId,
            userId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
        ) ?: return FileDraftData(emptyList(), null)
        val files = createFileSubmissionDao.findFilesForSubmissionId(draft.id)
            .map { it.toSubmissionFileData() }
        return FileDraftData(files = files, savedAt = draft.lastActivityDate)
    }
}

data class FileDraftData(
    val files: List<SubmissionFileData>,
    val savedAt: Date?,
)
