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

import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCookieConsentUseCaseTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val consentPrefs: ConsentPrefs = mockk(relaxed = true)

    private val useCase = GetCookieConsentUseCase(featureFlagProvider, consentPrefs)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `returns flag disabled with null consent when feature flag is off`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns false

        val result = useCase(Unit)

        assertFalse(result.flagEnabled)
        assertNull(result.consent)
    }

    @Test
    fun `fetches environment feature flags before checking`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns false

        useCase(Unit)

        coVerify(ordering = Ordering.ORDERED) {
            featureFlagProvider.fetchEnvironmentFeatureFlags()
            featureFlagProvider.checkCookieConsentFlag()
        }
    }

    @Test
    fun `does not read ConsentPrefs when flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns false

        useCase(Unit)

        verify(exactly = 0) { consentPrefs.currentUserConsent }
    }

    @Test
    fun `returns flag enabled with true consent when ConsentPrefs stores true`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        every { consentPrefs.currentUserConsent } returns true

        val result = useCase(Unit)

        assertTrue(result.flagEnabled)
        assertEquals(true, result.consent)
    }

    @Test
    fun `returns flag enabled with false consent when ConsentPrefs stores false`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        every { consentPrefs.currentUserConsent } returns false

        val result = useCase(Unit)

        assertTrue(result.flagEnabled)
        assertEquals(false, result.consent)
    }

    @Test
    fun `returns flag enabled with null consent when ConsentPrefs has no decision stored`() = runTest {
        coEvery { featureFlagProvider.checkCookieConsentFlag() } returns true
        every { consentPrefs.currentUserConsent } returns null

        val result = useCase(Unit)

        assertTrue(result.flagEnabled)
        assertNull(result.consent)
    }
}
