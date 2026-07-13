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
package com.instructure.ngc.features.assignmentdetails

import androidx.lifecycle.SavedStateHandle
import com.instructure.ngc.features.submission.domain.FileDraftData
import com.instructure.ngc.features.submission.domain.GetFileDraftUseCase
import com.instructure.ngc.features.submission.domain.GetTextDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmissionFileData
import com.instructure.ngc.features.submission.domain.TextDraftData
import com.instructure.pandautils.utils.Const
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NGCSubmitDraftHintsViewModelTest {

    private val getTextDraftUseCase: GetTextDraftUseCase = mockk()
    private val getFileDraftUseCase: GetFileDraftUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val assignmentId = 99L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): NGCSubmitDraftHintsViewModel = NGCSubmitDraftHintsViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Const.ASSIGNMENT_ID to assignmentId)),
        getTextDraftUseCase = getTextDraftUseCase,
        getFileDraftUseCase = getFileDraftUseCase,
    )

    @Test
    fun `uiState defaults to no drafts`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns null
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.hasTextDraft)
        assertFalse(state.hasFileDraft)
    }

    @Test
    fun `uiState reflects text draft when one exists`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns TextDraftData(html = "<p>draft</p>", savedAt = Date())
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.hasTextDraft)
        assertFalse(vm.uiState.value.hasFileDraft)
    }

    @Test
    fun `uiState reflects file draft when one exists with files`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns null
        val fileData = SubmissionFileData(
            id = 1L,
            name = "essay.pdf",
            size = 1000L,
            contentType = "application/pdf",
            fullPath = "/path/essay.pdf",
            errorFlag = false,
            error = null,
        )
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(listOf(fileData), Date())

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.hasTextDraft)
        assertTrue(vm.uiState.value.hasFileDraft)
    }

    @Test
    fun `uiState hasFileDraft is false when file draft has empty file list`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns null
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), Date())

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.hasFileDraft)
    }

    @Test
    fun `refresh re-loads draft hints into uiState`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns null
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.hasTextDraft)

        coEvery { getTextDraftUseCase(any()) } returns TextDraftData(html = "<p>new draft</p>", savedAt = Date())
        vm.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.hasTextDraft)
    }

    @Test
    fun `uiState exposes both drafts when both exist`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns TextDraftData(html = "<p>text</p>", savedAt = Date())
        val fileData = SubmissionFileData(
            id = 2L,
            name = "doc.pdf",
            size = 500L,
            contentType = "application/pdf",
            fullPath = "/path/doc.pdf",
            errorFlag = false,
            error = null,
        )
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(listOf(fileData), Date())

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.hasTextDraft)
        assertTrue(state.hasFileDraft)
    }

    @Test
    fun `uiState hasTextDraft is false when use case throws`() = runTest {
        coEvery { getTextDraftUseCase(any()) } throws RuntimeException("DB error")
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.hasTextDraft)
    }

    @Test
    fun `uiState exposes unified state shape matching NGCSubmitDraftHintsUiState`() = runTest {
        coEvery { getTextDraftUseCase(any()) } returns null
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(NGCSubmitDraftHintsUiState(hasTextDraft = false, hasFileDraft = false), state)
    }
}
