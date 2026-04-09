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
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCookieConsentUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val useCase = GetCookieConsentUseCase(userRepository, featureFlagProvider)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `returns flag disabled with null consent when feature flag is off`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns false

        val result = useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT))

        assertFalse(result.flagEnabled)
        assertNull(result.consent)
    }

    @Test
    fun `fetches environment feature flags before checking`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns false

        useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT))

        coVerify(ordering = Ordering.ORDERED) {
            featureFlagProvider.fetchEnvironmentFeatureFlags()
            featureFlagProvider.checkCookieConsentFlag()
        }
    }

    @Test
    fun `does not call repository when flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns false

        useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT))

        coVerify(exactly = 0) { userRepository.getCookieConsentData(any()) }
    }

    @Test
    fun `returns flag enabled with true consent when api returns true`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        coEvery { userRepository.getCookieConsentData(any()) } returns DataResult.Success(
            CookieConsentResponse(CookieConsentContent(true))
        )

        val result = useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT))

        assertTrue(result.flagEnabled)
        assertEquals(true, result.consent)
    }

    @Test
    fun `returns flag enabled with false consent when api returns false`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        coEvery { userRepository.getCookieConsentData(any()) } returns DataResult.Success(
            CookieConsentResponse(CookieConsentContent(false))
        )

        val result = useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.TEACHER))

        assertTrue(result.flagEnabled)
        assertEquals(false, result.consent)
    }

    @Test
    fun `returns flag enabled with null consent when api returns null data`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        coEvery { userRepository.getCookieConsentData(any()) } returns DataResult.Success(
            CookieConsentResponse(null)
        )

        val result = useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.PARENT))

        assertTrue(result.flagEnabled)
        assertNull(result.consent)
    }

    @Test
    fun `returns flag enabled with null consent when api fails`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        coEvery { userRepository.getCookieConsentData(any()) } returns DataResult.Fail()

        val result = useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.STUDENT))

        assertTrue(result.flagEnabled)
        assertNull(result.consent)
    }

    @Test
    fun `calls repository with correct namespace value`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        coEvery { userRepository.getCookieConsentData(any()) } returns DataResult.Fail()

        useCase(GetCookieConsentUseCase.Params(CookieConsentNamespace.TEACHER))

        coVerify { userRepository.getCookieConsentData("MOBILE_CANVAS_TEACHER_COOKIE_CONSENT") }
    }
}

