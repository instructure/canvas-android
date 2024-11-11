/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.dashboard.edit.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentEditDashboardNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)

    private val dataSource = StudentEditDashboardNetworkDataSource(courseApi, groupApi)

    @Test
    fun `Get the correct courses when all requests are successful`() = runTest {
        coEvery { courseApi.firstPageCoursesByEnrollmentState("active", any()) } returns
            DataResult.Success(listOf(Course(1, name = "Course 1")))

        coEvery { courseApi.firstPageCoursesByEnrollmentState("completed", any()) } returns
            DataResult.Success(listOf(Course(2, name = "Course 2")))

        coEvery { courseApi.firstPageCoursesByEnrollmentState("invited_or_pending", any()) } returns
            DataResult.Success(listOf(Course(3, name = "Course 3")))

        val result = dataSource.getCourses()

        assertEquals(3, result.flatten().size)
        assertEquals("Course 1", result.flatten().first().name)
        assertEquals("Course 2", result.flatten()[1].name)
        assertEquals("Course 3", result.flatten()[2].name)
    }

    @Test
    fun `Do not show unpublished future courses`() = runTest {
        coEvery { courseApi.firstPageCoursesByEnrollmentState("active", any()) } returns
            DataResult.Success(listOf(Course(1, name = "Course 1")))

        coEvery { courseApi.firstPageCoursesByEnrollmentState("completed", any()) } returns
            DataResult.Success(listOf(Course(2, name = "Course 2")))

        coEvery { courseApi.firstPageCoursesByEnrollmentState("invited_or_pending", any()) } returns
            DataResult.Success(listOf(Course(3, name = "Course 3"), Course(4, name = "Course 4", workflowState = Course.WorkflowState.UNPUBLISHED)))

        val result = dataSource.getCourses()

        assertEquals(3, result.flatten().size)
        assertEquals("Course 1", result.flatten().first().name)
        assertEquals("Course 2", result.flatten()[1].name)
        assertEquals("Course 3", result.flatten()[2].name)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when at least one request fails`() = runTest {
        coEvery { courseApi.firstPageCoursesByEnrollmentState("active", any()) } returns
            DataResult.Success(listOf(Course(1, name = "Course 1")))

        coEvery { courseApi.firstPageCoursesByEnrollmentState("completed", any()) } returns
            DataResult.Success(listOf(Course(2, name = "Course 2")))

        coEvery { courseApi.firstPageCoursesByEnrollmentState("invited_or_pending", any()) } returns
            DataResult.Fail()

        dataSource.getCourses()
    }

    @Test
    fun `getGroups returns empty list if it's failed`() = runTest {
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val result = dataSource.getGroups()

        assertEquals(emptyList<Group>(), result)
    }

    @Test
    fun `getGroups returns correct groups`() = runTest {
        val groups = listOf(Group(1), Group(2))
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val result = dataSource.getGroups()

        assertEquals(groups, result)
    }
}