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
package com.instructure.pandautils.features.speedgrader.grade.comments

import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpeedGraderCommentsRepositoryTest {

    private lateinit var submissionCommentsManager: SubmissionCommentsManager
    private lateinit var submissionApi: SubmissionAPI.SubmissionInterface
    private lateinit var featuresApi: FeaturesAPI.FeaturesInterface
    private lateinit var repository: SpeedGraderCommentsRepository

    @Before
    fun setup() {
        submissionCommentsManager = mockk()
        submissionApi = mockk()
        featuresApi = mockk()
        repository =
            SpeedGraderCommentsRepository(submissionCommentsManager, submissionApi, featuresApi)
    }

    @Test
    fun `getSubmissionComments returns expected data`() = runTest {
        val expected = mockk<SubmissionCommentsResponseWrapper>()

        coEvery { submissionCommentsManager.getSubmissionComments(1L, 2L, false) } returns expected

        val result = repository.getSubmissionComments(1L, 2L)
        assertEquals(expected, result)
    }

    @Test(expected = Exception::class)
    fun `throw exception if getSubmissionComments throws exception`() = runTest {
        coEvery {
            submissionCommentsManager.getSubmissionComments(
                1L,
                2L,
                true
            )
        } throws Exception("Network Error")

        repository.getSubmissionComments(1L, 2L, true)
    }

    @Test
    fun `createSubmissionComment returns expected data`() = runTest {
        val expected = mockk<CreateSubmissionCommentMutation.Data>()
        coEvery {
            submissionCommentsManager.createSubmissionComment(
                1L,
                "comment",
                null,
                false
            )
        } returns expected

        val result = repository.createSubmissionComment(1L, "comment")
        assertEquals(expected, result)
    }

    @Test(expected = Exception::class)
    fun `throw exception if createSubmissionComment throws exception`() = runTest {
        coEvery {
            submissionCommentsManager.createSubmissionComment(
                1L,
                "comment",
                null,
                false
            )
        } throws Exception("Network Error")

        repository.createSubmissionComment(1L, "comment")
    }

    @Test
    fun `getSingleSubmission returns expected submission`() = runTest {
        val expectedSubmission = mockk<Submission>()
        coEvery { submissionApi.getSingleSubmission(1L, 2L, 3L, any()) } returns DataResult.Success(
            expectedSubmission
        )

        val result = repository.getSingleSubmission(1L, 2L, 3L)
        assertEquals(expectedSubmission, result)
    }

    @Test
    fun `getSingleSubmission returns null when api returns null data`() = runTest {
        coEvery { submissionApi.getSingleSubmission(1L, 2L, 3L, any()) } returns DataResult.Fail()

        val result = repository.getSingleSubmission(1L, 2L, 3L)
        assertEquals(null, result)
    }

    @Test
    fun `getCourseFeatures returns expected features`() = runTest {
        val features = listOf("feature1", "feature2")
        coEvery { featuresApi.getEnabledFeaturesForCourse(1L, any()) } returns DataResult.Success(
            features
        )

        val result = repository.getCourseFeatures(1L)
        assertEquals(features, result)
    }

    @Test
    fun `getCourseFeatures returns empty list when api returns null data`() = runTest {
        coEvery { featuresApi.getEnabledFeaturesForCourse(1L, any()) } returns DataResult.Fail()

        val result = repository.getCourseFeatures(1L)
        assertTrue(result.isEmpty())
    }
}