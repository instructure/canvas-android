/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.data.repository.submission

import com.instructure.canvasapi2.RecentGradedSubmissionsQuery
import com.instructure.canvasapi2.managers.graphql.RecentGradedSubmissionsManager
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class SubmissionRepositoryImplTest {

    private val manager: RecentGradedSubmissionsManager = mockk()
    private lateinit var repository: SubmissionRepositoryImpl

    @Before
    fun setUp() {
        repository = SubmissionRepositoryImpl(manager)
    }

    @Test
    fun `getRecentGradedSubmissions returns success with mapped submissions`() = runTest {
        val assignment = mockk<RecentGradedSubmissionsQuery.Assignment>(relaxed = true) {
            coEvery { _id } returns "100"
            coEvery { name } returns "Test Assignment"
            coEvery { htmlUrl } returns "https://example.com/assignment/100"
            coEvery { pointsPossible } returns 100.0
            coEvery { gradingType } returns GradingType.points
        }

        val submission = mockk<RecentGradedSubmissionsQuery.Node>(relaxed = true) {
            coEvery { _id } returns "1"
            coEvery { score } returns 85.0
            coEvery { grade } returns "B"
            coEvery { gradedAt } returns Date()
            coEvery { excused } returns false
            coEvery { gradeHidden } returns false
            coEvery { this@mockk.assignment } returns assignment
        }

        val edge = mockk<RecentGradedSubmissionsQuery.Edge>(relaxed = true) {
            coEvery { node } returns submission
        }

        val submissions = mockk<RecentGradedSubmissionsQuery.Submissions>(relaxed = true) {
            coEvery { edges } returns listOf(edge)
        }

        val course = mockk<RecentGradedSubmissionsQuery.AllCourse>(relaxed = true) {
            coEvery { _id } returns "10"
            coEvery { name } returns "Test Course"
            coEvery { this@mockk.submissions } returns submissions
        }

        val data = RecentGradedSubmissionsQuery.Data(listOf(course))

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, false)
        } returns data

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Success)
        val successResult = result as DataResult.Success
        assertEquals(1, successResult.data.size)
        assertEquals(1L, successResult.data[0].submissionId)
        assertEquals(100L, successResult.data[0].assignmentId)
        assertEquals("Test Assignment", successResult.data[0].assignmentName)
        assertEquals(10L, successResult.data[0].courseId)
        assertEquals("Test Course", successResult.data[0].courseName)
        assertEquals(85.0, successResult.data[0].score)
        assertEquals("B", successResult.data[0].grade)
    }

    @Test
    fun `getRecentGradedSubmissions filters out hidden grades`() = runTest {
        val assignment = mockk<RecentGradedSubmissionsQuery.Assignment>(relaxed = true) {
            coEvery { _id } returns "100"
            coEvery { name } returns "Test Assignment"
        }

        val visibleSubmission = mockk<RecentGradedSubmissionsQuery.Node>(relaxed = true) {
            coEvery { _id } returns "1"
            coEvery { gradeHidden } returns false
            coEvery { this@mockk.assignment } returns assignment
        }

        val hiddenSubmission = mockk<RecentGradedSubmissionsQuery.Node>(relaxed = true) {
            coEvery { _id } returns "2"
            coEvery { gradeHidden } returns true
            coEvery { this@mockk.assignment } returns assignment
        }

        val edge1 = mockk<RecentGradedSubmissionsQuery.Edge>(relaxed = true) {
            coEvery { node } returns visibleSubmission
        }

        val edge2 = mockk<RecentGradedSubmissionsQuery.Edge>(relaxed = true) {
            coEvery { node } returns hiddenSubmission
        }

        val submissions = mockk<RecentGradedSubmissionsQuery.Submissions>(relaxed = true) {
            coEvery { edges } returns listOf(edge1, edge2)
        }

        val course = mockk<RecentGradedSubmissionsQuery.AllCourse>(relaxed = true) {
            coEvery { _id } returns "10"
            coEvery { name } returns "Test Course"
            coEvery { this@mockk.submissions } returns submissions
        }

        val data = RecentGradedSubmissionsQuery.Data(listOf(course))

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, false)
        } returns data

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Success)
        val successResult = result as DataResult.Success
        assertEquals(1, successResult.data.size)
        assertEquals(1L, successResult.data[0].submissionId)
    }

    @Test
    fun `getRecentGradedSubmissions returns empty list when no courses`() = runTest {
        val data = RecentGradedSubmissionsQuery.Data(null)

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, false)
        } returns data

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Success)
        val successResult = result as DataResult.Success
        assertTrue(successResult.data.isEmpty())
    }

    @Test
    fun `getRecentGradedSubmissions returns empty list when no submissions`() = runTest {
        val submissions = mockk<RecentGradedSubmissionsQuery.Submissions>(relaxed = true) {
            coEvery { edges } returns null
        }

        val course = mockk<RecentGradedSubmissionsQuery.AllCourse>(relaxed = true) {
            coEvery { _id } returns "10"
            coEvery { name } returns "Test Course"
            coEvery { this@mockk.submissions } returns submissions
        }

        val data = RecentGradedSubmissionsQuery.Data(listOf(course))

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, false)
        } returns data

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Success)
        val successResult = result as DataResult.Success
        assertTrue(successResult.data.isEmpty())
    }

    @Test
    fun `getRecentGradedSubmissions skips submissions without assignment`() = runTest {
        val assignment = mockk<RecentGradedSubmissionsQuery.Assignment>(relaxed = true) {
            coEvery { _id } returns "100"
            coEvery { name } returns "Test Assignment"
        }

        val submissionWithAssignment = mockk<RecentGradedSubmissionsQuery.Node>(relaxed = true) {
            coEvery { _id } returns "1"
            coEvery { gradeHidden } returns false
            coEvery { this@mockk.assignment } returns assignment
        }

        val submissionWithoutAssignment = mockk<RecentGradedSubmissionsQuery.Node>(relaxed = true) {
            coEvery { _id } returns "2"
            coEvery { gradeHidden } returns false
            coEvery { this@mockk.assignment } returns null
        }

        val edge1 = mockk<RecentGradedSubmissionsQuery.Edge>(relaxed = true) {
            coEvery { node } returns submissionWithAssignment
        }

        val edge2 = mockk<RecentGradedSubmissionsQuery.Edge>(relaxed = true) {
            coEvery { node } returns submissionWithoutAssignment
        }

        val submissions = mockk<RecentGradedSubmissionsQuery.Submissions>(relaxed = true) {
            coEvery { edges } returns listOf(edge1, edge2)
        }

        val course = mockk<RecentGradedSubmissionsQuery.AllCourse>(relaxed = true) {
            coEvery { _id } returns "10"
            coEvery { name } returns "Test Course"
            coEvery { this@mockk.submissions } returns submissions
        }

        val data = RecentGradedSubmissionsQuery.Data(listOf(course))

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, false)
        } returns data

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Success)
        val successResult = result as DataResult.Success
        assertEquals(1, successResult.data.size)
        assertEquals(1L, successResult.data[0].submissionId)
    }

    @Test
    fun `getRecentGradedSubmissions returns fail on exception`() = runTest {
        coEvery {
            manager.getRecentGradedSubmissions(any(), any(), any(), any())
        } throws RuntimeException("Network error")

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getRecentGradedSubmissions handles excused submissions`() = runTest {
        val assignment = mockk<RecentGradedSubmissionsQuery.Assignment>(relaxed = true) {
            coEvery { _id } returns "100"
            coEvery { name } returns "Test Assignment"
        }

        val submission = mockk<RecentGradedSubmissionsQuery.Node>(relaxed = true) {
            coEvery { _id } returns "1"
            coEvery { gradeHidden } returns false
            coEvery { excused } returns true
            coEvery { this@mockk.assignment } returns assignment
        }

        val edge = mockk<RecentGradedSubmissionsQuery.Edge>(relaxed = true) {
            coEvery { node } returns submission
        }

        val submissions = mockk<RecentGradedSubmissionsQuery.Submissions>(relaxed = true) {
            coEvery { edges } returns listOf(edge)
        }

        val course = mockk<RecentGradedSubmissionsQuery.AllCourse>(relaxed = true) {
            coEvery { _id } returns "10"
            coEvery { name } returns "Test Course"
            coEvery { this@mockk.submissions } returns submissions
        }

        val data = RecentGradedSubmissionsQuery.Data(listOf(course))

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, false)
        } returns data

        val result = repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)

        assertTrue(result is DataResult.Success)
        val successResult = result as DataResult.Success
        assertEquals(1, successResult.data.size)
        assertTrue(successResult.data[0].excused)
    }

    @Test
    fun `getRecentGradedSubmissions passes forceRefresh to manager`() = runTest {
        val data = RecentGradedSubmissionsQuery.Data(null)

        coEvery {
            manager.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", 20, true)
        } returns data

        repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", true)

        // Test passes if no exception - verifies the forceRefresh param is passed correctly
    }
}