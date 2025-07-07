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

package com.instructure.student.features.calendarevent

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.isGroup
import com.instructure.student.features.calendarevent.createupdate.StudentCreateUpdateEventRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class StudentCreateUpdateEventRepositoryTest {

    private val eventsApi: CalendarEventAPI.CalendarEventInterface = mockk(relaxed = true)
    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupsApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val repository = StudentCreateUpdateEventRepository(eventsApi, coursesApi, groupsApi, apiPrefs)

    @Test
    fun `Get contexts returns the user only if course request is failed`() = runTest {
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Fail()
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val result = repository.getCanvasContexts()

        Assert.assertEquals(1, result.size)
    }

    @Test
    fun `Get contexts adds user context when course request is successful`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Fail()
        coEvery { apiPrefs.user } returns User(1, "Test User")

        val canvasContextsResults = repository.getCanvasContexts()

        Assert.assertEquals(1, canvasContextsResults[0].id)
        Assert.assertEquals("Test User", canvasContextsResults[0].name)
    }

    @Test
    fun `Get contexts returns groups if successful`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        val groups = listOf(Group(id = 63, courseId = 44, name = "First group"))
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = repository.getCanvasContexts()

        Assert.assertEquals(groups[0].id, canvasContextsResults[1].id)
        Assert.assertEquals(groups[0].name, canvasContextsResults[1].name)
    }

    @Test
    fun `Get contexts returns only valid groups`() = runTest {
        val courses = listOf(Course(44, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))))
        val groups = listOf(
            Group(id = 63, courseId = 44, name = "First group"),
            Group(id = 63, courseId = 33, name = "First group"), // Invalid course id
        )
        coEvery { coursesApi.getFirstPageCoursesCalendar(any()) } returns DataResult.Success(courses)
        coEvery { groupsApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val canvasContextsResults = repository.getCanvasContexts()

        Assert.assertEquals(1, canvasContextsResults.count { it.isGroup })
        Assert.assertEquals(groups[0].id, canvasContextsResults[1].id)
        Assert.assertEquals(groups[0].name, canvasContextsResults[1].name)
    }
}
