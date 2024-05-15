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

package com.instructure.teacher.features.calendarevent

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class TeacherCreateUpdateEventRepositoryTest {

    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val repository = TeacherCreateUpdateEventRepository(calendarEventApi, coursesApi, apiPrefs)

    @Test
    fun `Get contexts returns the user only if course request is failed`() = runTest {
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Fail()

        val result = repository.getCanvasContexts()

        Assert.assertEquals(1, result.size)
    }

    @Test
    fun `Get contexts adds user context and returns it with courses when course request is successful`() = runTest {
        val courses = listOf(
            Course(
                44,
                enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE, type = Enrollment.EnrollmentType.Teacher))
            ),
            Course(
                55,
                enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE, type = Enrollment.EnrollmentType.Student))
            )
        )
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { apiPrefs.user } returns User(1, "Test User")

        val canvasContextsResults = repository.getCanvasContexts()

        Assert.assertEquals(1, canvasContextsResults.first().id)
        Assert.assertEquals("Test User", canvasContextsResults.first().name)
        Assert.assertEquals(courses[0].id, canvasContextsResults[1].id)
    }
}