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

import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.grades.datasource.GradesLocalDataSource
import com.instructure.student.features.grades.datasource.GradesNetworkDataSource
import com.instructure.student.util.StudentPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StudentGradesRepositoryTest {

    private val networkDataSource: GradesNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: GradesLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val studentPrefs: StudentPrefs = mockk(relaxed = true)

    private val repository = StudentGradesRepository(
        localDataSource,
        networkDataSource,
        networkStateProvider,
        featureFlagProvider,
        apiPrefs,
        studentPrefs
    )

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get assignment groups with grading period id when device is online`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1))
        val offlineExpected = listOf(AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery {
            networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any()
            )
        } returns onlineExpected
        coEvery {
            localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any()
            )
        } returns offlineExpected

        val result = repository.loadAssignmentGroups(1, 1, true)

        coVerify {
            networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                courseId = 1,
                gradingPeriodId = 1,
                scopeToStudent = true,
                forceNetwork = true
            )
        }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get assignment groups with grading period id when device is offline`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1))
        val offlineExpected = listOf(AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery {
            networkDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any()
            )
        } returns onlineExpected
        coEvery {
            localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                any(),
                any(),
                any(),
                any()
            )
        } returns offlineExpected

        val result = repository.loadAssignmentGroups(1, 1, true)

        coVerify {
            localDataSource.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                courseId = 1,
                gradingPeriodId = 1,
                scopeToStudent = true,
                forceNetwork = true
            )
        }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get assignment groups without grading period id when device is online`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1))
        val offlineExpected = listOf(AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery {
            networkDataSource.getAssignmentGroupsWithAssignments(any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getAssignmentGroupsWithAssignments(any(), any())
        } returns offlineExpected

        val result = repository.loadAssignmentGroups(
            courseId = 1,
            gradingPeriodId = null,
            forceRefresh = true
        )

        coVerify {
            networkDataSource.getAssignmentGroupsWithAssignments(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get assignment groups without grading period id when device is offline`() = runTest {
        val onlineExpected = listOf(AssignmentGroup(id = 1))
        val offlineExpected = listOf(AssignmentGroup(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery {
            networkDataSource.getAssignmentGroupsWithAssignments(any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getAssignmentGroupsWithAssignments(any(), any())
        } returns offlineExpected

        val result = repository.loadAssignmentGroups(
            courseId = 1,
            gradingPeriodId = null,
            forceRefresh = true
        )

        coVerify {
            localDataSource.getAssignmentGroupsWithAssignments(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get grading periods when device is online`() = runTest {
        val onlineExpected = listOf(GradingPeriod(id = 1))
        val offlineExpected = listOf(GradingPeriod(id = 2))

        every { networkStateProvider.isOnline() } returns true
        coEvery {
            networkDataSource.getGradingPeriodsForCourse(any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getGradingPeriodsForCourse(any(), any())
        } returns offlineExpected

        val result = repository.loadGradingPeriods(1, true)

        coVerify {
            networkDataSource.getGradingPeriodsForCourse(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get grading periods when device is offline`() = runTest {
        val onlineExpected = listOf(GradingPeriod(id = 1))
        val offlineExpected = listOf(GradingPeriod(id = 2))

        every { networkStateProvider.isOnline() } returns false
        coEvery {
            networkDataSource.getGradingPeriodsForCourse(any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getGradingPeriodsForCourse(any(), any())
        } returns offlineExpected

        val result = repository.loadGradingPeriods(1, true)

        coVerify {
            localDataSource.getGradingPeriodsForCourse(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get enrollments when device is online`() = runTest {
        val onlineExpected = listOf(Enrollment(id = 1))
        val offlineExpected = listOf(Enrollment(id = 2))

        every { apiPrefs.user?.id } returns 1
        every { networkStateProvider.isOnline() } returns true
        coEvery {
            networkDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any())
        } returns offlineExpected

        val result = repository.loadEnrollments(1, 1, true)

        coVerify {
            networkDataSource.getUserEnrollmentsForGradingPeriod(
                courseId = 1,
                userId = 1,
                gradingPeriodId = 1,
                forceNetwork = true
            )
        }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get enrollments when device is offline`() = runTest {
        val onlineExpected = listOf(Enrollment(id = 1))
        val offlineExpected = listOf(Enrollment(id = 2))

        every { apiPrefs.user?.id } returns 1
        every { networkStateProvider.isOnline() } returns false
        coEvery {
            networkDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getUserEnrollmentsForGradingPeriod(any(), any(), any(), any())
        } returns offlineExpected

        val result = repository.loadEnrollments(1, 1, true)

        coVerify {
            localDataSource.getUserEnrollmentsForGradingPeriod(
                courseId = 1,
                userId = 1,
                gradingPeriodId = 1,
                forceNetwork = true
            )
        }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get course when device is online`() = runTest {
        val onlineExpected = Course(id = 1)
        val offlineExpected = Course(id = 2)

        every { networkStateProvider.isOnline() } returns true
        coEvery {
            networkDataSource.getCourseWithGrade(any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getCourseWithGrade(any(), any())
        } returns offlineExpected

        val result = repository.loadCourse(1, true)

        coVerify {
            networkDataSource.getCourseWithGrade(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get course when device is offline`() = runTest {
        val onlineExpected = Course(id = 1)
        val offlineExpected = Course(id = 2)

        every { networkStateProvider.isOnline() } returns false
        coEvery {
            networkDataSource.getCourseWithGrade(any(), any())
        } returns onlineExpected
        coEvery {
            localDataSource.getCourseWithGrade(any(), any())
        } returns offlineExpected

        val result = repository.loadCourse(1, true)

        coVerify {
            localDataSource.getCourseWithGrade(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Course grade calculated correctly`() = runTest {
        val course = Course(id = 1)
        val enrollments = listOf(
            Enrollment(
                id = 1,
                userId = 1,
                currentPeriodComputedCurrentGrade = "A",
                currentPeriodComputedCurrentScore = 100.0,
                currentPeriodComputedFinalGrade = "B",
                currentPeriodComputedFinalScore = 80.0
            )
        )

        val expected = CourseGrade(
            currentGrade = "A",
            currentScore = 100.0,
            finalGrade = "B",
            finalScore = 80.0,
            isLocked = false,
            noCurrentGrade = false,
            noFinalGrade = false
        )

        val result = repository.getCourseGrade(course, 1, enrollments, 1L)
        assertEquals(expected, result)
    }

    @Test
    fun `Get custom statuses when device is online`() = runTest {
        val node1 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { _id } returns "1"
            every { name } returns "Custom Status 1"
        }
        val node2 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { _id } returns "2"
            every { name } returns "Custom Status 2"
        }

        coEvery { networkDataSource.getCustomGradeStatuses(any(), any()) } returns listOf(node1)
        coEvery { localDataSource.getCustomGradeStatuses(any(), any()) } returns listOf(node2)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getCustomGradeStatuses(1, true)

        coVerify {
            networkDataSource.getCustomGradeStatuses(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals("1", result.first()._id)
    }

    @Test
    fun `Get custom statuses when device is offline`() = runTest {
        val node1 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { _id } returns "1"
            every { name } returns "Custom Status 1"
        }
        val node2 = mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
            every { _id } returns "2"
            every { name } returns "Custom Status 2"
        }

        coEvery { networkDataSource.getCustomGradeStatuses(any(), any()) } returns listOf(node1)
        coEvery { localDataSource.getCustomGradeStatuses(any(), any()) } returns listOf(node2)
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getCustomGradeStatuses(1, true)

        coVerify {
            localDataSource.getCustomGradeStatuses(
                courseId = 1,
                forceNetwork = true
            )
        }
        assertEquals("2", result.first()._id)
    }
}
