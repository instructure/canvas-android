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

import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class SetCookieConsentUseCaseTest {

    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val useCase = SetCookieConsentUseCase(apiPrefs)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `stores true consent in apiPrefs`() = runTest {
        useCase(SetCookieConsentUseCase.Params(true))

        verify { apiPrefs.mobileConsent = true }
    }

    @Test
    fun `stores false consent in apiPrefs`() = runTest {
        useCase(SetCookieConsentUseCase.Params(false))

        verify { apiPrefs.mobileConsent = false }
    }
}
