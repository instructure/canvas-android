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
 * Room-only use case: loads the MEDIA_RECORDING draft for an assignment without any network call.
 * Returns [MediaDraftData] with the single clip's file row and the draft's real [Date], or null if
 * no draft exists.
 */
class GetMediaDraftUseCase @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val createFileSubmissionDao: CreateFileSubmissionDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<GetMediaDraftUseCase.Params, MediaDraftData?>() {

    data class Params(val assignmentId: Long)

    override suspend fun execute(params: Params): MediaDraftData? {
        val userId = apiPrefs.user?.id ?: return null
        val draft = createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
            params.assignmentId,
            userId,
            Assignment.SubmissionType.MEDIA_RECORDING.apiString,
        ) ?: return null
        val clip = createFileSubmissionDao.findFilesForSubmissionId(draft.id)
            .map { it.toSubmissionFileData() }
            .firstOrNull() ?: return null
        return MediaDraftData(
            path = clip.fullPath,
            name = clip.name,
            contentType = clip.contentType,
            savedAt = draft.lastActivityDate,
        )
    }
}

data class MediaDraftData(
    val path: String?,
    val name: String,
    val contentType: String?,
    val savedAt: Date?,
)
