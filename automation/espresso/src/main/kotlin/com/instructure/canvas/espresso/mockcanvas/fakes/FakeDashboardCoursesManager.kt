/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvasapi2.CourseAnnouncementsQuery
import com.instructure.canvasapi2.DashboardCoursesQuery
import com.instructure.canvasapi2.DashboardSingleCourseQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager

class FakeDashboardCoursesManager : DashboardCoursesManager {

    override suspend fun getDashboardCourses(
        forceNetwork: Boolean
    ): DashboardCoursesQuery.Data {
        return DashboardCoursesQuery.Data(allCourses = emptyList())
    }

    override suspend fun getSingleCourse(
        courseId: Long,
        forceNetwork: Boolean
    ): DashboardSingleCourseQuery.Data {
        return DashboardSingleCourseQuery.Data(course = null)
    }

    override suspend fun getCourseAnnouncements(
        courseId: Long,
        forceNetwork: Boolean
    ): CourseAnnouncementsQuery.Data {
        return CourseAnnouncementsQuery.Data(course = null)
    }
}