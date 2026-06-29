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
 */
package com.instructure.canvasapi2.managers.graphql.horizon

import com.instructure.canvasapi2.apis.DomainServicesAuthenticationAPI
import com.instructure.canvasapi2.models.DomainService
import com.instructure.canvasapi2.models.DomainServicesWorkflow
import com.instructure.canvasapi2.models.JWTTokenResponse
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DomainServicesApiPref
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class DomainServicesAuthenticationManagerTest {

    private val api: DomainServicesAuthenticationAPI = mockk()

    private fun newPref(initial: String? = null): DomainServicesApiPref =
        object : DomainServicesApiPref("test_pref") {
            override var token: String? = initial
        }

    private fun newManager(pref: DomainServicesApiPref) = TestAuthenticationManager(api, pref)

    @Test
    fun `returns null when refresh API returns no data`() = runTest {
        coEvery {
            api.getDomainServiceAuthentication(any(), any(), any<DomainServicesWorkflow>(), any())
        } returns DataResult.Fail()

        val manager = newManager(newPref())

        assertNull(manager.getAuthenticationToken(forceRefresh = true))
    }

    @Test
    fun `returns null when refresh API returns an undecodable token`() = runTest {
        coEvery {
            api.getDomainServiceAuthentication(any(), any(), any<DomainServicesWorkflow>(), any())
        } returns DataResult.Success(JWTTokenResponse(token = "!!!not-base64!!!"))

        val manager = newManager(newPref())

        assertNull(manager.getAuthenticationToken(forceRefresh = true))
    }

    @Test
    fun `malformed cached token is treated as expired and triggers refresh`() = runTest {
        val pref = newPref(initial = "garbage-not-a-jwt")
        val freshToken = encodeAsServerWouldReturn(FAR_FUTURE_JWT)
        coEvery {
            api.getDomainServiceAuthentication(any(), any(), any<DomainServicesWorkflow>(), any())
        } returns DataResult.Success(JWTTokenResponse(token = freshToken))

        val manager = newManager(pref)

        assertEquals(FAR_FUTURE_JWT, manager.getAuthenticationToken())
        assertEquals(FAR_FUTURE_JWT, pref.token)
    }

    @Test
    fun `uses cached token when valid and not forced`() = runTest {
        val pref = newPref(initial = FAR_FUTURE_JWT)

        val manager = newManager(pref)

        assertEquals(FAR_FUTURE_JWT, manager.getAuthenticationToken())
    }

    @Test
    fun `force refresh bypasses cached token`() = runTest {
        val pref = newPref(initial = FAR_FUTURE_JWT)
        val freshToken = encodeAsServerWouldReturn(FAR_FUTURE_JWT_2)
        coEvery {
            api.getDomainServiceAuthentication(any(), any(), any<DomainServicesWorkflow>(), any())
        } returns DataResult.Success(JWTTokenResponse(token = freshToken))

        val manager = newManager(pref)

        assertEquals(FAR_FUTURE_JWT_2, manager.getAuthenticationToken(forceRefresh = true))
        assertEquals(FAR_FUTURE_JWT_2, pref.token)
    }

    private class TestAuthenticationManager(
        api: DomainServicesAuthenticationAPI,
        pref: DomainServicesApiPref,
    ) : DomainServicesAuthenticationManager(api, pref, DomainService.JOURNEY)

    companion object {
        // JWTs with `exp` far in the future (year 9999); valid base64url, decoded without crashing.
        private const val FAR_FUTURE_JWT =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjI1MzQwMjMwMDc5OSwic3ViIjoiYSJ9.sig"
        private const val FAR_FUTURE_JWT_2 =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjI1MzQwMjMwMDc5OSwic3ViIjoiYiJ9.sig"

        @OptIn(ExperimentalEncodingApi::class)
        private fun encodeAsServerWouldReturn(plaintext: String): String =
            Base64.Default.encode(plaintext.toByteArray())
    }
}
