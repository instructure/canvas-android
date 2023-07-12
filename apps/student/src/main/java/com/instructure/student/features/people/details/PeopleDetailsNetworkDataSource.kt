/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.people.details

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.weave.awaitOrThrow
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup

class PeopleDetailsNetworkDataSource(private val userApi: UserAPI.UsersInterface): PeopleDetailsDataSource {
    override suspend fun loadUser(canvasContext: CanvasContext, userId: Long, forceNetwork: Boolean): User {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return userApi.getUserForContextId(canvasContext.apiContext(), canvasContext.id, userId, restParams).dataOrThrow
    }

    override suspend fun loadMessagePermission(canvasContext: CanvasContext, user: User?): Boolean {
        val id = canvasContext.id
        return when {
            canvasContext.isGroup -> GroupManager.getPermissionsAsync(id).awaitOrThrow().send_messages
            canvasContext.isCourse -> {
                val isTeacher = user?.enrollments?.any { it.courseId == id && (it.isTA || it.isTeacher) } ?: false
                isTeacher || CourseManager.getPermissionsAsync(id).awaitOrThrow().send_messages
            }
            else -> false
        }
    }
}