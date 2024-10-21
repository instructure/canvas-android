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

package com.instructure.student.features.grades.datasource

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GradesListNetworkDataSourceTest {

    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)

    private val dataSource = GradesListNetworkDataSource(courseApi, enrollmentApi, assignmentApi, submissionApi)

    @Test
    fun `Get course with grade successfully returns data`() = runTest {
        val expected = Course(1)

        coEvery { courseApi.getCourseWithGrade(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getCourseWithGrade(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course with grade failure throws exception`() = runTest {
        coEvery { courseApi.getCourseWithGrade(any(), any()) } returns DataResult.Fail()

        dataSource.getCourseWithGrade(1, true)
    }

    @Test
    fun `Get observee enrollments successfully returns data`() = runTest {
        val expected = listOf(Enrollment(1))

        coEvery { enrollmentApi.firstPageObserveeEnrollments(any()) } returns DataResult.Success(expected)

        val result = dataSource.getObserveeEnrollments(true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get observee enrollments failure throws exception`() = runTest {
        coEvery { enrollmentApi.firstPageObserveeEnrollments(any()) } returns DataResult.Fail()

        dataSource.getObserveeEnrollments(true)
    }

    @Test
    fun `Get assignment groups with assignments for grading period successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Success(expected)

        val result = dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment groups with assignments for grading period failure throws exception`() = runTest {
        coEvery {
            assignmentApi.getFirstPageAssignmentGroupListWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail()

        dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)
    }

    @Test
    fun `Get submissions for multiple assignments successfully returns data`() = runTest {
        val expected = listOf(Submission(1))

        coEvery { submissionApi.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get submissions for multiple assignments failure throws exception`() = runTest {
        coEvery { submissionApi.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true)
    }

    @Test
    fun `Get courses with syllabus successfully returns data`() = runTest {
        val expected = listOf(Course(1))

        coEvery { courseApi.firstPageCoursesWithSyllabus(any()) } returns DataResult.Success(expected)

        val result = dataSource.getCoursesWithSyllabus(true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get courses with syllabus failure throws exception`() = runTest {
        coEvery { courseApi.firstPageCoursesWithSyllabus(any()) } returns DataResult.Fail()

        dataSource.getCoursesWithSyllabus(true)
    }

    @Test
    fun `Get grading period for course successfully returns data`() = runTest {
        val expected = listOf(GradingPeriod(1))

        coEvery { courseApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Success(GradingPeriodResponse(expected))

        val result = dataSource.getGradingPeriodsForCourse(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get grading period for course failure throws exception`() = runTest {
        coEvery { courseApi.getGradingPeriodsForCourse(any(), any()) } returns DataResult.Fail()

        dataSource.getGradingPeriodsForCourse(1, true)
    }

    @Test
    fun `Get user enrollments for grading period successfully returns data`() = runTest {
        val expected = listOf(Enrollment(1))

        coEvery { courseApi.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get user enrollments for grading period failure throws exception`() = runTest {
        coEvery { courseApi.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)
    }

    @Test
    fun `Get assignment groups with assignments successfully returns data`() = runTest {
        val expected = listOf(AssignmentGroup(1))

        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.getAssignmentGroupsWithAssignments(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get assignment groups with assignments failure throws exception`() = runTest {
        coEvery { assignmentApi.getFirstPageAssignmentGroupListWithAssignments(any(), any()) } returns DataResult.Fail()

        dataSource.getAssignmentGroupsWithAssignments(1, true)
    }
}
