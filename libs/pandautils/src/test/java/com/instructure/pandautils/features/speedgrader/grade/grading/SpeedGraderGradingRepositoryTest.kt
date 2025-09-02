/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader.grade.grading

import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SpeedGraderGradingRepositoryTest {

    private val submissionGradeManager: SubmissionGradeManager = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)

    private lateinit var repository: SpeedGraderGradingRepository

    @Before
    fun setUp() {
        repository = SpeedGraderGradingRepository(submissionGradeManager, submissionApi)
    }

    @Test
    fun `getSubmissionGrade calls manager`() = runTest {
        val expected = SubmissionGradeQuery.Data(submission = null)
        coEvery { submissionGradeManager.getSubmissionGrade(1L, 2L, true) } returns expected

        val result = repository.getSubmissionGrade(1L, 2L, true)

        assertEquals(expected, result)
        coVerify { submissionGradeManager.getSubmissionGrade(1L, 2L, true) }
    }

    @Test
    fun `updateSubmissionGrade calls API and returns data`() = runTest {
        val expectedSubmission = Submission(id = 123L)
        coEvery {
            submissionApi.postSubmissionGrade(
                contextId = any(),
                assignmentId = 2L,
                userId = 3L,
                assignmentScore = "100",
                isExcused = false,
                restParams = any()
            )
        } returns DataResult.Success(expectedSubmission)

        val result = repository.updateSubmissionGrade("100", 3L, 2L, 1L, false)

        assertEquals(expectedSubmission, result)
        coVerify {
            submissionApi.postSubmissionGrade(
                contextId = any(),
                assignmentId = 2L,
                userId = 3L,
                assignmentScore = "100",
                isExcused = false,
                restParams = any()
            )
        }
    }

    @Test
    fun `excuseSubmission calls API and returns data`() = runTest {
        val expectedSubmission = Submission(id = 456L, excused = true)
        coEvery {
            submissionApi.postSubmissionExcusedStatus(
                contextId = any(),
                assignmentId = 2L,
                userId = 3L,
                isExcused = true,
                restParams = any()
            )
        } returns DataResult.Success(expectedSubmission)

        val result = repository.excuseSubmission(3L, 2L, 1L)

        assertEquals(expectedSubmission, result)
        coVerify {
            submissionApi.postSubmissionExcusedStatus(
                contextId = any(),
                assignmentId = 2L,
                userId = 3L,
                isExcused = true,
                restParams = any()
            )
        }
    }

    @Test
    fun `updateSubmissionStatus calls manager with null customStatusId`() = runTest {
        val expected = mockk<UpdateSubmissionStatusMutation.Data>()
        coEvery { submissionGradeManager.updateSubmissionStatus(1L, null, "late") } returns expected

        val result = repository.updateSubmissionStatus(1L, latePolicyStatus = "late")

        assertEquals(expected, result)
        coVerify { submissionGradeManager.updateSubmissionStatus(1L, null, "late") }
    }

    @Test
    fun `updateSubmissionStatus calls manager with null latePolicyStatus`() = runTest {
        val expected = mockk<UpdateSubmissionStatusMutation.Data>()
        coEvery { submissionGradeManager.updateSubmissionStatus(1L, "customId", null) } returns expected

        val result = repository.updateSubmissionStatus(1L, customStatusId = "customId")

        assertEquals(expected, result)
        coVerify { submissionGradeManager.updateSubmissionStatus(1L, "customId", null) }
    }

    @Test
    fun `updateSubmissionStatus calls manager with all null optionals`() = runTest {
        val expected = mockk<UpdateSubmissionStatusMutation.Data>()
        coEvery { submissionGradeManager.updateSubmissionStatus(1L, null, null) } returns expected

        val result = repository.updateSubmissionStatus(1L)

        assertEquals(expected, result)
        coVerify { submissionGradeManager.updateSubmissionStatus(1L, null, null) }
    }

    @Test
    fun `postSubmissionLateSecondsOverride calls API and returns data`() = runTest {
        val expectedSubmission = Submission(id = 789L)
        coEvery {
            submissionApi.postSubmissionLateSecondsOverride(
                contextId = 1L,
                assignmentId = 2L,
                userId = 3L,
                lateSeconds = 86400,
                restParams = any()
            )
        } returns DataResult.Success(expectedSubmission)

        val result = repository.updateLateSecondsOverride(3L, 2L, 1L, 86400)

        assertEquals(expectedSubmission, result)
        coVerify {
            submissionApi.postSubmissionLateSecondsOverride(
                contextId = 1L,
                assignmentId = 2L,
                userId = 3L,
                lateSeconds = 86400,
                restParams = any()
            )
        }
    }
}