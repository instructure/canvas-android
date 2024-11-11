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
package com.instructure.student.features.dashboard

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class DashboardNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)

    private val dataSource = DashboardNetworkDataSource(courseApi, groupApi, apiPrefs, userApi)

    @Test
    fun `getCourses returns courses for teacher if we are in the student view`() = runTest {
        val teacherCourses = listOf(Course(1), Course(2))
        val studentCourses = listOf(Course(3), Course(4))
        coEvery { courseApi.getFirstPageCoursesTeacher(any()) } returns DataResult.Success(teacherCourses)
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(studentCourses)
        every { apiPrefs.isStudentView } returns true

        val result = dataSource.getCourses(true)

        Assert.assertEquals(teacherCourses, result)
    }

    @Test
    fun `getCourses returns courses for student if we are not in the student view`() = runTest {
        val teacherCourses = listOf(Course(1), Course(2))
        val studentCourses = listOf(Course(3), Course(4))
        coEvery { courseApi.getFirstPageCoursesTeacher(any()) } returns DataResult.Success(teacherCourses)
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Success(studentCourses)
        every { apiPrefs.isStudentView } returns false

        val result = dataSource.getCourses(true)

        Assert.assertEquals(studentCourses, result)
    }

    @Test
    fun `getCourses returns empty list if it's failed`() = runTest {
        coEvery { courseApi.getFirstPageCourses(any()) } returns DataResult.Fail()
        every { apiPrefs.isStudentView } returns false

        val result = dataSource.getCourses(true)

        Assert.assertEquals(emptyList<Course>(), result)
    }

    @Test
    fun `getGroups returns empty list if it's failed`() = runTest {
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Fail()

        val result = dataSource.getGroups(true)

        Assert.assertEquals(emptyList<Group>(), result)
    }

    @Test
    fun `getGroups returns correct groups`() = runTest {
        val groups = listOf(Group(1), Group(2))
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)

        val result = dataSource.getGroups(true)

        Assert.assertEquals(groups, result)
    }

    @Test
    fun `Returns list of Dashboard cards if getDashboardCourses is successful`() = runTest {
        val expected = listOf(DashboardCard(id = 1), DashboardCard(id = 2))
        coEvery { courseApi.getDashboardCourses(any()) } returns DataResult.Success(expected)

        val result = dataSource.getDashboardCards(true)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Returns empty list if getDashboardCourses is failed`() = runTest {
        coEvery { courseApi.getDashboardCourses(any()) } returns DataResult.Fail()

        val result = dataSource.getDashboardCards(true)

        Assert.assertEquals(emptyList<DashboardCard>(), result)
    }
}