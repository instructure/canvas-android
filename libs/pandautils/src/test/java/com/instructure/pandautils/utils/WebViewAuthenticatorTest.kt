/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.utils

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WebViewAuthenticatorTest {

    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val webViewAuthenticator = WebViewAuthenticator(oAuthApi, apiPrefs)

    @Test
    fun `Authenticate webviews when timestamp is older than an hour`() = runTest {
        every { apiPrefs.webViewAuthenticationTimestamp } returns System.currentTimeMillis() - 1000 * 60 * 61
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        webViewAuthenticator.authenticateWebViews(this, mockk())
        this.testScheduler.advanceUntilIdle()

        coVerify { oAuthApi.getAuthenticatedSession(any(), any()) }
    }

    @Test
    fun `Do not authenticate webviews when timestamp is not older than an hour`() = runTest {
        every { apiPrefs.webViewAuthenticationTimestamp } returns System.currentTimeMillis()
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        webViewAuthenticator.authenticateWebViews(this, mockk())

        coVerify(exactly = 0) { oAuthApi.getAuthenticatedSession(any(), any()) }
    }
}