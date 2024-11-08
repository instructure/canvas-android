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

package com.instructure.student.test.assignment.details.submissionDetails

import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade
import com.instructure.student.mobius.assignmentDetails.submissionDetails.datasource.SubmissionDetailsLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SubmissionDetailsLocalDataSourceTest {

    private val enrollmentFacade: EnrollmentFacade = mockk(relaxed = true)
    private val submissionFacade: SubmissionFacade = mockk(relaxed = true)
    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)
    private val quizDao: QuizDao = mockk(relaxed = true)
    private val courseFeaturesDao: CourseFeaturesDao = mockk(relaxed = true)
    private val courseSettingsDao: CourseSettingsDao = mockk(relaxed = true)
    private lateinit var localDataSource: SubmissionDetailsLocalDataSource

    @Before
    fun setup() {
        localDataSource =
            SubmissionDetailsLocalDataSource(enrollmentFacade, submissionFacade, assignmentFacade, quizDao, courseFeaturesDao, courseSettingsDao)
    }

    @Test
    fun `Return observee enrollment api model list`() = runTest {
        val expected = listOf(Enrollment(1), Enrollment(2))

        coEvery { enrollmentFacade.getAllEnrollments() } returns expected

        val result = localDataSource.getObserveeEnrollments(true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            enrollmentFacade.getAllEnrollments()
        }
    }

    @Test
    fun `Return empty observee enrollment api model list if enrollments not found`() = runTest {
        val expected = emptyList<Enrollment>()

        coEvery { enrollmentFacade.getAllEnrollments() } returns expected

        val result = localDataSource.getObserveeEnrollments(true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            enrollmentFacade.getAllEnrollments()
        }
    }

    @Test
    fun `Return submission api model`() = runTest {
        val expected = Submission(1)

        coEvery { submissionFacade.findByAssignmentId(1) } returns expected

        val result = localDataSource.getSingleSubmission(1, 1, 1, true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            submissionFacade.findByAssignmentId(1)
        }
    }

    @Test
    fun `Return failed data result if submission not found`() = runTest {
        coEvery { submissionFacade.findByAssignmentId(1) } returns null

        val result = localDataSource.getSingleSubmission(1, 1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            submissionFacade.findByAssignmentId(1)
        }
    }

    @Test
    fun `Return assignment api model`() = runTest {
        val expected = Assignment(1)

        coEvery { assignmentFacade.getAssignmentById(1) } returns expected

        val result = localDataSource.getAssignment(1, 1, true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            assignmentFacade.getAssignmentById(1)
        }
    }

    @Test
    fun `Return failed data result if assignment not found`() = runTest {
        coEvery { assignmentFacade.getAssignmentById(1) } returns null

        val result = localDataSource.getAssignment(1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            assignmentFacade.getAssignmentById(1)
        }
    }

    @Test
    fun `Return failed data result for external tool launch url call`() = runTest {
        val result = localDataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return failed data result for lti from authentication url call`() = runTest {
        val result = localDataSource.getLtiFromAuthenticationUrl("url", true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return quiz api model`() = runTest {
        val expected = Quiz(1)

        coEvery { quizDao.findById(1) } returns QuizEntity(expected, 1L)

        val result = localDataSource.getQuiz(1, 1, true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            quizDao.findById(1)
        }
    }

    @Test
    fun `Return failed data result if quiz not found`() = runTest {
        coEvery { quizDao.findById(1) } returns null

        val result = localDataSource.getQuiz(1, 1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            quizDao.findById(1)
        }
    }

    @Test
    fun `Return course features api model`() = runTest {
        val expected = listOf("feature")

        coEvery { courseFeaturesDao.findByCourseId(1) } returns CourseFeaturesEntity(1, expected)

        val result = localDataSource.getCourseFeatures(1, true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            courseFeaturesDao.findByCourseId(1)
        }
    }

    @Test
    fun `Return failed data result if course features not found`() = runTest {
        coEvery { courseFeaturesDao.findByCourseId(1) } returns null

        val result = localDataSource.getCourseFeatures(1, true)

        TestCase.assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            courseFeaturesDao.findByCourseId(1)
        }
    }

    @Test
    fun `Load course settings successfully returns api model`() = runTest {
        val expected = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseSettingsDao.findByCourseId(any()) } returns CourseSettingsEntity(expected, 1L)

        val result = localDataSource.loadCourseSettings(1, true)

        assertEquals(expected, result)
    }
}
