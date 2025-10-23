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
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCommentsManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AssignmentDetailsRepositoryTest {
    private val assignmentApi: AssignmentAPI.AssignmentInterface = mockk(relaxed = true)
    private val oAuthInterface: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val horizonGetCommentsManager: HorizonGetCommentsManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val userId = 1L
    private val courseId = 1L
    private val assignmentId = 1L

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = userId, name = "Test User")
    }

    @Test
    fun `Test successful assignment retrieval`() = runTest {
        val assignment = Assignment(id = assignmentId, name = "Test Assignment", pointsPossible = 100.0)
        coEvery { assignmentApi.getAssignmentWithHistory(courseId, assignmentId, any()) } returns
            DataResult.Success(assignment)

        val result = getRepository().getAssignment(assignmentId, courseId, false)

        assertEquals(assignment, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed assignment retrieval throws exception`() = runTest {
        coEvery { assignmentApi.getAssignmentWithHistory(courseId, assignmentId, any()) } returns
            DataResult.Fail()

        getRepository().getAssignment(assignmentId, courseId, false)
    }

    @Test
    fun `Test successful URL authentication`() = runTest {
        val originalUrl = "https://example.com/file"
        val authenticatedUrl = "https://example.com/file?session=xyz"
        val session = AuthenticatedSession(sessionUrl = authenticatedUrl)

        coEvery { oAuthInterface.getAuthenticatedSession(originalUrl, any()) } returns
            DataResult.Success(session)

        val result = getRepository().authenticateUrl(originalUrl)

        assertEquals(authenticatedUrl, result)
    }

    @Test
    fun `Test URL authentication fallback on failure`() = runTest {
        val originalUrl = "https://example.com/file"
        coEvery { oAuthInterface.getAuthenticatedSession(originalUrl, any()) } returns DataResult.Fail()

        val result = getRepository().authenticateUrl(originalUrl)

        assertEquals(originalUrl, result)
    }

    @Test
    fun `Test URL authentication fallback on null session`() = runTest {
        val originalUrl = "https://example.com/file"
        coEvery { oAuthInterface.getAuthenticatedSession(originalUrl, any()) } returns DataResult.Fail()

        val result = getRepository().authenticateUrl(originalUrl)

        assertEquals(originalUrl, result)
    }

    @Test
    fun `Test has unread comments returns true when count greater than zero`() = runTest {
        coEvery { horizonGetCommentsManager.getUnreadCommentsCount(assignmentId, userId, false) } returns 3

        val result = getRepository().hasUnreadComments(assignmentId, false)

        assertTrue(result)
    }

    @Test
    fun `Test has unread comments returns false when count is zero`() = runTest {
        coEvery { horizonGetCommentsManager.getUnreadCommentsCount(assignmentId, userId, false) } returns 0

        val result = getRepository().hasUnreadComments(assignmentId, false)

        assertFalse(result)
    }

    @Test
    fun `Test force network parameter is passed correctly`() = runTest {
        val assignment = Assignment(id = assignmentId, name = "Test Assignment")
        coEvery { assignmentApi.getAssignmentWithHistory(courseId, assignmentId, any()) } returns
            DataResult.Success(assignment)

        getRepository().getAssignment(assignmentId, courseId, true)

        coEvery { assignmentApi.getAssignmentWithHistory(courseId, assignmentId, match { it.isForceReadFromNetwork }) }
    }

    private fun getRepository(): AssignmentDetailsRepository {
        return AssignmentDetailsRepository(assignmentApi, oAuthInterface, horizonGetCommentsManager, apiPrefs)
    }
}
