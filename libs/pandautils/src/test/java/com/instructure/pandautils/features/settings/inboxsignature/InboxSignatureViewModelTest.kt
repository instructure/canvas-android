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

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxSignatureViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val repository: InboxSignatureRepository = mockk(relaxed = true)

    @Before
    fun setup() {
    }

    @Test
    fun `Show error state when signature request fails`() {
        coEvery { repository.getInboxSignature() } returns DataResult.Fail()

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.error)
    }

    @Test
    fun `Show signature state when signature request is successful`() {
        val expected = DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "I don't want to write more tests"
            )
        )
        coEvery { repository.getInboxSignature() } returns expected

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.error)
        assertEquals("I don't want to write more tests", viewModel.uiState.value.signatureText.text)
        assertTrue(viewModel.uiState.value.signatureEnabled)
    }

    @Test
    fun `Refresh after error`() {
        coEvery { repository.getInboxSignature() } returns DataResult.Fail()

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.error)

        coEvery { repository.getInboxSignature() } returns DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "This is fine"
            )
        )

        viewModel.handleAction(InboxSignatureAction.Refresh)

        assertFalse(viewModel.uiState.value.error)
        assertEquals("This is fine", viewModel.uiState.value.signatureText.text)
        assertTrue(viewModel.uiState.value.signatureEnabled)
    }

    @Test
    fun `Changing signature text updates save enabled`() {
        val expected = DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "Something new"
            )
        )
        coEvery { repository.getInboxSignature() } returns expected

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.signatureEnabled)
        assertEquals("Something new", viewModel.uiState.value.signatureText.text)

        viewModel.handleAction(InboxSignatureAction.UpdateSignature(TextFieldValue("")))

        assertFalse(viewModel.uiState.value.saveEnabled)
        assertEquals("", viewModel.uiState.value.signatureText.text)
    }

    @Test
    fun `Changing signature enabled updates save enabled`() {
        val expected = DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "Something new"
            )
        )
        coEvery { repository.getInboxSignature() } returns expected

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.signatureEnabled)
        assertEquals("Something new", viewModel.uiState.value.signatureText.text)

        viewModel.handleAction(InboxSignatureAction.UpdateSignature(TextFieldValue("")))

        assertFalse(viewModel.uiState.value.saveEnabled)
        assertEquals("", viewModel.uiState.value.signatureText.text)

        viewModel.handleAction(InboxSignatureAction.UpdateSignatureEnabled(false))

        assertTrue(viewModel.uiState.value.saveEnabled)
    }

    @Test
    fun `Save signature successfully`() = runTest {
        val expected = DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "Sign"
            )
        )
        coEvery { repository.getInboxSignature() } returns expected
        coEvery { repository.updateInboxSignature(any()) } returns expected

        val viewModel = createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.handleAction(InboxSignatureAction.Save)

        coVerify { repository.updateInboxSignature(InboxSignatureSettings("Sign", true)) }
        assertEquals(InboxSignatureViewModelAction.CloseAndUpdateSettings(true), events.last())
    }

    @Test
    fun `Save signature error`() = runTest {
        val expected = DataResult.Success(
            InboxSignatureSettings(
                useSignature = true,
                signature = "Sign"
            )
        )
        coEvery { repository.getInboxSignature() } returns expected
        coEvery { repository.updateInboxSignature(any()) } returns DataResult.Fail()

        val viewModel = createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.handleAction(InboxSignatureAction.Save)

        coVerify { repository.updateInboxSignature(InboxSignatureSettings("Sign", true)) }
        assertEquals(InboxSignatureViewModelAction.ShowErrorToast, events.last())
    }

    private fun createViewModel(): InboxSignatureViewModel {
        return InboxSignatureViewModel(repository)
    }
}
