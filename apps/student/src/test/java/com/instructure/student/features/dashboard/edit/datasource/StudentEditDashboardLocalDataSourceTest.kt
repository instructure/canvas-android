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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.daos.EditDashboardItemDao
import com.instructure.pandautils.room.offline.entities.EditDashboardItemEntity
import com.instructure.pandautils.room.offline.entities.EnrollmentState
import com.instructure.pandautils.room.offline.facade.CourseFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentEditDashboardLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk(relaxed = true)
    private val editDashboardItemDao: EditDashboardItemDao = mockk(relaxed = true)

    private val dataSource = StudentEditDashboardLocalDataSource(courseFacade, editDashboardItemDao)

    @Test
    fun `getCourses returns correct courses partitioned by enrollment state`() = runTest {
        val fullCourse1 = Course(1, name = "Full Course 1", originalName = "Original name")
        val fullCourse2 = Course(2, name = "Full Course 2", originalName = "Original name 2")

        coEvery { editDashboardItemDao.findByEnrollmentState(EnrollmentState.CURRENT) } returns
            listOf(EditDashboardItemEntity(Course(1), EnrollmentState.CURRENT, 0))

        coEvery { editDashboardItemDao.findByEnrollmentState(EnrollmentState.PAST) } returns
            listOf(EditDashboardItemEntity(Course(2), EnrollmentState.PAST, 0))

        coEvery { editDashboardItemDao.findByEnrollmentState(EnrollmentState.FUTURE) } returns
            listOf(EditDashboardItemEntity(Course(3), EnrollmentState.FUTURE, 0))

        coEvery { courseFacade.getCourseById(1) } returns fullCourse1
        coEvery { courseFacade.getCourseById(2) } returns fullCourse2
        coEvery { courseFacade.getCourseById(3) } returns null

        val result = dataSource.getCourses()

        assertEquals(3, result.flatten().size)
        assertEquals(fullCourse1, result.flatten().first())
        assertEquals(fullCourse2, result.flatten()[1])
        assertEquals(Course(3), result.flatten()[2])
    }

    @Test
    fun `getGroups returns empty list`() = runTest {
        assertEquals(emptyList<Group>(), dataSource.getGroups())
    }
}