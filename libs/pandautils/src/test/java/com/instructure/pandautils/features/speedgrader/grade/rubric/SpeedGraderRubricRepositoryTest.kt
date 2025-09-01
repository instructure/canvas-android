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
package com.instructure.pandautils.features.speedgrader.grade.rubric

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloGraphQLException
import com.instructure.canvasapi2.SubmissionRubricQuery
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SpeedGraderRubricRepositoryTest {

    private val submissionRubricManager: SubmissionRubricManager = mockk(relaxed = true)
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val submissionApi: SubmissionAPI.SubmissionInterface = mockk(relaxed = true)

    private lateinit var repository: SpeedGraderRubricRepository

    @Before
    fun setup() {
        repository = SpeedGraderRubricRepository(submissionRubricManager, assignmentApi, submissionApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `get rubrics calls submissionRubricManager`() = runTest {
        val assignmentId = 123L
        val userId = 456L

        val expected = mockk<SubmissionRubricQuery.Data>(relaxed = true)
        coEvery { submissionRubricManager.getRubrics(assignmentId, userId) } returns expected

        val result = repository.getRubrics(assignmentId, userId)

        assertEquals(expected, result)
    }

    @Test(expected = ApolloGraphQLException::class)
    fun `throw exception if submissionRubricManager throws exception`() = runTest {
        val assignmentId = 123L
        val userId = 456L

        coEvery { submissionRubricManager.getRubrics(assignmentId, userId) } throws ApolloGraphQLException(Error.Builder("Test error").build())

        repository.getRubrics(assignmentId, userId)
    }

    @Test
    fun `get assignment rubric calls assignmentApi`() = runTest {
        val courseId = 789L
        val assignmentId = 123L

        val expected = mockk<Assignment>(relaxed = true)
        coEvery { assignmentApi.getAssignment(courseId, assignmentId, any()) } returns DataResult.Success(expected)

        val result = repository.getAssignmentRubric(courseId, assignmentId)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `throw exception if assignmentApi returns fail`() = runTest {
        val courseId = 789L
        val assignmentId = 123L

        coEvery { assignmentApi.getAssignment(courseId, assignmentId, any()) } returns DataResult.Fail()

        repository.getAssignmentRubric(courseId, assignmentId)
    }

    @Test
    fun `post submission rubric assessment calls submissionApi`() = runTest {
        val courseId = 789L
        val assignmentId = 123L
        val userId = 456L
        val rubricAssessmentMap = mapOf("criterion1" to "rating1")

        val expected = mockk<Submission>(relaxed = true)
        coEvery { submissionApi.postSubmissionRubricAssessmentMap(courseId, assignmentId, userId, rubricAssessmentMap, any()) } returns DataResult.Success(expected)

        val result = repository.postSubmissionRubricAssessment(courseId, assignmentId, userId, rubricAssessmentMap)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `throw exception if submissionApi returns fail`() = runTest {
        val courseId = 789L
        val assignmentId = 123L
        val userId = 456L
        val rubricAssessmentMap = mapOf("criterion1" to "rating1")

        coEvery { submissionApi.postSubmissionRubricAssessmentMap(courseId, assignmentId, userId, rubricAssessmentMap, any()) } returns DataResult.Fail()

        repository.postSubmissionRubricAssessment(courseId, assignmentId, userId, rubricAssessmentMap)
    }

}