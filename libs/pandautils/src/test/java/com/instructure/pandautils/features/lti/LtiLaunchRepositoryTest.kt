/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.lti

import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LtiLaunchRepositoryTest {

    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface = mockk(relaxed = true)
    private val assignmentApi = mockk<AssignmentAPI.AssignmentInterface>()
    private val oAuthInterface = mockk<OAuthAPI.OAuthInterface>()

    private val repository = LtiLaunchRepository(launchDefinitionsApi, assignmentApi, oAuthInterface)

    @Before
    fun setup() {
        val urlCaptor = slot<String>()
        coEvery { oAuthInterface.getAuthenticatedSession(capture(urlCaptor), any()) } answers {
            DataResult.Success(AuthenticatedSession(sessionUrl = urlCaptor.captured))
        }
    }

    @Test
    fun `Get lti from authentication url throws exception when fails`() = runTest {
        val url = "https://www.instructure.com"
        val result = runCatching { repository.getLtiFromAuthenticationUrl(url, null) }
        assert(result.isFailure)
    }

    @Test
    fun `Get lti from authentication url returns data when successful`() = runTest {
        val url = "https://www.instructure.com"
        val expected = LTITool()
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(url, any()) } returns DataResult.Success(expected)

        val result = repository.getLtiFromAuthenticationUrl(url, null)

        assertEquals(expected, result)
    }

    @Test
    fun `Get lti from authentication url returns lti from assignments api when lti tool is present`() = runTest {
        val url = "https://www.instructure.com"
        val ltiTool = LTITool(courseId = 1, id = 2, assignmentId = 3)
        val expected = LTITool()
        coEvery { assignmentApi.getExternalToolLaunchUrl(ltiTool.courseId, ltiTool.id, ltiTool.assignmentId, any(), any()) } returns DataResult.Success(expected)

        val result = repository.getLtiFromAuthenticationUrl(url, ltiTool)

        coVerify { assignmentApi.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get lti from authentication url returns lti from launchdefinitions api when lti tool is present but assignment api fails`() = runTest {
        val url = "https://www.instructure.com"
        val ltiTool = LTITool(courseId = 1, id = 2, assignmentId = 3)
        val expected = LTITool()
        coEvery { assignmentApi.getExternalToolLaunchUrl(ltiTool.courseId, ltiTool.id, ltiTool.assignmentId, any(), any()) } returns DataResult.Fail()
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(url, any()) } returns DataResult.Success(expected)

        val result = repository.getLtiFromAuthenticationUrl(url, ltiTool)

        coVerify { assignmentApi.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) }
        coVerify { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get lti from authentication url throws exception when lti tool is present and both request fails`() = runTest {
        val url = "https://www.instructure.com"
        val ltiTool = LTITool(courseId = 1, id = 2, assignmentId = 3)
        coEvery { assignmentApi.getExternalToolLaunchUrl(ltiTool.courseId, ltiTool.id, ltiTool.assignmentId, any(), any()) } returns DataResult.Fail()
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(url, any()) } returns DataResult.Fail()

        val result = runCatching { repository.getLtiFromAuthenticationUrl(url, ltiTool) }
        assert(result.isFailure)

        coVerify { assignmentApi.getExternalToolLaunchUrl(any(), any(), any(), any(), any()) }
        coVerify { launchDefinitionsApi.getLtiFromAuthenticationUrl(any(), any()) }
    }

    @Test
    fun `Get authenticated url returns data when successful`() = runTest {
        val urlCaptor = slot<String>()
        coEvery { oAuthInterface.getAuthenticatedSession(capture(urlCaptor), any()) } answers {
            DataResult.Success(AuthenticatedSession(sessionUrl = urlCaptor.captured + "/authenticated"))
        }

        val url = "https://www.instructure.com"

        val result = repository.authenticateUrl(url)

        assertEquals("$url/authenticated", result)
    }

    @Test
    fun `Get authenticated url returns original url if request fails`() = runTest {
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        val url = "https://www.instructure.com"

        val result = repository.authenticateUrl(url)

        assertEquals(url, result)
    }

    @Test
    fun `Get lti from authentication url uses contextId when courseId is 0`() = runTest {
        val url = "https://www.instructure.com"
        val ltiTool = LTITool(courseId = 0, contextId = 123, id = 2, assignmentId = 3)
        val expected = LTITool()
        coEvery { assignmentApi.getExternalToolLaunchUrl(123, ltiTool.id, ltiTool.assignmentId, any(), any()) } returns DataResult.Success(expected)

        val result = repository.getLtiFromAuthenticationUrl(url, ltiTool)

        coVerify { assignmentApi.getExternalToolLaunchUrl(123, ltiTool.id, ltiTool.assignmentId, any(), any()) }
        assertEquals(expected, result)
    }
}