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
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import javax.inject.Inject

/**
 * Returns the in-flight (non-draft, non-error) ONLINE_UPLOAD submission for the current user,
 * or null if none exists. Used by the VM to re-attach after process death.
 */
class ResumeInFlightSubmissionUseCase @Inject constructor(
    private val createSubmissionDao: CreateSubmissionDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<ResumeInFlightSubmissionUseCase.Params, CreateSubmissionEntity?>() {

    data class Params(val assignmentId: Long)

    override suspend fun execute(params: Params): CreateSubmissionEntity? {
        val userId = apiPrefs.user?.id ?: return null
        return createSubmissionDao.findSubmissionsByAssignmentIdAndType(
            params.assignmentId,
            userId,
            Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
        ).firstOrNull { !it.isDraft && !it.errorFlag }
    }
}
