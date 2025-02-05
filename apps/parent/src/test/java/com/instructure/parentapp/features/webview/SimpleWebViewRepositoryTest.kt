/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.parentapp.features.webview

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class SimpleWebViewRepositoryTest {

    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val repository = SimpleWebViewRepository(oAuthApi, apiPrefs)

    @Test
    fun `Get authenticated session successfully returns data`() = runTest {
        val expected = "sessionUrl"

        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Success(AuthenticatedSession(sessionUrl = expected))

        val result = repository.getAuthenticatedSession("url")
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get authenticated session fails throws exception`() = runTest {
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        repository.getAuthenticatedSession("url")
    }

    @Test
    fun `Get authenticated session while masquerading successfully returns data`() = runTest {
        val expected = "sessionUrl"

        coEvery { apiPrefs.isMasquerading } returns true
        coEvery {
            oAuthApi.getAuthenticatedSessionMasquerading(any(), any(), any())
        } returns DataResult.Success(AuthenticatedSession(sessionUrl = expected))

        val result = repository.getAuthenticatedSession("url")
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get authenticated session while masquerading fails throws exception`() = runTest {
        coEvery { apiPrefs.isMasquerading } returns true
        coEvery { oAuthApi.getAuthenticatedSessionMasquerading(any(), any(), any()) } returns DataResult.Fail()

        repository.getAuthenticatedSession("url")
    }
}
