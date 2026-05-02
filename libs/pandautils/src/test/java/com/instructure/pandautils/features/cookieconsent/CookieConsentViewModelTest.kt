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
import io.mockk.coEvery
import io.mockk.coVerify
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
class CookieConsentViewModelTest {

    private val getCookieConsentUseCase: GetCookieConsentUseCase = mockk(relaxed = true)
    private val setCookieConsentUseCase: SetCookieConsentUseCase = mockk(relaxed = true)
    private val analyticsConsentHandler: AnalyticsConsentHandler = mockk(relaxed = true)
    private val namespace = CookieConsentNamespace.STUDENT
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
    fun `initial state has loading true and showDialog false`() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.loading)
        assertFalse(state.showDialog)
        assertNull(state.consentResult)
        assertNull(state.errorMessage)
        assertFalse(state.saving)
    }

    @Test
    fun `initial state has correct namespace`() {
        val viewModel = createViewModel()

        assertEquals(CookieConsentNamespace.STUDENT, viewModel.uiState.value.namespace)
    }

    @Test
    fun `checkAndShowIfNeeded shows dialog when flag enabled and consent is null`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, consent = null
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertTrue(state.showDialog)
        assertNull(state.consentResult)
    }

    @Test
    fun `checkAndShowIfNeeded skips dialog when flag disabled`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = null, consent = null
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.showDialog)
        assertEquals(ConsentResult(consentGiven = false, needed = false), state.consentResult)
    }

    @Test
    fun `checkAndShowIfNeeded skips dialog when consent already given`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_TRACK, consent = true
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.showDialog)
        assertEquals(ConsentResult(consentGiven = true, needed = false), state.consentResult)
    }

    @Test
    fun `checkAndShowIfNeeded skips dialog when consent already declined`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = com.instructure.canvasapi2.models.UserSettings.USAGE_METRICS_NO_TRACK, consent = false
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.showDialog)
        assertEquals(ConsentResult(consentGiven = false, needed = false), state.consentResult)
    }

    @Test
    fun `checkAndShowIfNeeded handles exception by skipping dialog`() {
        coEvery { getCookieConsentUseCase(Unit) } throws RuntimeException("Network error")

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.showDialog)
        assertEquals(ConsentResult(consentGiven = false, needed = false), state.consentResult)
    }

    @Test
    fun `checkAndShowIfNeeded invokes use case`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = null, consent = null
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        coVerify { getCookieConsentUseCase(Unit) }
    }

    @Test
    fun `onAllow submits consent true and sets consent result`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, consent = null
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        viewModel.uiState.value.onAllow()

        val state = viewModel.uiState.value
        assertFalse(state.showDialog)
        assertFalse(state.saving)
        assertEquals(ConsentResult(consentGiven = true, needed = true), state.consentResult)
        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) }
    }

    @Test
    fun `onDecline submits consent false and sets consent result`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, consent = null
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        viewModel.uiState.value.onDecline()

        val state = viewModel.uiState.value
        assertFalse(state.showDialog)
        assertFalse(state.saving)
        assertEquals(ConsentResult(consentGiven = false, needed = true), state.consentResult)
        coVerify { setCookieConsentUseCase(SetCookieConsentUseCase.Params(false)) }
        verify { analyticsConsentHandler.onConsentRevoked() }
    }

    @Test
    fun `submit consent shows error message on failure`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, consent = null
        )
        coEvery { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) } throws RuntimeException("Save failed")

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        viewModel.uiState.value.onAllow()

        val state = viewModel.uiState.value
        assertFalse(state.saving)
        assertEquals("Save failed", state.errorMessage)
        assertNull(state.consentResult)
    }

    @Test
    fun `submit consent shows default error message when exception has no message`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, consent = null
        )
        coEvery { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) } throws RuntimeException()

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        viewModel.uiState.value.onAllow()

        assertEquals("Failed to save consent", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onErrorDismissed clears error message`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = UserSettings.USAGE_METRICS_ASK_FOR_CONSENT, consent = null
        )
        coEvery { setCookieConsentUseCase(SetCookieConsentUseCase.Params(true)) } throws RuntimeException("Error")

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()
        viewModel.uiState.value.onAllow()

        viewModel.uiState.value.onErrorDismissed()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onConsentResultHandled clears consent result`() {
        coEvery { getCookieConsentUseCase(Unit) } returns GetCookieConsentUseCase.Result(
            usageMetrics = null, consent = null
        )

        val viewModel = createViewModel()
        viewModel.checkAndShowIfNeeded()

        viewModel.uiState.value.onConsentResultHandled()

        assertNull(viewModel.uiState.value.consentResult)
    }

    private fun createViewModel(): CookieConsentViewModel {
        return CookieConsentViewModel(
            getCookieConsentUseCase,
            setCookieConsentUseCase,
            namespace,
            analyticsConsentHandler
        )
    }
}
