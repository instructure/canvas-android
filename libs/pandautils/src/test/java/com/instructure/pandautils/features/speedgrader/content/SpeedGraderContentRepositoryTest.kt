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
package com.instructure.pandautils.features.speedgrader.content

import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SpeedGraderContentRepositoryTest {

    private lateinit var submissionContentManager: SubmissionContentManager
    private lateinit var submissionApi: SubmissionAPI.SubmissionInterface
    private lateinit var repository: SpeedGraderContentRepository

    @Before
    fun setup() {
        submissionContentManager = mockk()
        submissionApi = mockk()
        repository = SpeedGraderContentRepository(submissionContentManager, submissionApi)
    }

    @Test
    fun `getSubmission calls submissionContentManager and returns data`() = runTest {
        val assignmentId = 123L
        val studentId = 456L
        val expectedData = mockk<SubmissionContentQuery.Data>()

        coEvery { submissionContentManager.getSubmissionContent(studentId, assignmentId) } returns expectedData

        val result = repository.getSubmission(assignmentId, studentId)

        Assert.assertEquals(expectedData, result)
    }

    @Test
    fun `getSingleSubmission calls submissionApi and returns data`() = runTest {
        val courseId = 789L
        val assignmentId = 123L
        val studentId = 456L
        val expectedSubmission = mockk<Submission>()

        coEvery {
            submissionApi.getSingleSubmission(
                courseId,
                assignmentId,
                studentId,
                any<RestParams>()
            )
        } returns DataResult.Success(expectedSubmission)

        val result = repository.getSingleSubmission(courseId, assignmentId, studentId)

        assertEquals(expectedSubmission, result)
    }

    @Test
    fun `getSingleSubmission returns null when api returns null data`() = runTest {
        val courseId = 789L
        val assignmentId = 123L
        val studentId = 456L

        coEvery {
            submissionApi.getSingleSubmission(
                courseId,
                assignmentId,
                studentId,
                any<RestParams>()
            )
        } returns DataResult.Fail()

        val result = repository.getSingleSubmission(courseId, assignmentId, studentId)

        Assert.assertEquals(null, result)
    }
}