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

package com.instructure.pandautils.domain.usecase.session

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAuthenticatedSessionUseCaseTest {

    private val oauthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var useCase: GetAuthenticatedSessionUseCase

    @Before
    fun setUp() {
        useCase = GetAuthenticatedSessionUseCase(oauthApi, apiPrefs)
        every { apiPrefs.fullDomain } returns "https://canvas.instructure.com"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute returns authenticated session URL when URL starts with fullDomain`() = runTest {
        val targetUrl = "https://canvas.instructure.com/courses/1/conferences/1/join"
        val authenticatedUrl = "https://canvas.instructure.com/session/abc123?return_to=/courses/1/conferences/1/join"
        val authenticatedSession = AuthenticatedSession(sessionUrl = authenticatedUrl)

        coEvery { oauthApi.getAuthenticatedSession(targetUrl, any()) } returns DataResult.Success(authenticatedSession)

        val result = useCase(GetAuthenticatedSessionUseCase.Params(targetUrl))

        assertEquals(authenticatedUrl, result)
        coVerify { oauthApi.getAuthenticatedSession(targetUrl, any()) }
    }

    @Test
    fun `execute returns original URL when URL does not start with fullDomain`() = runTest {
        val targetUrl = "https://external.conference.com/join/123"

        val result = useCase(GetAuthenticatedSessionUseCase.Params(targetUrl))

        assertEquals(targetUrl, result)
        coVerify(exactly = 0) { oauthApi.getAuthenticatedSession(any(), any()) }
    }

    @Test
    fun `execute returns original URL when authentication fails`() = runTest {
        val targetUrl = "https://canvas.instructure.com/courses/1/conferences/1/join"

        coEvery { oauthApi.getAuthenticatedSession(targetUrl, any()) } returns DataResult.Fail()

        val result = useCase(GetAuthenticatedSessionUseCase.Params(targetUrl))

        assertEquals(targetUrl, result)
    }

    @Test
    fun `execute handles different domain prefixes correctly`() = runTest {
        every { apiPrefs.fullDomain } returns "https://school.instructure.com"

        val targetUrl = "https://canvas.instructure.com/courses/1/conferences/1/join"

        val result = useCase(GetAuthenticatedSessionUseCase.Params(targetUrl))

        assertEquals(targetUrl, result)
        coVerify(exactly = 0) { oauthApi.getAuthenticatedSession(any(), any()) }
    }
}