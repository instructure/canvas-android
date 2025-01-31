/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.features.calendartodo.details

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.User


class ToDoRepository(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface,
    private val userApi: UserAPI.UsersInterface,
) {

    suspend fun deletePlannerNote(noteId: Long) {
        plannerApi.deletePlannerNote(
            noteId,
            RestParams()
        ).dataOrThrow
    }

    suspend fun getPlannerNote(noteId: Long): Plannable {
        return plannerApi.getPlannerNote(
            noteId,
            RestParams()
        ).dataOrThrow
    }

    suspend fun getCourse(courseId: Long): Course {
        return courseApi.getCourse(
            courseId,
            RestParams()
        ).dataOrThrow
    }

    suspend fun getGroup(groupId: Long): Group {
        return groupApi.getDetailedGroup(
            groupId,
            RestParams()
        ).dataOrThrow
    }

    suspend fun getUser(userId: Long): User {
        return userApi.getUser(
            userId,
            RestParams()
        ).dataOrThrow
    }
}
