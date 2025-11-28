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

import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.ObserveeAssignment
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class AssignmentDetailsNetworkDataSourceTest {

    private val coursesInterface: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val assignmentInterface: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val quizInterface: QuizAPI.QuizInterface = mockk(relaxed = true)
    private val submissionInterface: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)
    private val customGradeStatusesManager: CustomGradeStatusesManager = mockk(relaxed = true)

    private val dataSource = AssignmentDetailsNetworkDataSource(
        coursesInterface,
        assignmentInterface,
        quizInterface,
        submissionInterface,
        customGradeStatusesManager
    )

    @Test
    fun `Get course successfully returns data`() = runTest {
        val expected = Course(1)
        coEvery { coursesInterface.getCourseWithGrade(any(), any()) } returns DataResult.Success(expected)

        val courseResult = dataSource.getCourseWithGrade(1, true)

        Assert.assertEquals(expected, courseResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course failure throws exception`() = runTest {
        coEvery { coursesInterface.getCourseWithGrade(any(), any()) } returns DataResult.Fail()

        dataSource.getCourseWithGrade(1, true)
    }

    @Test
    fun `Get assignment as student successfully returns data`() = runTest {
        val expected = Assignment(1)
        coEvery { assignmentInterface.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = dataSource.getAssignment(false, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get assignment as observer successfully returns data`() = runTest {
        val observeeAssignment = ObserveeAssignment(1, submissionList = listOf(Submission()))
        val expected = observeeAssignment.toAssignmentForObservee()
        coEvery { assignmentInterface.getAssignmentIncludeObservees(any(), any(), any()) } returns DataResult.Success(observeeAssignment)

        val assignmentResult = dataSource.getAssignment(true, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment failure throws exception`() = runTest {
        coEvery { assignmentInterface.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Fail()

        dataSource.getAssignment(false, 1, 1, true)
    }

    @Test(expected = IllegalAccessException::class)
    fun `Get assignment failure throws IllegalAccessException on auth error`() = runTest {
        coEvery { assignmentInterface.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Fail(failure = Failure.Authorization())

        dataSource.getAssignment(false, 1, 1, true)
    }

    @Test
    fun `Get quiz successfully returns data`() = runTest {
        val expected = Quiz()
        coEvery { quizInterface.getQuiz(any(), any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = dataSource.getQuiz(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get quiz failure throws exception`() = runTest {
        coEvery { quizInterface.getQuiz(any(), any(), any()) } returns DataResult.Fail()

        dataSource.getQuiz(1, 1, true)
    }

    @Test
    fun `Get LTI by launch url successfully returns data`() = runTest {
        val expected = LTITool()
        coEvery { assignmentInterface.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = dataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get LTI by launch url failure returns null`() = runTest {
        coEvery { assignmentInterface.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        Assert.assertNull(result)
    }

    @Test
    fun `Get LTI by auth url successfully returns data`() = runTest {
        val expected = LTITool()
        coEvery { submissionInterface.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(expected)

        val assignmentResult = dataSource.getLtiFromAuthenticationUrl("", true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get LTI by auth url failure throws exception`() = runTest {
        coEvery { submissionInterface.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Fail()

        dataSource.getLtiFromAuthenticationUrl("", true)
    }

    @Test
    fun `Get custom grade statuses returns data`() = runTest {
        val node1 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { name } returns "Custom Status 1"
            every { _id } returns "123"
        }

        val node2 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { name } returns "Custom Status 2"
            every { _id } returns "456"
        }

        val connection = mockk<CustomGradeStatusesQuery.CustomGradeStatusesConnection> {
            every { nodes } returns listOf(node1, node2, null)
        }

        val course = mockk<CustomGradeStatusesQuery.Course> {
            every { customGradeStatusesConnection } returns connection
        }

        val data = mockk<CustomGradeStatusesQuery.Data> {
            every { this@mockk.course } returns course
        }

        coEvery { customGradeStatusesManager.getCustomGradeStatuses(1L, true) } returns data

        val result = dataSource.getCustomGradeStatuses(1L, true)

        Assert.assertEquals(listOf(node1, node2), result)
    }

    @Test(expected = Exception::class)
    fun `Get custom grade statuses throws exception when fetch fails`() = runTest {
        coEvery { customGradeStatusesManager.getCustomGradeStatuses(1L, true) } throws Exception("Network error")

        dataSource.getCustomGradeStatuses(1L, true)
    }
}