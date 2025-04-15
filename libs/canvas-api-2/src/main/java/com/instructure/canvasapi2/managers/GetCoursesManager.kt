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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure

class GetCoursesManager {

    suspend fun getCoursesWithProgress(userId: Long, forceNetwork: Boolean): DataResult<List<CourseWithProgress>> {
        return try {
            val query = GetCoursesQuery(userId.toString())
            val result = QLClientConfig.enqueueQuery(query, forceNetwork).dataAssertNoErrors

            val coursesList = result.legacyNode?.onUser?.enrollments?.mapNotNull { mapCourse(it.course) } ?: emptyList()
            return DataResult.Success(coursesList)
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    private fun mapCourse(course: GetCoursesQuery.Course?): CourseWithProgress? {
        val progress = course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage
        val courseId = course?.id?.toLong()
        val courseName = course?.name
        val courseSyllabus = course?.syllabus_body
        val incompleteModulesConnection =
            course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.incompleteModulesConnection?.nodes?.firstOrNull()
        val moduleId = incompleteModulesConnection?.module?.id?.toLong()
        val moduleItemId = incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.id?.toLong()

        return if (courseId != null && courseName != null) {
            CourseWithProgress(Course(courseId, courseName, syllabusBody = courseSyllabus), progress, moduleItemId, moduleId)
        } else {
            null
        }
    }
}

data class CourseWithProgress(val course: Course, val progress: Double?, val nextUpModuleItemId: Long?, val nextUpModuleId: Long?)