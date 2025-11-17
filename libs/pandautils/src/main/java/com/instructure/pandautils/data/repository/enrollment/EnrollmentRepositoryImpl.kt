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
 *
 */
package com.instructure.pandautils.data.repository.enrollment

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class EnrollmentRepositoryImpl @Inject constructor(
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface
) : EnrollmentRepository {

    override suspend fun getSelfEnrollments(
        types: List<String>?,
        states: List<String>?,
        forceRefresh: Boolean
    ): DataResult<List<Enrollment>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return enrollmentApi.getFirstPageSelfEnrollments(types, states, params)
    }

    override suspend fun handleInvitation(
        courseId: Long,
        enrollmentId: Long,
        accept: Boolean
    ): DataResult<Unit> {
        val params = RestParams()
        val action = if (accept) "accept" else "reject"
        return enrollmentApi.handleInvite(courseId, enrollmentId, action, params)
    }
}