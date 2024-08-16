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

package com.instructure.parentapp.features.courses.details

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Tab


class CourseDetailsRepository(
    private val courseApi: CourseAPI.CoursesInterface,
    private val tabApi: TabAPI.TabsInterface
) {

    suspend fun getCourse(id: Long, forceRefresh: Boolean): Course {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourseWithSyllabus(id, params).dataOrThrow
    }

    suspend fun getCourseTabs(id: Long, forceRefresh: Boolean): List<Tab> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return tabApi.getTabs(id, CanvasContext.Type.COURSE.apiString, params).dataOrThrow
    }

    suspend fun getCourseSettings(id: Long, forceRefresh: Boolean): CourseSettings {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourseSettings(id, params).dataOrThrow
    }
}
