/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvasapi2

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RequestInterceptorTest {

    private val chain: Interceptor.Chain = mockk()
    private val requestSlot = slot<Request>()

    private val interceptor = RequestInterceptor()

    @Before
    fun setup() {
        mockkObject(ApiPrefs)
        mockkObject(APIHelper)
        every { ApiPrefs.getValidToken() } returns TOKEN
        every { ApiPrefs.userAgent } returns "test-agent"
        every { ApiPrefs.fullDomain } returns DOMAIN
        every { ApiPrefs.isMasquerading } returns false
        every { APIHelper.hasNetworkConnection() } returns false

        every { chain.proceed(capture(requestSlot)) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Adds bearer token when shouldIgnoreToken is false`() {
        val proceeded = intercept(RestParams(shouldIgnoreToken = false))

        assertEquals("Bearer $TOKEN", proceeded.header("Authorization"))
    }

    @Test
    fun `Does not add bearer token when shouldIgnoreToken is true`() {
        val proceeded = intercept(RestParams(shouldIgnoreToken = true))

        assertNull(proceeded.header("Authorization"))
    }

    @Test
    fun `Does not add authorization header when there is no valid token`() {
        every { ApiPrefs.getValidToken() } returns ""

        val proceeded = intercept(RestParams(shouldIgnoreToken = false))

        assertNull(proceeded.header("Authorization"))
    }

    @Test
    fun `Adds no_verifiers query param when file verifiers are disabled`() {
        val proceeded = intercept(RestParams(shouldIgnoreToken = false, disableFileVerifiers = true))

        assertEquals("1", proceeded.url.queryParameter("no_verifiers"))
    }

    @Test
    fun `Does not add no_verifiers query param when file verifiers are enabled`() {
        val proceeded = intercept(RestParams(shouldIgnoreToken = false, disableFileVerifiers = false))

        assertNull(proceeded.url.queryParameter("no_verifiers"))
    }

    private fun intercept(params: RestParams): Request {
        val request = Request.Builder()
            .url("$DOMAIN/files/1/download")
            .tag(RestParams::class.java, params)
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        return requestSlot.captured
    }

    companion object {
        private const val TOKEN = "test-token"
        private const val DOMAIN = "https://canvas.instructure.com"
    }
}
