/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.canvasapi2.utils.DataResult
import java.util.Date

class FakeGetHorizonCourseManager(): HorizonGetCoursesManager {
    override suspend fun getCoursesWithProgress(
        userId: Long,
        forceNetwork: Boolean
    ): DataResult<List<CourseWithProgress>> {
        return DataResult.Success(getCourses())
    }

    override suspend fun getEnrollments(
        userId: Long,
        forceNetwork: Boolean
    ): DataResult<List<GetCoursesQuery.Enrollment>> {
        val activeCourse = getCourses()[0]
        val completedCourse = getCourses()[1]
        val invitedCourse = getCourses()[2]
        return DataResult.Success(
            listOf(
                GetCoursesQuery.Enrollment(
                    id = MockCanvas.data.enrollments.values.toList()[0].id.toString(),
                    state = EnrollmentWorkflowState.active,
                    lastActivityAt = Date(),
                    course = GetCoursesQuery.Course(
                        id = activeCourse.courseId.toString(),
                        name = activeCourse.courseName,
                        image_download_url = null,
                        syllabus_body = activeCourse.courseSyllabus,
                        account = GetCoursesQuery.Account(
                            "Account 1"
                        ),
                        usersConnection = null
                    )
                ),
                GetCoursesQuery.Enrollment(
                    id = MockCanvas.data.enrollments.values.toList()[1].id.toString(),
                    state = EnrollmentWorkflowState.completed,
                    lastActivityAt = Date(),
                    course = GetCoursesQuery.Course(
                        id = completedCourse.courseId.toString(),
                        name = completedCourse.courseName,
                        image_download_url = null,
                        syllabus_body = completedCourse.courseSyllabus,
                        account = GetCoursesQuery.Account(
                            "Account 1"
                        ),
                        usersConnection = null
                    )
                )
            )
        )
    }

    override suspend fun getProgramCourses(
        courseId: Long,
        forceNetwork: Boolean
    ): DataResult<CourseWithModuleItemDurations> {
        return DataResult.Success(
            CourseWithModuleItemDurations(
                courseId = courseId,
                courseName = "Program Course",
            )
        )
    }

    fun getCourses(): List<CourseWithProgress> {
        val courses = MockCanvas.data.courses.values.toList()
        val activeCourse = if (courses.size > 0) {
            CourseWithProgress(
                courseId = courses[0].id,
                courseName = courses[0].name,
                courseSyllabus = "Syllabus for Course 1",
                progress = 0.25
            )
        } else { null }
        val completedCourse = if (courses.size > 1) {
            CourseWithProgress(
                courseId = courses[1].id,
                courseName = courses[1].name,
                courseSyllabus = "Syllabus for Course 2",
                progress = 1.0
            )
        } else { null }
        val invitedCourse = if (courses.size > 2) {
            CourseWithProgress(
                courseId = courses[2].id,
                courseName = courses[2].name,
                courseSyllabus = null,
                progress = 0.0
            )
        } else { null }

        return listOfNotNull(activeCourse, completedCourse, invitedCourse)
    }
}