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

package com.instructure.pandautils.domain.usecase.audit

import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.model.GradedSubmission
import com.instructure.pandautils.data.repository.submission.SubmissionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@ExperimentalCoroutinesApi
class LoadRecentGradeChangesUseCaseTest {

    private val repository: SubmissionRepository = mockk()
    private lateinit var useCase: LoadRecentGradeChangesUseCase

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    @Before
    fun setUp() {
        useCase = LoadRecentGradeChangesUseCase(repository)
    }

    private fun createGradedSubmission(
        id: Long,
        gradedAt: Date?
    ): GradedSubmission {
        return GradedSubmission(
            submissionId = id,
            assignmentId = id * 10,
            assignmentName = "Assignment $id",
            courseId = 1L,
            courseName = "Course",
            score = 90.0,
            grade = "A",
            gradedAt = gradedAt,
            excused = false,
            assignmentUrl = "https://example.com/assignment/$id",
            pointsPossible = 100.0,
            gradingType = GradingType.points
        )
    }

    @Test
    fun `execute returns empty list when startTime is null`() = runTest {
        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = null
            )
        )

        assertEquals(emptyList<GradedSubmission>(), result)
    }

    @Test
    fun `execute returns sorted submissions by gradedAt descending`() = runTest {
        val date1 = dateFormat.parse("2025-01-01T10:00:00Z")!!
        val date2 = dateFormat.parse("2025-01-03T10:00:00Z")!!
        val date3 = dateFormat.parse("2025-01-02T10:00:00Z")!!

        val submission1 = createGradedSubmission(1, date1)
        val submission2 = createGradedSubmission(2, date2)
        val submission3 = createGradedSubmission(3, date3)

        val submissions = listOf(submission1, submission2, submission3)
        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)
        } returns DataResult.Success(submissions)

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z"
            )
        )

        assertEquals(3, result.size)
        assertEquals(2L, result[0].submissionId)
        assertEquals(3L, result[1].submissionId)
        assertEquals(1L, result[2].submissionId)
    }

    @Test
    fun `execute filters submissions by endTime`() = runTest {
        val date1 = dateFormat.parse("2025-01-01T10:00:00Z")!!
        val date2 = dateFormat.parse("2025-01-03T10:00:00Z")!!
        val date3 = dateFormat.parse("2025-01-05T10:00:00Z")!!

        val submission1 = createGradedSubmission(1, date1)
        val submission2 = createGradedSubmission(2, date2)
        val submission3 = createGradedSubmission(3, date3)

        val submissions = listOf(submission1, submission2, submission3)
        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)
        } returns DataResult.Success(submissions)

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z",
                endTime = "2025-01-04T00:00:00Z"
            )
        )

        assertEquals(2, result.size)
        assertEquals(2L, result[0].submissionId)
        assertEquals(1L, result[1].submissionId)
    }

    @Test
    fun `execute includes submissions with null gradedAt when filtering by endTime`() = runTest {
        val date1 = dateFormat.parse("2025-01-01T10:00:00Z")!!
        val submission1 = createGradedSubmission(1, date1)
        val submission2 = createGradedSubmission(2, null)

        val submissions = listOf(submission1, submission2)
        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)
        } returns DataResult.Success(submissions)

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z",
                endTime = "2025-01-04T00:00:00Z"
            )
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `execute forces refresh when forceRefresh is true`() = runTest {
        val date = dateFormat.parse("2025-01-01T10:00:00Z")!!
        val submission = createGradedSubmission(1, date)

        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", true)
        } returns DataResult.Success(listOf(submission))

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z",
                forceRefresh = true
            )
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `execute returns empty list when no submissions in date range`() = runTest {
        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)
        } returns DataResult.Success(emptyList())

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z"
            )
        )

        assertEquals(emptyList<GradedSubmission>(), result)
    }

    @Test
    fun `execute handles submissions with same gradedAt timestamp`() = runTest {
        val date = dateFormat.parse("2025-01-01T10:00:00Z")!!

        val submission1 = createGradedSubmission(1, date)
        val submission2 = createGradedSubmission(2, date)
        val submission3 = createGradedSubmission(3, date)

        val submissions = listOf(submission1, submission2, submission3)
        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)
        } returns DataResult.Success(submissions)

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z"
            )
        )

        assertEquals(3, result.size)
        assertTrue(result.all { it.gradedAt == date })
    }

    @Test
    fun `execute filters out submissions after exact endTime`() = runTest {
        val endTime = dateFormat.parse("2025-01-04T00:00:00Z")!!
        val beforeEndTime = dateFormat.parse("2025-01-03T23:59:59Z")!!
        val afterEndTime = dateFormat.parse("2025-01-04T00:00:01Z")!!

        val submission1 = createGradedSubmission(1, beforeEndTime)
        val submission2 = createGradedSubmission(2, endTime)
        val submission3 = createGradedSubmission(3, afterEndTime)

        val submissions = listOf(submission1, submission2, submission3)
        coEvery {
            repository.getRecentGradedSubmissions(123L, "2025-01-01T00:00:00Z", false)
        } returns DataResult.Success(submissions)

        val result = useCase(
            LoadRecentGradeChangesParams(
                studentId = 123L,
                startTime = "2025-01-01T00:00:00Z",
                endTime = "2025-01-04T00:00:00Z"
            )
        )

        assertEquals(2, result.size)
        assertEquals(2L, result[0].submissionId)
        assertEquals(1L, result[1].submissionId)
    }
}