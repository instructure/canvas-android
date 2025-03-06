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
package com.instructure.pandautils.features.settings.inboxsignature

import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InboxSignatureRepositoryTest {

    private val inboxSettingsManager: InboxSettingsManager = mockk(relaxed = true)

    private val repository = InboxSignatureRepository(inboxSettingsManager)

    @Test
    fun `Repository calls get settings and returns its result`() = runTest {
        val expected = DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "I don't want to write more tests"
            )
        )
        coEvery { inboxSettingsManager.getInboxSignatureSettings(any()) } returns expected

        val result = repository.getInboxSignature()

        assertEquals(expected, result)
        coVerify { inboxSettingsManager.getInboxSignatureSettings(forceNetwork = true)}
    }

    @Test
    fun `Repository calls update settings and returns its result`() = runTest {
        val updatedSettings = InboxSignatureSettings(useSignature = true, signature = "Okay, almost done, keep going!")
        val expected = DataResult.Success(updatedSettings)
        coEvery { inboxSettingsManager.updateInboxSignatureSettings(any()) } returns expected

        val result = repository.updateInboxSignature(updatedSettings)

        assertEquals(expected, result)
        coVerify { inboxSettingsManager.updateInboxSignatureSettings(updatedSettings) }
    }
}