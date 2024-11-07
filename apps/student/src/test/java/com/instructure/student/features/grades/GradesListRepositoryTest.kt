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

package com.instructure.student.features.grades

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.grades.datasource.GradesListLocalDataSource
import com.instructure.student.features.grades.datasource.GradesListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GradesListRepositoryTest {

    private val networkDataSource: GradesListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: GradesListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = GradesListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get course with grade if device is online`() = runTest {
        val onlineExpected = Course(1)
        val offlineExpected = Course(2)

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourseWithGrade(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getCourseWithGrade(any(), any()) } returns offlineExpected

        val result = repository.getCourseWithGrade(1, true)

        coVerify { networkDataSource.getCourseWithGrade(1, true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get course with grade if device is offline`() = runTest {
        val onlineExpected = Course(1)
        val offlineExpected = Course(2)

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourseWithGrade(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getCourseWithGrade(any(), any()) } returns offlineExpected

        val result = repository.getCourseWithGrade(1, true)

        coVerify { localDataSource.getCourseWithGrade(1, true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get observee enrollments if device is online`() = runTest {
        val onlineExpected = listOf(Enrollment(1))
        val offlineExpected = listOf(Enrollment(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getObserveeEnrollments(any()) } returns onlineExpected
        coEvery { localDataSource.getObserveeEnrollments(any()) } returns offlineExpected

        val result = repository.getObserveeEnrollments(true)

        coVerify { networkDataSource.getObserveeEnrollments(true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get observee enrollments if device is offline`() = runTest {
        val onlineExpected = listOf(Enrollment(1))
        val offlineExpected = listOf(Enrollment(2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getObserveeEnrollments(any()) } returns onlineExpected
        coEvery { localDataSource.getObserveeEnrollments(any()) } returns offlineExpected

        val result = repository.getObserveeEnrollments(true)

        coVerify { localDataSource.getObserveeEnrollments(true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period if device is online`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))
        val offlineExpected = listOf(AssignmentGroup(id = 3), AssignmentGroup(id = 4))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns offlineExpected

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period if device is offline`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1), AssignmentGroup(id = 2))
        val offlineExpected = listOf(AssignmentGroup(id = 3), AssignmentGroup(id = 4))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns offlineExpected

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period if there are hidden assignments and device is online`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3))))
        val onlineResult = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1), Assignment(2, isHiddenInGradeBook = true))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3), Assignment(4, isHiddenInGradeBook = true))))
        val offlineExpected = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7))))
        val offlineResult = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5), Assignment(6, isHiddenInGradeBook = true))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7), Assignment(8, isHiddenInGradeBook = true))))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns onlineResult
        coEvery { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns offlineResult

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments for grading period if there are hidden assignments and device is offline`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3))))
        val onlineResult = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1), Assignment(2, isHiddenInGradeBook = true))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3), Assignment(4, isHiddenInGradeBook = true))))
        val offlineExpected = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7))))
        val offlineResult = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5), Assignment(6, isHiddenInGradeBook = true))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7), Assignment(8, isHiddenInGradeBook = true))))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns onlineResult
        coEvery { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(any(), any(), any(), any()) } returns offlineResult

        val result = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(1, 1, scopeToStudent = true, forceNetwork = true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get submissions for multiple assignments if device is online`() = runTest {
        val onlineExpected = listOf(Submission(1))
        val offlineExpected = listOf(Submission(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns offlineExpected

        val result = repository.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true)

        coVerify { networkDataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get submissions for multiple assignments if device is offline`() = runTest {
        val onlineExpected = listOf(Submission(1))
        val offlineExpected = listOf(Submission(2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getSubmissionsForMultipleAssignments(any(), any(), any(), any()) } returns offlineExpected

        val result = repository.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true)

        coVerify { localDataSource.getSubmissionsForMultipleAssignments(1, 1, listOf(1), true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get course with syllabus if device is online`() = runTest {
        val onlineExpected = listOf(Course(1))
        val offlineExpected = listOf(Course(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCoursesWithSyllabus(any()) } returns onlineExpected
        coEvery { localDataSource.getCoursesWithSyllabus(any()) } returns offlineExpected

        val result = repository.getCoursesWithSyllabus(true)

        coVerify { networkDataSource.getCoursesWithSyllabus(true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get course with syllabus if device is offline`() = runTest {
        val onlineExpected = listOf(Course(1))
        val offlineExpected = listOf(Course(2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCoursesWithSyllabus(any()) } returns onlineExpected
        coEvery { localDataSource.getCoursesWithSyllabus(any()) } returns offlineExpected

        val result = repository.getCoursesWithSyllabus(true)

        coVerify { localDataSource.getCoursesWithSyllabus(true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get grading periods for course if device is online`() = runTest {
        val onlineExpected = listOf(GradingPeriod(1))
        val offlineExpected = listOf(GradingPeriod(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getGradingPeriodsForCourse(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getGradingPeriodsForCourse(any(), any()) } returns offlineExpected

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { networkDataSource.getGradingPeriodsForCourse(1, true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get grading periods for course if device is offline`() = runTest {
        val onlineExpected = listOf(GradingPeriod(1))
        val offlineExpected = listOf(GradingPeriod(2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getGradingPeriodsForCourse(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getGradingPeriodsForCourse(any(), any()) } returns offlineExpected

        val result = repository.getGradingPeriodsForCourse(1, true)

        coVerify { localDataSource.getGradingPeriodsForCourse(1, true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get user enrollments for grading period if device is online`() = runTest {
        val onlineExpected = listOf(Enrollment(1))
        val offlineExpected = listOf(Enrollment(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns offlineExpected

        val result = repository.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)

        coVerify { networkDataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get user enrollments for grading period if device is offline`() = runTest {
        val onlineExpected = listOf(Enrollment(1))
        val offlineExpected = listOf(Enrollment(2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any()) } returns offlineExpected

        val result = repository.getUserEnrollmentsForGradingPeriod(1, 1, 1, true)

        coVerify { localDataSource.getUserEnrollmentsForGradingPeriod(1, 1, 1, true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments if device is online`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(2))))
        val offlineExpected = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(4))))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns offlineExpected

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignments(1, true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments if device is offline`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(2))))
        val offlineExpected = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(4))))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns onlineExpected
        coEvery { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns offlineExpected

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignments(1, true) }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments if there are hidden assignments and device is online`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3))))
        val onlineResult = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1), Assignment(2, isHiddenInGradeBook = true))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3), Assignment(4, isHiddenInGradeBook = true))))
        val offlineExpected = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7))))
        val offlineResult = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5), Assignment(6, isHiddenInGradeBook = true))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7), Assignment(8, isHiddenInGradeBook = true))))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns onlineResult
        coEvery { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns offlineResult

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { networkDataSource.getAssignmentGroupsWithAssignments(1, true) }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get assignment groups with assignments if there are hidden assignments and device is offline`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3))))
        val onlineResult = listOf(AssignmentGroup(id = 1, assignments = listOf(Assignment(1), Assignment(2, isHiddenInGradeBook = true))), AssignmentGroup(id = 2, assignments = listOf(Assignment(3), Assignment(4, isHiddenInGradeBook = true))))
        val offlineExpected = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7))))
        val offlineResult = listOf(AssignmentGroup(id = 3, assignments = listOf(Assignment(5), Assignment(6, isHiddenInGradeBook = true))), AssignmentGroup(id = 4, assignments = listOf(Assignment(7), Assignment(8, isHiddenInGradeBook = true))))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns onlineResult
        coEvery { localDataSource.getAssignmentGroupsWithAssignments(any(), any()) } returns offlineResult

        val result = repository.getAssignmentGroupsWithAssignments(1, true)

        coVerify { localDataSource.getAssignmentGroupsWithAssignments(1, true) }
        assertEquals(offlineExpected, result)
    }
}
