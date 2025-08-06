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

package com.instructure.student.features.offline.assignmentdetails

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.CustomGradeStatusDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class AssignmentDetailsLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk(relaxed = true)
    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)
    private val quizDao: QuizDao = mockk(relaxed = true)
    private val customGradeStatusDao: CustomGradeStatusDao = mockk(relaxed = true)

    private val dataSource = AssignmentDetailsLocalDataSource(courseFacade, assignmentFacade, quizDao, customGradeStatusDao)

    @Test
    fun `Get course successfully returns api model`() = runTest {
        val expected = Course(1)
        coEvery { courseFacade.getCourseById(any()) } returns expected

        val course = dataSource.getCourseWithGrade(1, true)

        Assert.assertEquals(expected, course)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course failure throws exception`() = runTest {
        coEvery { courseFacade.getCourseById(any()) } returns null

        dataSource.getCourseWithGrade(1, true)
    }

    @Test
    fun `Get assignment successfully returns api model`() = runTest {
        val expected = Assignment(1)
        coEvery { assignmentFacade.getAssignmentById(any()) } returns expected

        val assignment = dataSource.getAssignment(false, 1, 1, true)

        Assert.assertEquals(expected, assignment)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment failure throws exception`() = runTest {
        coEvery { assignmentFacade.getAssignmentById(any()) } returns null

        dataSource.getAssignment(false, 1, 1, true)
    }

    @Test
    fun `Get quiz successfully returns api model`() = runTest {
        val expected = Quiz(1)
        coEvery { quizDao.findById(any()) } returns QuizEntity(expected, 1L)

        val quiz = dataSource.getQuiz(1, 1, true)

        Assert.assertEquals(expected, quiz)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get quiz failure throws exception`() = runTest {
        coEvery { quizDao.findById(any()) } returns null

        dataSource.getQuiz(1, 1, true)
    }
}