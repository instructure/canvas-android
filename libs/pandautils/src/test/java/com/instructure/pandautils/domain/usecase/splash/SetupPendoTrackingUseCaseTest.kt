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
package com.instructure.pandautils.domain.usecase.splash

import android.content.Context
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.models.UserSettings
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.features.FeaturesRepository
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.PendoTokenConfig
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import sdk.pendo.io.Pendo

class SetupPendoTrackingUseCaseTest {

    private val featuresRepository: FeaturesRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val consentPrefs: ConsentPrefs = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val pendoTokenConfig = PendoTokenConfig(
        fallbackToken = "fallback-token",
        apiTokenSelector = { it.pendoMobileStudentApiKey }
    )

    private val useCase = SetupPendoTrackingUseCase(
        featuresRepository, userRepository, apiPrefs, featureFlagProvider,
        consentPrefs, analytics, pendoTokenConfig, context
    )

    @Before
    fun setup() {
        mockkStatic(Pendo::class)
        every { Pendo.startSession(any(), any(), any(), any()) } returns Unit
        every { Pendo.endSession() } returns Unit
        every { Pendo.setup(any(), any(), any(), any()) } returns Unit
        every { analytics.isSessionActive() } returns false
        coEvery { userRepository.getSelfWithUuid(any()) } returns DataResult.Success(
            User(uuid = "test-uuid", accountUuid = "account-uuid")
        )
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `track_usage starts pendo session without consent check`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK)
        )

        useCase(Unit)

        verify { Pendo.startSession(any(), any(), any(), any()) }
    }

    @Test
    fun `no_track_usage ends pendo session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_NO_TRACK)
        )

        useCase(Unit)

        verify { Pendo.endSession() }
    }

    @Test
    fun `ask_for_consent with consent granted starts session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        )
        every { consentPrefs.currentUserConsent } returns true

        useCase(Unit)

        verify { Pendo.startSession(any(), any(), any(), any()) }
    }

    @Test
    fun `ask_for_consent with consent declined ends session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        )
        every { consentPrefs.currentUserConsent } returns false

        useCase(Unit)

        verify { Pendo.endSession() }
    }

    @Test
    fun `ask_for_consent with no consent decision ends session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        )
        every { consentPrefs.currentUserConsent } returns null

        useCase(Unit)

        verify { Pendo.endSession() }
    }

    @Test
    fun `null usageMetrics falls back to feature flag enabled and starts session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to true)
        )

        useCase(Unit)

        verify { Pendo.startSession(any(), any(), any(), any()) }
    }

    @Test
    fun `null usageMetrics falls back to feature flag disabled and ends session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to false)
        )

        useCase(Unit)

        verify { Pendo.endSession() }
    }

    @Test
    fun `null usageMetrics from success response falls back to feature flag`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(UserSettings())
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to true)
        )

        useCase(Unit)

        verify { Pendo.startSession(any(), any(), any(), any()) }
    }

    @Test
    fun `feature flag fallback does not check consent`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to true)
        )

        useCase(Unit)

        verify(exactly = 0) { consentPrefs.currentUserConsent }
    }

    @Test
    fun `track_usage does not check consent`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK)
        )

        useCase(Unit)

        verify(exactly = 0) { consentPrefs.currentUserConsent }
    }

    @Test
    fun `uses api token from settings when available`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK, pendoMobileStudentApiKey = "api-token")
        )

        useCase(Unit)

        verify { Pendo.setup(any(), "api-token", any(), any()) }
    }

    @Test
    fun `falls back to hardcoded token when api token is null`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK, pendoMobileStudentApiKey = null)
        )

        useCase(Unit)

        verify { Pendo.setup(any(), "fallback-token", any(), any()) }
    }

    @Test
    fun `falls back to hardcoded token when api token is empty`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK, pendoMobileStudentApiKey = "")
        )

        useCase(Unit)

        verify { Pendo.setup(any(), "fallback-token", any(), any()) }
    }

    @Test
    fun `falls back to hardcoded token when settings call fails`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to true)
        )

        useCase(Unit)

        verify { Pendo.setup(any(), "fallback-token", any(), any()) }
    }

    @Test
    fun `skips pendo setup when session is already active`() = runTest {
        every { analytics.isSessionActive() } returns true
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK)
        )

        useCase(Unit)

        verify(exactly = 0) { Pendo.setup(any(), any(), any(), any()) }
        verify { Pendo.startSession(any(), any(), any(), any()) }
    }

    @Test
    fun `calls pendo setup when session is not active`() = runTest {
        every { analytics.isSessionActive() } returns false
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK)
        )

        useCase(Unit)

        verify { Pendo.setup(any(), any(), any(), any()) }
        verify { Pendo.startSession(any(), any(), any(), any()) }
    }
}
