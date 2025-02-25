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

import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GradesListLocalDataSourceTest {

    private val courseFacade: CourseFacade = mockk(relaxed = true)
    private val enrollmentFacade: EnrollmentFacade = mockk(relaxed = true)
    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)
    private val submissionFacade: SubmissionFacade = mockk(relaxed = true)

    private val dataSource = GradesListLocalDataSource(courseFacade, enrollmentFacade, assignmentFacade, submissionFacade)

    @Test
    fun `Get course with grade successfully returns api model`() = runTest {
        val expected = Course(1L)

        coEvery { courseFacade.getCourseById(any()) } returns expected

        val result = dataSource.getCourseWithGrade(1, true)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course with grade failure throws exception`() = runTest {
        coEvery { courseFacade.getCourseById(any()) } returns null

        dataSource.getCourseWithGrade(1, true)
    }

    @Test
    fun `Get observee enrollments successfully returns api model`() = runTest {
        val expected = listOf(Enrollment(1), Enrollment(2))

        coEvery { enrollmentFacade.getAllEnrollments() } returns expected

        val result = dataSource.getObserveeEnrollments(true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1L), AssignmentGroup(2L))

        coEvery { assignmentFacade.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any()) } returns expected

        val result = dataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get submissions by assignment ids successfully returns api model`() = runTest {
        val expected = listOf(Submission(1), Submission(2))

        coEvery { submissionFacade.findByAssignmentIds(listOf(1, 2)) } returns expected

        val result = dataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1, 2), true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get courses with syllabus successfully returns api model`() = runTest {
        val expected = listOf(Course(1), Course(2))

        coEvery { courseFacade.getAllCourses() } returns expected

        val result = dataSource.getCoursesWithSyllabus(true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get grading periods for course successfully returns api model`() = runTest {
        val expected = listOf(GradingPeriod(1), GradingPeriod(2))

        coEvery { courseFacade.getGradingPeriodsByCourseId(1) } returns expected

        val result = dataSource.getGradingPeriodsForCourse(1, true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get user enrollments for grading period successfully returns api model`() = runTest {
        val expected = listOf(Enrollment(1), Enrollment(2))

        coEvery { enrollmentFacade.getEnrollmentsByGradingPeriodId(1) } returns expected

        val result = dataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)

        assertEquals(expected, result)
    }

    @Test
    fun `Get assignment groups with assignments by course id successfully returns api model`() = runTest {
        val expected = listOf(AssignmentGroup(1), AssignmentGroup(2))

        coEvery { assignmentFacade.getAssignmentGroupsWithAssignments(1) } returns expected

        val result = dataSource.getAssignmentGroupsWithAssignments(1, true)

        assertEquals(expected, result)
    }
}
