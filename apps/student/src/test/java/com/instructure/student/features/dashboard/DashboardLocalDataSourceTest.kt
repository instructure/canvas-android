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

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.facade.CourseFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk(relaxed = true)
    private val dashboardCardDao: DashboardCardDao = mockk(relaxed = true)

    private val dataSource = DashboardLocalDataSource(courseFacade, dashboardCardDao)

    @Test
    fun `GetCourses returns all courses`() = runTest {
        val courses = listOf(Course(1), Course(2))
        coEvery { courseFacade.getAllCourses() } returns courses

        val result = dataSource.getCourses(false)

        Assert.assertEquals(courses, result)
    }

    @Test
    fun `GetGroups returns empty list`() = runTest {
        val result = dataSource.getGroups(false)

        Assert.assertEquals(emptyList<Course>(), result)
    }
}