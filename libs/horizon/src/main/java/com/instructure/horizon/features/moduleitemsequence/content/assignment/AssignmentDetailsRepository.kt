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
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import javax.inject.Inject

class AssignmentDetailsRepository @Inject constructor(
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val oAuthInterface: OAuthAPI.OAuthInterface
) {

    suspend fun getAssignment(assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        val params = RestParams(isForceReadFromNetwork = true) // TODO
        return assignmentApi.getAssignmentWithHistory(courseId, assignmentId, params).dataOrThrow
    }

    suspend fun authenticateUrl(url: String): String {
        return oAuthInterface.getAuthenticatedSession(
            url,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrNull?.sessionUrl ?: url
    }
}