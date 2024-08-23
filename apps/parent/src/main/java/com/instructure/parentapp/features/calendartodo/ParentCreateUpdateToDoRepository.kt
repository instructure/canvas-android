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
package com.instructure.parentapp.features.calendartodo

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.util.ParentPrefs

class ParentCreateUpdateToDoRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    private val parentPrefs: ParentPrefs,
    plannerApi: PlannerAPI.PlannerInterface
) : CreateUpdateToDoRepository(plannerApi) {

    override suspend fun getCourses(): List<Course> {
        val params = RestParams(usePerPageQueryParam = true)

        val coursesResult = coursesApi.firstPageObserveeCourses(params)
            .depaginate { nextUrl -> coursesApi.next(nextUrl, params) }
            .dataOrNull.orEmpty()

        val currentStudent = parentPrefs.currentStudent

        return coursesResult.filter { it.isObserver }.filter {
            it.enrollments?.any { enrollment -> enrollment.userId == currentStudent?.id }.orDefault()
        }
    }
}