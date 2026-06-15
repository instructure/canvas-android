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
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.features.FeaturesRepository
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.features.cookieconsent.SetCookieConsentUseCase
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.PendoTokenConfig
import com.instructure.pandautils.utils.PendoTokenHolder
import io.mockk.coEvery
import io.mockk.coVerify
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
    private val setCookieConsentUseCase: SetCookieConsentUseCase = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val pendoTokenConfig = PendoTokenConfig(
        fallbackToken = "fallback-token",
        apiTokenSelector = { it.pendoMobileStudentApiKey }
    )

    private val useCase = SetupPendoTrackingUseCase(
        featuresRepository, userRepository, apiPrefs, featureFlagProvider,
        consentPrefs, pendoTokenConfig, setCookieConsentUseCase, context
    )

    @Before
    fun setup() {
        mockkStatic(Pendo::class)
        every { Pendo.startSession(any(), any(), any(), any()) } returns Unit
        every { Pendo.endSession() } returns Unit
        every { Pendo.setup(any(), any(), any(), any()) } returns Unit
        coEvery { userRepository.getSelfWithUuid(any()) } returns DataResult.Success(
            User(uuid = "test-uuid", accountUuid = "account-uuid")
        )
        PendoTokenHolder.initializedToken = null
    }

    @After
    fun teardown() {
        PendoTokenHolder.initializedToken = null
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
    fun `only_guide_events ends pendo session`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ONLY_GUIDE_EVENTS)
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
    fun `track_usage persists consent as true`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK)
        )

        useCase(Unit)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) }
    }

    @Test
    fun `no_track_usage persists consent as false`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_NO_TRACK)
        )

        useCase(Unit)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(false)) }
    }

    @Test
    fun `only_guide_events persists consent as false`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ONLY_GUIDE_EVENTS)
        )

        useCase(Unit)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(false)) }
    }

    @Test
    fun `feature flag enabled persists consent as true`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to true)
        )

        useCase(Unit)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) }
    }

    @Test
    fun `feature flag disabled persists consent as false`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Fail()
        coEvery { featuresRepository.getEnvironmentFeatureFlags(any()) } returns DataResult.Success(
            mapOf(FeaturesManager.SEND_USAGE_METRICS to false)
        )

        useCase(Unit)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(false)) }
    }

    @Test
    fun `ask_for_consent does not persist consent`() = runTest {
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT)
        )
        every { consentPrefs.currentUserConsent } returns true

        useCase(Unit)

        coVerify(exactly = 0) { setCookieConsentUseCase(any()) }
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
    fun `skips pendo setup when token is already initialized with the same value`() = runTest {
        PendoTokenHolder.initializedToken = "api-token"
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK, pendoMobileStudentApiKey = "api-token")
        )

        useCase(Unit)

        verify(exactly = 0) { Pendo.setup(any(), any(), any(), any()) }
        verify { Pendo.startSession(any(), any(), any(), any()) }
    }

    @Test
    fun `calls pendo setup when token has not been initialized yet`() = runTest {
        PendoTokenHolder.initializedToken = null
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK, pendoMobileStudentApiKey = "api-token")
        )

        useCase(Unit)

        verify { Pendo.setup(any(), "api-token", any(), any()) }
        verify { Pendo.startSession(any(), any(), any(), any()) }
        assert(PendoTokenHolder.initializedToken == "api-token")
    }

    @Test
    fun `ends session and skips start when resolved token differs from initialized token`() = runTest {
        PendoTokenHolder.initializedToken = "old-token"
        coEvery { userRepository.getMobileSettings(any()) } returns DataResult.Success(
            UserSettings(usageMetrics = UserSettings.USAGE_METRICS_TRACK, pendoMobileStudentApiKey = "new-token")
        )

        useCase(Unit)

        verify { Pendo.endSession() }
        verify(exactly = 0) { Pendo.setup(any(), any(), any(), any()) }
        verify(exactly = 0) { Pendo.startSession(any(), any(), any(), any()) }
        assert(PendoTokenHolder.initializedToken == "old-token")
    }
}
