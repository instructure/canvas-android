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
package com.instructure.pandautils.data.repository.user

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.CookieConsentBody
import com.instructure.canvasapi2.models.CookieConsentContent
import com.instructure.canvasapi2.models.CookieConsentResponse
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRepositoryImplTest {

    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)
    private val repository = UserRepositoryImpl(userApi)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getCookieConsentData calls api with correct namespace and returns result`() = runTest {
        val namespace = "MOBILE_CANVAS_STUDENT_COOKIE_CONSENT"
        val expected = DataResult.Success(CookieConsentResponse(CookieConsentContent(true)))
        coEvery { userApi.getCookieConsentData(namespace, any()) } returns expected

        val result = repository.getCookieConsentData(namespace)

        assertEquals(expected, result)
        coVerify { userApi.getCookieConsentData(namespace, any()) }
    }

    @Test
    fun `getCookieConsentData returns fail result when api fails`() = runTest {
        val namespace = "MOBILE_CANVAS_TEACHER_COOKIE_CONSENT"
        val expected = DataResult.Fail()
        coEvery { userApi.getCookieConsentData(namespace, any()) } returns expected

        val result = repository.getCookieConsentData(namespace)

        assertEquals(expected, result)
    }

    @Test
    fun `putCookieConsentData calls api with correct namespace and consent body`() = runTest {
        val namespace = "MOBILE_CANVAS_PARENT_COOKIE_CONSENT"
        val consent = true
        val expected = DataResult.Success(CookieConsentResponse(CookieConsentContent(consent)))
        val bodySlot = slot<CookieConsentBody>()
        coEvery { userApi.putCookieConsentData(namespace, capture(bodySlot), any()) } returns expected

        val result = repository.putCookieConsentData(namespace, consent)

        assertEquals(expected, result)
        assertEquals(consent, bodySlot.captured.data.mobileConsent)
        coVerify { userApi.putCookieConsentData(namespace, any(), any()) }
    }

    @Test
    fun `putCookieConsentData sends false consent correctly`() = runTest {
        val namespace = "MOBILE_CANVAS_STUDENT_COOKIE_CONSENT"
        val consent = false
        val expected = DataResult.Success(CookieConsentResponse(CookieConsentContent(consent)))
        val bodySlot = slot<CookieConsentBody>()
        coEvery { userApi.putCookieConsentData(namespace, capture(bodySlot), any()) } returns expected

        val result = repository.putCookieConsentData(namespace, consent)

        assertEquals(expected, result)
        assertEquals(false, bodySlot.captured.data.mobileConsent)
    }

    @Test
    fun `putCookieConsentData returns fail result when api fails`() = runTest {
        val namespace = "MOBILE_CANVAS_STUDENT_COOKIE_CONSENT"
        val expected = DataResult.Fail()
        coEvery { userApi.putCookieConsentData(any(), any(), any()) } returns expected

        val result = repository.putCookieConsentData(namespace, true)

        assertEquals(expected, result)
    }
}

