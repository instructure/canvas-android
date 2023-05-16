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

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class AssignmentDetailsNetworkDataSourceTest {

    private val coursesInterface: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val assignmentInterface: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val quizInterface: QuizAPI.QuizInterface = mockk(relaxed = true)
    private val submissionInterface: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)

    private val dataSource = AssignmentDetailsNetworkDataSource(coursesInterface, assignmentInterface, quizInterface, submissionInterface)

    @Test
    fun `Get course successfully returns data`() = runTest {
        val expected = DataResult.Success(Course(1))
        coEvery { coursesInterface.getCourseWithGrade(any(), any()) } returns expected

        val courseResult = dataSource.getCourseWithGrade(1, true)

        Assert.assertEquals(expected, courseResult)
    }

    @Test
    fun `Get course failure returns failed result`() = runTest {
        val expected = DataResult.Fail()
        coEvery { coursesInterface.getCourseWithGrade(any(), any()) } returns expected

        val courseResult = dataSource.getCourseWithGrade(1, true)

        Assert.assertEquals(expected, courseResult)
    }

    @Test
    fun `Get assignment as student successfully returns data`() = runTest {
        val expected = DataResult.Success(Assignment(1))
        coEvery { assignmentInterface.getAssignmentWithHistory(any(), any(), any()) } returns expected

        val assignmentResult = dataSource.getAssignmentWithHistory(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get assignment as observer successfully returns data`() = runTest {
        val observeeAssignment = ObserveeAssignment(1)
        val expected = DataResult.Success(observeeAssignment.toAssignmentForObservee())
        coEvery { assignmentInterface.getAssignmentIncludeObservees(any(), any(), any()) } returns DataResult.Success(observeeAssignment)

        val assignmentResult = dataSource.getAssignmentIncludeObservees(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get assignment failure returns failed result`() = runTest {
        val expected = DataResult.Fail()
        coEvery { assignmentInterface.getAssignmentWithHistory(any(), any(), any()) } returns expected

        val assignmentResult = dataSource.getAssignmentWithHistory(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get quiz successfully returns data`() = runTest {
        val expected = DataResult.Success(Quiz())
        coEvery { quizInterface.getQuiz(any(), any(), any()) } returns expected

        val assignmentResult = dataSource.getQuiz(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get quiz failure returns failed result`() = runTest {
        val expected = DataResult.Fail()
        coEvery { quizInterface.getQuiz(any(), any(), any()) } returns expected

        val assignmentResult = dataSource.getQuiz(1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get LTI by launch url successfully returns data`() = runTest {
        val expected = DataResult.Success(LTITool())
        coEvery { assignmentInterface.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns expected

        val assignmentResult = dataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get LTI by launch url failure returns failed result`() = runTest {
        val expected = DataResult.Fail()
        coEvery { assignmentInterface.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) } returns expected

        val assignmentResult = dataSource.getExternalToolLaunchUrl(1, 1, 1, true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get LTI by auth url successfully returns data`() = runTest {
        val expected = DataResult.Success(LTITool())
        coEvery { submissionInterface.getLtiFromAuthenticationUrl(any(), any()) } returns expected

        val assignmentResult = dataSource.getLtiFromAuthenticationUrl("", true)

        Assert.assertEquals(expected, assignmentResult)
    }

    @Test
    fun `Get LTI by auth url failure returns failed result`() = runTest {
        val expected = DataResult.Fail()
        coEvery { submissionInterface.getLtiFromAuthenticationUrl(any(), any()) } returns expected

        val assignmentResult = dataSource.getLtiFromAuthenticationUrl("", true)

        Assert.assertEquals(expected, assignmentResult)
    }
}