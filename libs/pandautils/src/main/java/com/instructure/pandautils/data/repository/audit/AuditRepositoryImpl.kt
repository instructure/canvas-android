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

package com.instructure.pandautils.data.repository.audit

import com.instructure.canvasapi2.apis.AuditAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.GradeChange
import com.instructure.canvasapi2.utils.DataResult

class AuditRepositoryImpl(
    private val auditApi: AuditAPI
) : AuditRepository {

    override suspend fun getGradeChanges(
        studentId: Long,
        startTime: String?,
        endTime: String?,
        forceRefresh: Boolean
    ): DataResult<List<GradeChange>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return auditApi.getGradeChangesForStudent(studentId, startTime, endTime, params)
    }
}