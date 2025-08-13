/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.settings

import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.models.EnvironmentSettings
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {

    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)
    private val inboxSettingsManager: InboxSettingsManager = mockk(relaxed = true)
    private val settingsBehaviour: SettingsBehaviour = mockk(relaxed = true)
    private val experienceApi: ExperienceAPI = mockk(relaxed = true)

    private val repository = SettingsRepository(featuresApi, inboxSettingsManager, settingsBehaviour, experienceApi)

    @Test
    fun `Return hidden state when feature request fails`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Fail()

        val inboxSignatureState = repository.getInboxSignatureState()

        assertEquals(InboxSignatureState.HIDDEN, inboxSignatureState)
    }

    @Test
    fun `Return hidden state when feature is disabled`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = false))

        val inboxSignatureState = repository.getInboxSignatureState()

        assertEquals(InboxSignatureState.HIDDEN, inboxSignatureState)
    }

    @Test
    fun `Return hidden state when feature is enabled but disabled for the role`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = true))
        coEvery { settingsBehaviour.isInboxSignatureEnabledForRole(any()) } returns false

        val inboxSignatureState = repository.getInboxSignatureState()

        assertEquals(InboxSignatureState.HIDDEN, inboxSignatureState)
    }

    @Test
    fun `Return unknown state when feature is enabled but signature request failed`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = true))
        coEvery { settingsBehaviour.isInboxSignatureEnabledForRole(any()) } returns true
        coEvery { inboxSettingsManager.getInboxSignatureSettings(any()) } returns DataResult.Fail()

        val inboxSignatureState = repository.getInboxSignatureState()

        assertEquals(InboxSignatureState.UNKNOWN, inboxSignatureState)
    }

    @Test
    fun `Return enabled state when signature is enabled`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = true))
        coEvery { settingsBehaviour.isInboxSignatureEnabledForRole(any()) } returns true
        coEvery { inboxSettingsManager.getInboxSignatureSettings(any()) } returns DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "signature"
            )
        )

        val inboxSignatureState = repository.getInboxSignatureState()

        assertEquals(InboxSignatureState.ENABLED, inboxSignatureState)
    }

    @Test
    fun `Return disabled state when signature is disabled`() = runTest {
        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = true))
        coEvery { settingsBehaviour.isInboxSignatureEnabledForRole(any()) } returns true
        coEvery { inboxSettingsManager.getInboxSignatureSettings(any()) } returns DataResult.Success(
            InboxSignatureSettings(
                useSignature = false,
                signature = "signature"
            )
        )

        val inboxSignatureState = repository.getInboxSignatureState()

        assertEquals(InboxSignatureState.DISABLED, inboxSignatureState)
    }
}