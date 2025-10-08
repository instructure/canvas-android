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
package com.instructure.horizon.features.moduleitemsequence.content.assessment

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class AssessmentRepositoryTest {
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val oAuthInterface: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface = mockk(relaxed = true)

    private lateinit var repository: AssessmentRepository

    private val testAssignment = Assignment(
        id = 1L,
        name = "Test Quiz",
        courseId = 100L,
        url = "https://example.com/quiz/1"
    )

    private val testLTITool = LTITool(
        url = "https://lti.example.com/tool",
        id = 1
    )

    @Before
    fun setup() {
        repository = AssessmentRepository(assignmentApi, oAuthInterface, launchDefinitionsApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAssignment returns assignment successfully`() = runTest {
        coEvery { assignmentApi.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Success(testAssignment)

        val result = repository.getAssignment(assignmentId = 1L, courseId = 100L, forceNetwork = false)

        assertEquals("Test Quiz", result.name)
        assertEquals(1L, result.id)
        coVerify { assignmentApi.getAssignmentWithHistory(100L, 1L, any()) }
    }

    @Test
    fun `getAssignment with forceNetwork true`() = runTest {
        coEvery { assignmentApi.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Success(testAssignment)

        repository.getAssignment(assignmentId = 1L, courseId = 100L, forceNetwork = true)

        coVerify { assignmentApi.getAssignmentWithHistory(any(), any(), match { it.isForceReadFromNetwork }) }
    }

    @Test
    fun `getAssignment with forceNetwork false`() = runTest {
        coEvery { assignmentApi.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Success(testAssignment)

        repository.getAssignment(assignmentId = 1L, courseId = 100L, forceNetwork = false)

        coVerify { assignmentApi.getAssignmentWithHistory(any(), any(), match { !it.isForceReadFromNetwork }) }
    }

    @Test
    fun `authenticateUrl returns authenticated URL for LTI tool`() = runTest {
        val session = AuthenticatedSession(sessionUrl = "https://authenticated.lti.url")
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(testLTITool)
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Success(session)

        val result = repository.authenticateUrl("https://example.com/quiz")

        assertEquals("https://authenticated.lti.url", result)
        coVerify { launchDefinitionsApi.getLtiFromAuthenticationUrl("https://example.com/quiz", any()) }
        coVerify { oAuthInterface.getAuthenticatedSession("https://lti.example.com/tool", any()) }
    }

    @Test
    fun `authenticateUrl returns original URL when LTI tool URL is null`() = runTest {
        val ltiToolWithoutUrl = LTITool(url = null, id = 1)
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(ltiToolWithoutUrl)

        val result = repository.authenticateUrl("https://example.com/quiz")

        assertEquals("https://example.com/quiz", result)
    }

    @Test
    fun `authenticateUrl returns original URL when session URL is null`() = runTest {
        val session = AuthenticatedSession(sessionUrl = "https://example.com/quiz/authenticated")
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(testLTITool)
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Success(session)

        val result = repository.authenticateUrl("https://example.com/quiz")

        assertEquals("https://example.com/quiz/authenticated", result)
    }

    @Test
    fun `authenticateUrl returns original URL when authentication fails`() = runTest {
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(testLTITool)
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        val result = repository.authenticateUrl("https://example.com/quiz")

        assertEquals("https://example.com/quiz", result)
    }

    @Test
    fun `authenticateUrl always uses forceNetwork`() = runTest {
        val session = AuthenticatedSession(sessionUrl = "https://authenticated.url")
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) } returns DataResult.Success(testLTITool)
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Success(session)

        repository.authenticateUrl("https://example.com")

        coVerify { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), match { it.isForceReadFromNetwork }) }
        coVerify { oAuthInterface.getAuthenticatedSession(any(), match { it.isForceReadFromNetwork }) }
    }

    @Test
    fun `getAssignment with different course and assignment IDs`() = runTest {
        coEvery { assignmentApi.getAssignmentWithHistory(any(), any(), any()) } returns DataResult.Success(testAssignment)

        repository.getAssignment(assignmentId = 99L, courseId = 200L, forceNetwork = false)

        coVerify { assignmentApi.getAssignmentWithHistory(200L, 99L, any()) }
    }
}
