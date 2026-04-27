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
package com.instructure.pandautils.features.cookieconsent

import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ConsentPrefs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SetCookieConsentUseCaseTest {

    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val consentPrefs: ConsentPrefs = mockk(relaxed = true)

    private val useCase = SetCookieConsentUseCase(apiPrefs, consentPrefs)

    @Before
    fun setup() {
        every { consentPrefs.setConsent(any(), any(), any()) } just Runs
        val user = mockk<User> { every { id } returns 42L }
        every { apiPrefs.user } returns user
        every { apiPrefs.domain } returns "test.instructure.com"
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `stores true consent in ConsentPrefs`() = runTest {
        useCase(SetCookieConsentUseCase.Params(true))

        verify { consentPrefs.setConsent(42L, "test.instructure.com", true) }
    }

    @Test
    fun `stores false consent in ConsentPrefs`() = runTest {
        useCase(SetCookieConsentUseCase.Params(false))

        verify { consentPrefs.setConsent(42L, "test.instructure.com", false) }
    }

    @Test
    fun `does nothing when user is null`() = runTest {
        every { apiPrefs.user } returns null

        useCase(SetCookieConsentUseCase.Params(true))

        verify(exactly = 0) { consentPrefs.setConsent(any(), any(), any()) }
    }
}
