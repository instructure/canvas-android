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

import com.instructure.canvasapi2.models.UserSettings
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.user.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCookieConsentUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val consentPrefs: ConsentPrefs = mockk(relaxed = true)

    private val useCase = GetCookieConsentUseCase(userRepository, consentPrefs)

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `returns null results when settings call fails`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()

        val result = useCase(Unit)

        assertNull(result.usageMetrics)
        assertNull(result.consent)
    }

    @Test
    fun `returns null results when usageMetrics is absent from settings`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(UserSettings())

        val result = useCase(Unit)

        assertNull(result.usageMetrics)
        assertNull(result.consent)
    }

    @Test
    fun `does not read ConsentPrefs when usageMetrics is absent`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(UserSettings())

        useCase(Unit)

        verify(exactly = 0) { consentPrefs.currentUserConsent }
    }

    @Test
    fun `returns usageMetrics and true consent`() = runTest {
        val settings = UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(settings)
        every { consentPrefs.currentUserConsent } returns true

        val result = useCase(Unit)

        assertEquals(UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, result.usageMetrics)
        assertEquals(true, result.consent)
    }

    @Test
    fun `returns usageMetrics and false consent`() = runTest {
        val settings = UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(settings)
        every { consentPrefs.currentUserConsent } returns false

        val result = useCase(Unit)

        assertEquals(UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, result.usageMetrics)
        assertEquals(false, result.consent)
    }

    @Test
    fun `returns usageMetrics and null consent when no decision stored`() = runTest {
        val settings = UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(settings)
        every { consentPrefs.currentUserConsent } returns null

        val result = useCase(Unit)

        assertEquals(UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, result.usageMetrics)
        assertNull(result.consent)
    }

    @Test
    fun `passes track_usage value through from settings`() = runTest {
        val settings = UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK)
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(settings)

        val result = useCase(Unit)

        assertEquals(UserSettings.USAGE_METRICS_TRACK, result.usageMetrics)
    }

    @Test
    fun `passes no_track_usage value through from settings`() = runTest {
        val settings = UserSettings(usageMetrics = UserSettings.USAGE_METRICS_NO_TRACK)
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(settings)

        val result = useCase(Unit)

        assertEquals(UserSettings.USAGE_METRICS_NO_TRACK, result.usageMetrics)
    }
}
