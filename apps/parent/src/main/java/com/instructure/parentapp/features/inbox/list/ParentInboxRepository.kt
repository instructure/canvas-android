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

package com.instructure.parentapp.features.inbox.list

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.features.inbox.list.InboxRepository

class ParentInboxRepository(
    inboxApi: InboxApi.InboxInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    groupsApi: GroupAPI.GroupInterface,
    progressApi: ProgressAPI.ProgressInterface,
    inboxSettingsManager: InboxSettingsManager,
    featuresApi: FeaturesAPI.FeaturesInterface
) : InboxRepository(inboxApi, groupsApi, progressApi, inboxSettingsManager, featuresApi) {

    override suspend fun getCourses(params: RestParams): DataResult<List<Course>> {
        val coursesResult = coursesApi.getCoursesByEnrollmentType(Enrollment.EnrollmentType.Observer.apiTypeString, params)
            .depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        if (coursesResult.isFail) return coursesResult

        val courses = (coursesResult as DataResult.Success).data

        val validCourses = courses.filter { it.isValidTerm() && it.hasActiveEnrollment() }
        return DataResult.Success(validCourses)
    }
}
