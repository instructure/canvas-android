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
package com.instructure.pandautils.features.privacysettings

import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.SetCookieConsentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacySettingsViewModelTest {

    private val setCookieConsentUseCase: SetCookieConsentUseCase = mockk(relaxed = true)
    private val analyticsConsentHandler: AnalyticsConsentHandler = mockk(relaxed = true)
    private val consentPrefs: ConsentPrefs = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state reflects consent true from prefs`() {
        every { consentPrefs.currentUserConsent } returns true

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.consentEnabled)
    }

    @Test
    fun `initial state reflects consent false from prefs`() {
        every { consentPrefs.currentUserConsent } returns false

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.consentEnabled)
    }

    @Test
    fun `initial state reflects consent null from prefs as false`() {
        every { consentPrefs.currentUserConsent } returns null

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.consentEnabled)
    }

    @Test
    fun `initial state has saving false and no error`() {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.saving)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onToggleChanged true saves consent and calls onConsentGranted`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.onToggleChanged(true)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) }
        verify { analyticsConsentHandler.onConsentGranted() }
    }

    @Test
    fun `onToggleChanged true updates consentEnabled to true`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.onToggleChanged(true)

        assertTrue(viewModel.uiState.value.consentEnabled)
        assertFalse(viewModel.uiState.value.saving)
    }

    @Test
    fun `onToggleChanged false saves consent and calls onConsentRevoked`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.onToggleChanged(false)

        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(false)) }
        verify { analyticsConsentHandler.onConsentRevoked() }
    }

    @Test
    fun `onToggleChanged false updates consentEnabled to false`() {
        every { consentPrefs.currentUserConsent } returns true
        val viewModel = createViewModel()

        viewModel.uiState.value.onToggleChanged(false)

        assertFalse(viewModel.uiState.value.consentEnabled)
        assertFalse(viewModel.uiState.value.saving)
    }

    @Test
    fun `onToggleChanged sets error message on failure`() {
        coEvery { setCookieConsentUseCase(any()) } throws RuntimeException("Save failed")
        val viewModel = createViewModel()

        viewModel.uiState.value.onToggleChanged(true)

        assertEquals("Save failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.saving)
    }

    @Test
    fun `onToggleChanged does not call handler on failure`() {
        coEvery { setCookieConsentUseCase(any()) } throws RuntimeException("Save failed")
        val viewModel = createViewModel()

        viewModel.uiState.value.onToggleChanged(true)

        verify(exactly = 0) { analyticsConsentHandler.onConsentGranted() }
        verify(exactly = 0) { analyticsConsentHandler.onConsentRevoked() }
    }

    @Test
    fun `onErrorDismissed clears error message`() {
        coEvery { setCookieConsentUseCase(any()) } throws RuntimeException("Save failed")
        val viewModel = createViewModel()
        viewModel.uiState.value.onToggleChanged(true)

        viewModel.uiState.value.onErrorDismissed()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    private fun createViewModel() = PrivacySettingsViewModel(
        setCookieConsentUseCase,
        analyticsConsentHandler,
        consentPrefs
    )
}
