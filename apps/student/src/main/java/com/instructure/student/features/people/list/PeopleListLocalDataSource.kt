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

package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.UserFacade

class PeopleListLocalDataSource(private val userFacade: UserFacade): PeopleListDataSource {
    override suspend fun loadFirstPagePeople(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<User>> {
        return DataResult.Success(userFacade.getUsersByCourseId(canvasContext.id))
    }

    override suspend fun loadNextPagePeople(canvasContext: CanvasContext, forceNetwork: Boolean, nextUrl: String): DataResult<List<User>> {
        return DataResult.Success(emptyList())
    }

    override suspend fun loadTeachers(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<User>> {
        return DataResult.Success(userFacade.getUsersByCourseIdAndRole(canvasContext.id, Enrollment.EnrollmentType.Teacher))
    }

    override suspend fun loadTAs(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<List<User>> {
        return DataResult.Success(userFacade.getUsersByCourseIdAndRole(canvasContext.id, Enrollment.EnrollmentType.Ta))
    }
}