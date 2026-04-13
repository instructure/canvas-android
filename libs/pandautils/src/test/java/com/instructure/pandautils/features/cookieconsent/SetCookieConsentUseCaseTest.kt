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

import com.instructure.canvasapi2.models.CookieConsentContent
import com.instructure.canvasapi2.models.CookieConsentResponse
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.user.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class SetCookieConsentUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val useCase = SetCookieConsentUseCase(userRepository, apiPrefs)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `sets consent to true and updates api prefs`() = runTest {
        coEvery { userRepository.putCookieConsentData(any(), any()) } returns DataResult.Success(
            CookieConsentResponse(CookieConsentContent(true))
        )

        useCase(SetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT, true))

        coVerify { userRepository.putCookieConsentData("MOBILE_CANVAS_STUDENT_COOKIE_CONSENT", true) }
        verify { apiPrefs.mobileConsent = true }
    }

    @Test
    fun `sets consent to false and updates api prefs`() = runTest {
        coEvery { userRepository.putCookieConsentData(any(), any()) } returns DataResult.Success(
            CookieConsentResponse(CookieConsentContent(false))
        )

        useCase(SetCookieConsentUseCase.Params(CookieConsentNamespace.TEACHER, false))

        coVerify { userRepository.putCookieConsentData("MOBILE_CANVAS_TEACHER_COOKIE_CONSENT", false) }
        verify { apiPrefs.mobileConsent = false }
    }

    @Test
    fun `uses correct namespace for parent`() = runTest {
        coEvery { userRepository.putCookieConsentData(any(), any()) } returns DataResult.Success(
            CookieConsentResponse(CookieConsentContent(true))
        )

        useCase(SetCookieConsentUseCase.Params(CookieConsentNamespace.PARENT, true))

        coVerify { userRepository.putCookieConsentData("MOBILE_CANVAS_PARENT_COOKIE_CONSENT", true) }
    }

    @Test(expected = IllegalStateException::class)
    fun `throws exception when repository fails`() = runTest {
        coEvery { userRepository.putCookieConsentData(any(), any()) } returns DataResult.Fail()

        useCase(SetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT, true))
    }

    @Test
    fun `does not update api prefs when repository fails`() = runTest {
        coEvery { userRepository.putCookieConsentData(any(), any()) } returns DataResult.Fail()

        runCatching {
            useCase(SetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT, true))
        }

        verify(exactly = 0) { apiPrefs.mobileConsent = any() }
    }
}

