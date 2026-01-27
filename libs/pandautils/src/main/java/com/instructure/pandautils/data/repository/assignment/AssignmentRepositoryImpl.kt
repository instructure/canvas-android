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

package com.instructure.pandautils.data.repository.assignment

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class AssignmentRepositoryImpl(
    private val userApi: UserAPI.UsersInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface
) : AssignmentRepository {

    override suspend fun getMissingAssignments(forceRefresh: Boolean): DataResult<List<Assignment>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return userApi.getMissingSubmissions(params).depaginate { nextUrl ->
            userApi.getNextPageMissingSubmissions(nextUrl, params)
        }
    }

    override suspend fun getAssignmentGroups(courseId: Long, forceRefresh: Boolean): DataResult<List<AssignmentGroup>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, params).depaginate { nextUrl ->
            assignmentApi.getNextPageAssignmentGroupListWithAssignments(nextUrl, params)
        }
    }
}