/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.calendartodo.createupdate

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository

class StudentCreateUpdateToDoRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    plannerApi: PlannerAPI.PlannerInterface
) : CreateUpdateToDoRepository(plannerApi) {

    override suspend fun getCourses(): List<Course> {
        val params = RestParams()
        return coursesApi.getFirstPageCoursesCalendar(params)
            .depaginate { nextUrl ->
                coursesApi.next(nextUrl, params)
            }
            .map {
                it.filter { course ->
                    !course.accessRestrictedByDate && course.hasActiveEnrollment() && course.isStudent
                }
            }
            .dataOrNull
            .orEmpty()
    }
}