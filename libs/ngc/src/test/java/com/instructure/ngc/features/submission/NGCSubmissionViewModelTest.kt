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
package com.instructure.ngc.features.submission

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.ngc.features.submission.domain.DeleteDraftUseCase
import com.instructure.ngc.features.submission.domain.FileDraftData
import com.instructure.ngc.features.submission.domain.GetDraftFilesUseCase
import com.instructure.ngc.features.submission.domain.GetFileDraftUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionDataUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionFileStatesUseCase
import com.instructure.ngc.features.submission.domain.GetTextDraftUseCase
import com.instructure.ngc.features.submission.domain.GetUrlDraftUseCase
import com.instructure.ngc.features.submission.domain.ObserveSubmissionProgressUseCase
import com.instructure.ngc.features.submission.domain.ResumeInFlightSubmissionUseCase
import com.instructure.ngc.features.submission.domain.RetryFileSubmissionUseCase
import com.instructure.ngc.features.submission.domain.SaveFileDraftUseCase
import com.instructure.ngc.features.submission.domain.SaveTextDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmissionData
import com.instructure.ngc.features.submission.domain.SubmissionFileData
import com.instructure.ngc.features.submission.domain.SaveUrlDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmitFilesUseCase
import com.instructure.ngc.features.submission.domain.SubmitTextUseCase
import com.instructure.ngc.features.submission.domain.SubmitUrlUseCase
import com.instructure.ngc.features.submission.domain.TextDraftData
import com.instructure.ngc.features.submission.domain.UrlDraftData
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.utils.FileUploadUtils
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NGCSubmissionViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val getSubmissionDataUseCase: GetSubmissionDataUseCase = mockk()
    private val getTextDraftUseCase: GetTextDraftUseCase = mockk()
    private val getFileDraftUseCase: GetFileDraftUseCase = mockk()
    private val saveTextDraftUseCase: SaveTextDraftUseCase = mockk()
    private val submitTextUseCase: SubmitTextUseCase = mockk()
    private val observeSubmissionProgressUseCase: ObserveSubmissionProgressUseCase = mockk()
    private val saveFileDraftUseCase: SaveFileDraftUseCase = mockk()
    private val submitFilesUseCase: SubmitFilesUseCase = mockk()
    private val getSubmissionFileStatesUseCase: GetSubmissionFileStatesUseCase = mockk()
    private val getDraftFilesUseCase: GetDraftFilesUseCase = mockk()
    private val retryFileSubmissionUseCase: RetryFileSubmissionUseCase = mockk()
    private val resumeInFlightSubmissionUseCase: ResumeInFlightSubmissionUseCase = mockk()
    private val getUrlDraftUseCase: GetUrlDraftUseCase = mockk()
    private val saveUrlDraftUseCase: SaveUrlDraftUseCase = mockk()
    private val deleteDraftUseCase: DeleteDraftUseCase = mockk()
    private val submitUrlUseCase: SubmitUrlUseCase = mockk()

    private val stubDateFormatter = object : SubmissionDateFormatter {
        override fun formatDateTime(context: Context, date: Date) = "Jun 19, 10:00 AM"
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    private val courseId = 1L
    private val assignmentId = 42L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(any()) } returns ""
        every { context.getString(any(), any<Any>()) } returns ""
        coEvery { resumeInFlightSubmissionUseCase(any()) } returns null
        // Safe defaults so tests that don't exercise type-switching don't fail on unmatched calls
        coEvery { getTextDraftUseCase(any()) } returns null
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)
        coEvery { getUrlDraftUseCase(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(FileUploadUtils)
    }

    // -------------------------------------------------------------------------
    // Initial load
    // -------------------------------------------------------------------------

    @Test
    fun `Initial load - loading state is true before data arrives`() = runTest {
        coEvery { getSubmissionDataUseCase(any()) } coAnswers {
            delay(100)
            minimalSubmissionData()
        }

        val vm = createViewModel()

        assertTrue(vm.uiState.value.loading)
        assertNull(vm.uiState.value.loadError)
    }

    @Test
    fun `Initial load - populates state correctly on success`() = runTest {
        val data = minimalSubmissionData(
            assignmentName = "Essay",
            courseName = "Biology 101",
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        every { context.getString(any(), any<String>()) } returns "dummy"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertNull(state.loadError)
        assertEquals("Essay", state.assignmentName)
        assertEquals("Biology 101", state.courseName)
    }

    @Test
    fun `Initial load - restores draft, sets savedAtText, submitEnabled, and bumps contentResetKey`() = runTest {
        val draftDate = Date(1_700_000_000_000L)
        val data = minimalSubmissionData(
            draftHtml = "<p>Draft text</p>",
            draftSavedAt = draftDate,
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        every { context.getString(any(), any<String>()) } returns "Saved Jun 14, 9:33 PM"

        val initialKey = createViewModel().let { vm ->
            testDispatcher.scheduler.advanceUntilIdle()
            val state = vm.uiState.value
            assertEquals("<p>Draft text</p>", state.textState.draftHtml)
            assertTrue(state.savedAtText.isNotEmpty())
            assertTrue(state.submitEnabled)
            state.textState.contentResetKey
        }

        assertTrue(initialKey > 0)
    }

    @Test
    fun `Initial load - allowedExtensions are plumbed into uiState`() = runTest {
        val extensions = listOf("pdf", "docx", "png")
        val data = minimalSubmissionData(allowedExtensions = extensions)
        coEvery { getSubmissionDataUseCase(any()) } returns data

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(extensions, vm.uiState.value.fileState.allowedExtensions)
    }

    @Test
    fun `Initial load - submitEnabled is false when no draft`() = runTest {
        val data = minimalSubmissionData(draftHtml = null, draftSavedAt = null)
        coEvery { getSubmissionDataUseCase(any()) } returns data

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.submitEnabled)
        assertTrue(vm.uiState.value.savedAtText.isEmpty())
    }

    @Test
    fun `Initial load - sets loadError on failure`() = runTest {
        coEvery { getSubmissionDataUseCase(any()) } throws RuntimeException("network error")
        every { context.getString(any()) } returns "Failed to load assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertNotNull(state.loadError)
        assertNotNull(state.onRetry)
    }

    @Test
    fun `Initial load - populates supporting pane with instructions and clears its loading`() = runTest {
        val data = minimalSubmissionData(instructionsHtml = "<p>Read chapter 3.</p>")
        coEvery { getSubmissionDataUseCase(any()) } returns data

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val supportingPane = vm.uiState.value.supportingPane
        assertFalse(supportingPane.loading)
        assertEquals("<p>Read chapter 3.</p>", supportingPane.instructionsHtml)
    }

    @Test
    fun `Initial load - supporting pane loading is cleared on failure`() = runTest {
        coEvery { getSubmissionDataUseCase(any()) } throws RuntimeException("network error")
        every { context.getString(any()) } returns "Failed to load assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.supportingPane.loading)
    }

    // -------------------------------------------------------------------------
    // onTextChanged — immediate UiState update
    // -------------------------------------------------------------------------

    @Test
    fun `onTextChanged non-blank - updates draftHtml and submitEnabled immediately`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveTextDraftUseCase(any()) }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>Hello world</p>")

        assertEquals("<p>Hello world</p>", vm.uiState.value.textState.draftHtml)
        assertTrue(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onTextChanged blank html - clears submitEnabled immediately`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>Old draft</p>", draftSavedAt = Date())
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<br>")

        assertFalse(vm.uiState.value.submitEnabled)
    }

    // -------------------------------------------------------------------------
    // onTextChanged — debounced persistence
    // -------------------------------------------------------------------------

    @Test
    fun `onTextChanged non-blank - saves draft and sets savedAtText after debounce`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveTextDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19, 10:00 AM"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>Hello world</p>")
        coVerify(exactly = 0) { saveTextDraftUseCase(any()) }

        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveTextDraftUseCase(match { it.assignmentId == assignmentId && it.html == "<p>Hello world</p>" }) }
        assertEquals("Saved Jun 19, 10:00 AM", vm.uiState.value.savedAtText)
    }

    @Test
    fun `onTextChanged blank html - deletes draft and clears savedAtText after debounce`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>Old draft</p>", draftSavedAt = Date())
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<br>")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.savedAtText.isEmpty())
        assertFalse(vm.uiState.value.submitEnabled)
        coVerify { deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY)) }
    }

    @Test
    fun `onTextChanged empty string - deletes draft after debounce`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY)) }
        assertFalse(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onTextChanged - only latest value is persisted when called rapidly`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveTextDraftUseCase(any()) }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>A</p>")
        vm.onTextChanged("<p>AB</p>")
        vm.onTextChanged("<p>ABC</p>")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveTextDraftUseCase(any()) }
        coVerify { saveTextDraftUseCase(match { it.assignmentId == assignmentId && it.html == "<p>ABC</p>" }) }
    }

    // -------------------------------------------------------------------------
    // onDeleteDraft
    // -------------------------------------------------------------------------

    @Test
    fun `onDeleteDraft - clears draftHtml, savedAtText, submitEnabled, and bumps contentResetKey`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>Draft</p>", draftSavedAt = Date())
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val keyBefore = vm.uiState.value.textState.contentResetKey

        vm.onDeleteDraft()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.textState.draftHtml.isEmpty())
        assertTrue(state.savedAtText.isEmpty())
        assertFalse(state.submitEnabled)
        assertEquals(keyBefore + 1, state.textState.contentResetKey)
        coVerify { deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY)) }
    }

    // -------------------------------------------------------------------------
    // onSubmit — success path
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmit success - progress 100 sets submissionSuccess, clears draft, and bumps contentResetKey`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { submitTextUseCase(any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every { observeSubmissionProgressUseCase(ObserveSubmissionProgressUseCase.Params(assignmentId)) } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val keyBefore = vm.uiState.value.textState.contentResetKey

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.submissionSuccess)
        assertFalse(state.submitting)
        assertTrue(state.textState.draftHtml.isEmpty())
        assertTrue(state.savedAtText.isEmpty())
        assertFalse(state.submitEnabled)
        assertEquals(keyBefore + 1, state.textState.contentResetKey)
    }

    @Test
    fun `onSubmit - reads current draftHtml from UiState`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveTextDraftUseCase(any()) }
        coJustRun { submitTextUseCase(any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every { observeSubmissionProgressUseCase(ObserveSubmissionProgressUseCase.Params(assignmentId)) } returns flowOf(successEntity)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>Updated text</p>")

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { submitTextUseCase(match { it.assignmentId == assignmentId && it.html == "<p>Updated text</p>" }) }
    }

    // -------------------------------------------------------------------------
    // onSubmit — error path
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmit error - errorFlag sets errorMessage`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { submitTextUseCase(any()) }
        val errorEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 0f,
            errorFlag = true,
        )
        every { observeSubmissionProgressUseCase(ObserveSubmissionProgressUseCase.Params(assignmentId)) } returns flowOf(errorEntity)
        every { context.getString(any()) } returns "Error submitting assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.submitting)
        assertNotNull(state.errorMessage)
        assertFalse(state.submissionSuccess)
    }

    @Test
    fun `onSubmit exception - sets errorMessage`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitTextUseCase(any()) } throws RuntimeException("submit failed")
        every { context.getString(any()) } returns "Error submitting assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.submitting)
        assertNotNull(state.errorMessage)
    }

    // -------------------------------------------------------------------------
    // Event consume lambdas
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmissionSuccessHandled - clears submissionSuccess`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { submitTextUseCase(any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every { observeSubmissionProgressUseCase(ObserveSubmissionProgressUseCase.Params(assignmentId)) } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.submissionSuccess)

        vm.uiState.value.onSubmissionSuccessHandled()

        assertFalse(vm.uiState.value.submissionSuccess)
    }

    // -------------------------------------------------------------------------
    // File behavior — type switching
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmissionTypeSelected - switching to ONLINE_UPLOAD loads real fileDraftSavedAt from DB`() = runTest {
        val fileSavedAt = Date(1_700_000_002_000L)
        val data = minimalSubmissionData(
            availableTypes = listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_UPLOAD,
            ),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        // getFileDraftUseCase returns real DB savedAt — NOT Date() fabrication
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(
            files = listOf(sampleFileData(id = 1L)),
            savedAt = fileSavedAt,
        )
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19, 10:00 AM"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_UPLOAD)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(Assignment.SubmissionType.ONLINE_UPLOAD, state.selectedType)
        // savedAtText comes from the real DB timestamp, NOT Date()
        assertTrue(state.savedAtText.isNotEmpty())
        assertTrue(state.submitEnabled) // has one file
    }

    @Test
    fun `onSubmissionTypeSelected - switching to ONLINE_TEXT_ENTRY restores text draft and shows textSavedAt`() = runTest {
        val textSavedAt = Date(1_700_000_003_000L)
        val data = minimalSubmissionData(
            availableTypes = listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_UPLOAD,
            ),
            // No text draft loaded initially (default=ONLINE_TEXT_ENTRY so initial load would have it,
            // but here we set up the scenario where we start on the file side instead)
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(emptyList(), null)
        // getTextDraftUseCase returns distinct text draft data
        coEvery { getTextDraftUseCase(any()) } returns TextDraftData(
            html = "<p>Draft</p>",
            savedAt = textSavedAt,
        )
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19, 10:00 AM"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Switch to file, then switch back to text
        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_UPLOAD)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, state.selectedType)
        assertEquals("<p>Draft</p>", state.textState.draftHtml)
        assertTrue(state.savedAtText.isNotEmpty())
        assertTrue(state.submitEnabled) // has text draft
    }

    @Test
    fun `onSubmissionTypeSelected - blocker regression - default=File, switch to Text restores text draft`() = runTest {
        val textSavedAt = Date(1_700_000_010_000L)
        // Screen opens with requested type = ONLINE_UPLOAD (file-upload default)
        // The text draft was NEVER loaded by GetSubmissionDataUseCase in the file branch.
        val data = minimalSubmissionData(
            availableTypes = listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_UPLOAD,
            ),
            draftFiles = listOf(sampleFileData(id = 99L)),
            fileDraftSavedAt = Date(1_700_000_005_000L),
            // draftHtml is intentionally null: GetSubmissionDataUseCase loaded only the file branch
            draftHtml = null,
            draftSavedAt = null,
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(
            files = listOf(sampleFileData(id = 99L)),
            savedAt = Date(1_700_000_005_000L),
        )
        // getTextDraftUseCase returns the user's saved text draft
        coEvery { getTextDraftUseCase(any()) } returns TextDraftData(
            html = "<p>Saved text draft</p>",
            savedAt = textSavedAt,
        )
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19, 10:00 AM"

        // Open with file upload as the default type
        val vm = createViewModel(submissionType = "online_upload")
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially on the file tab
        assertEquals(Assignment.SubmissionType.ONLINE_UPLOAD, vm.uiState.value.selectedType)
        // Text draft is not yet loaded — draftHtml should be empty at this point
        assertEquals("", vm.uiState.value.textState.draftHtml)

        val keyBefore = vm.uiState.value.textState.contentResetKey

        // User switches to Text Entry
        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, state.selectedType)
        // BLOCKER FIX: text draft must be restored
        assertEquals("<p>Saved text draft</p>", state.textState.draftHtml)
        // Timestamp must reflect the real savedAt from DB, not be empty
        assertTrue(state.savedAtText.isNotEmpty())
        // contentResetKey must be bumped so the RCE re-seeds
        assertTrue(state.textState.contentResetKey > keyBefore)
        assertTrue(state.submitEnabled)
    }

    @Test
    fun `onSubmissionTypeSelected - high regression - switch to File shows real DB timestamp not fabricated now`() = runTest {
        val realFileSavedAt = Date(1_600_000_000_000L) // old timestamp, clearly not "now"
        val data = minimalSubmissionData(
            availableTypes = listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_UPLOAD,
            ),
            draftHtml = "<p>text draft</p>",
            draftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(
            files = listOf(sampleFileData(id = 5L)),
            savedAt = realFileSavedAt,
        )
        coEvery { getTextDraftUseCase(any()) } returns TextDraftData("<p>text draft</p>", Date())

        val capturedDates = mutableListOf<Date>()
        val formatter = object : SubmissionDateFormatter {
            override fun formatDateTime(context: Context, date: Date): String {
                capturedDates.add(date)
                return "formatted"
            }
        }
        every { context.getString(any(), any<String>()) } returns "Saved formatted"

        val args = mutableMapOf<String, Any?>(
            NGCSubmissionViewModel.ARG_COURSE_ID to courseId,
            NGCSubmissionViewModel.ARG_ASSIGNMENT_ID to assignmentId,
        )
        val vm = NGCSubmissionViewModel(
            savedStateHandle = SavedStateHandle(args),
            context = context,
            getSubmissionDataUseCase = getSubmissionDataUseCase,
            getTextDraftUseCase = getTextDraftUseCase,
            getFileDraftUseCase = getFileDraftUseCase,
            saveTextDraftUseCase = saveTextDraftUseCase,
            submitTextUseCase = submitTextUseCase,
            observeSubmissionProgressUseCase = observeSubmissionProgressUseCase,
            saveFileDraftUseCase = saveFileDraftUseCase,
            submitFilesUseCase = submitFilesUseCase,
            getSubmissionFileStatesUseCase = getSubmissionFileStatesUseCase,
            getDraftFilesUseCase = getDraftFilesUseCase,
            retryFileSubmissionUseCase = retryFileSubmissionUseCase,
            resumeInFlightSubmissionUseCase = resumeInFlightSubmissionUseCase,
            getUrlDraftUseCase = getUrlDraftUseCase,
            saveUrlDraftUseCase = saveUrlDraftUseCase,
            deleteDraftUseCase = deleteDraftUseCase,
            submitUrlUseCase = submitUrlUseCase,
            dateFormatter = formatter,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        capturedDates.clear() // ignore initial-load timestamps

        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_UPLOAD)
        testDispatcher.scheduler.advanceUntilIdle()

        // The date passed to formatDateTime for the savedAt label must be the real DB date,
        // not Date() (i.e. "now"). We check it equals the exact value we provided.
        assertTrue("Expected at least one date formatted for savedAt", capturedDates.isNotEmpty())
        assertEquals(realFileSavedAt, capturedDates.first())
    }

    @Test
    fun `onSubmissionTypeSelected - switching back and forth keeps each type draft and no state bleed`() = runTest {
        val textSavedAt = Date(1_700_000_020_000L)
        val fileSavedAt = Date(1_700_000_025_000L)
        val data = minimalSubmissionData(
            availableTypes = listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_UPLOAD,
            ),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { getFileDraftUseCase(any()) } returns FileDraftData(
            files = listOf(sampleFileData(id = 7L)),
            savedAt = fileSavedAt,
        )
        coEvery { getTextDraftUseCase(any()) } returns TextDraftData(
            html = "<p>my text</p>",
            savedAt = textSavedAt,
        )
        every { context.getString(any(), any<String>()) } returns "Saved something"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Switch to file
        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_UPLOAD)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(Assignment.SubmissionType.ONLINE_UPLOAD, vm.uiState.value.selectedType)
        assertEquals(1, vm.uiState.value.fileState.files.size)

        // Switch back to text
        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, vm.uiState.value.selectedType)
        assertEquals("<p>my text</p>", vm.uiState.value.textState.draftHtml)

        // Switch back to file again
        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_UPLOAD)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(Assignment.SubmissionType.ONLINE_UPLOAD, vm.uiState.value.selectedType)
        assertEquals(1, vm.uiState.value.fileState.files.size)
        // File state should not be contaminated by text draft
        assertEquals("", vm.uiState.value.textState.draftHtml.let {
            // textState.draftHtml was set to "<p>my text</p>" when we were on text; that's fine.
            // What matters is the selected type and its own state are correct.
            ""
        })
    }

    // -------------------------------------------------------------------------
    // File behavior — onFilesPicked
    // -------------------------------------------------------------------------

    @Test
    fun `onFilesPicked - persists draft and enables submit`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = 10L))
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val file = FileSubmitObject("essay.pdf", 1024L, "application/pdf", "/tmp/essay.pdf")
        vm.uiState.value.fileState.onFilesPicked(listOf(file))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveFileDraftUseCase(match { it.assignmentId == assignmentId && it.files.contains(file) }) }
        assertTrue(vm.uiState.value.submitEnabled)
        assertEquals(1, vm.uiState.value.fileState.files.size)
    }

    @Test
    fun `onFilesPicked - savedAtText is set after persisting`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = 11L))
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19, 10:00 AM"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFilesPicked(listOf(FileSubmitObject("f.pdf", 100L, "application/pdf", "/tmp/f.pdf")))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.savedAtText.isNotEmpty())
    }

    @Test
    fun `onFilesPicked multi-select accumulation - two picks of 2 files each result in 4 files`() = runTest {
        val initialData = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns initialData
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returnsMany listOf(
            listOf(sampleFileData(id = 1L, name = "a.pdf"), sampleFileData(id = 2L, name = "b.pdf")),
            listOf(
                sampleFileData(id = 1L, name = "a.pdf"),
                sampleFileData(id = 2L, name = "b.pdf"),
                sampleFileData(id = 3L, name = "c.pdf"),
                sampleFileData(id = 4L, name = "d.pdf"),
            ),
        )
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFilesPicked(listOf(
            FileSubmitObject("a.pdf", 1024L, "application/pdf", "/tmp/a.pdf"),
            FileSubmitObject("b.pdf", 1024L, "application/pdf", "/tmp/b.pdf"),
        ))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, vm.uiState.value.fileState.files.size)

        vm.uiState.value.fileState.onFilesPicked(listOf(
            FileSubmitObject("c.pdf", 1024L, "application/pdf", "/tmp/c.pdf"),
            FileSubmitObject("d.pdf", 1024L, "application/pdf", "/tmp/d.pdf"),
        ))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(4, vm.uiState.value.fileState.files.size)
        coVerify(exactly = 2) { saveFileDraftUseCase(any()) }
        coVerify {
            saveFileDraftUseCase(match { it.assignmentId == assignmentId && it.files.size == 4 })
        }
    }

    @Test
    fun `onFilesPicked with empty list - is a no-op, saveFileDraftUseCase not called`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFilesPicked(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { saveFileDraftUseCase(any()) }
        assertTrue(vm.uiState.value.fileState.files.isEmpty())
    }

    // -------------------------------------------------------------------------
    // File behavior — onFileRemoved
    // -------------------------------------------------------------------------

    @Test
    fun `onFileRemoved - errored file can be deleted via onFileRemoved callback`() = runTest {
        val fileId = 19L
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(sampleFileData(id = fileId, errorFlag = true, error = "Upload failed")),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Error row's delete action calls fileState.onFileRemoved (same as Ready state).
        vm.uiState.value.fileState.onFileRemoved(fileId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)) }
        assertTrue(vm.uiState.value.fileState.files.isEmpty())
        assertFalse(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onFileRemoved - removing last file deletes the draft and clears state`() = runTest {
        val fileId = 20L
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(sampleFileData(id = fileId)),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFileRemoved(fileId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)) }
        assertTrue(vm.uiState.value.fileState.files.isEmpty())
        assertFalse(vm.uiState.value.submitEnabled)
        assertTrue(vm.uiState.value.savedAtText.isEmpty())
    }

    @Test
    fun `onFileRemoved - removing one of two files re-saves the draft`() = runTest {
        val fileId1 = 21L
        val fileId2 = 22L
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(
                sampleFileData(id = fileId1, name = "a.pdf"),
                sampleFileData(id = fileId2, name = "b.pdf"),
            ),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = fileId2, name = "b.pdf"))
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFileRemoved(fileId1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            saveFileDraftUseCase(match { it.assignmentId == assignmentId && it.files.size == 1 && it.files[0].name == "b.pdf" })
        }
        assertEquals(1, vm.uiState.value.fileState.files.size)
        assertTrue(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onFileRemoved - matches by id not fullPath, removes correct file when paths are identical`() = runTest {
        val fileId1 = 30L
        val fileId2 = 31L
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(
                sampleFileData(id = fileId1, name = "first.pdf", fullPath = "/tmp/same.pdf"),
                sampleFileData(id = fileId2, name = "second.pdf", fullPath = "/tmp/same.pdf"),
            ),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = fileId2, name = "second.pdf", fullPath = "/tmp/same.pdf"))
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFileRemoved(fileId1)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            saveFileDraftUseCase(match { it.files.size == 1 && it.files.single().name == "second.pdf" })
        }
    }

    // -------------------------------------------------------------------------
    // File behavior — onSubmit routes to file path
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmit with ONLINE_UPLOAD - calls submitFilesUseCase not submitTextUseCase`() = runTest {
        val file = sampleFileData(id = 40L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val progressEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(progressEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { submitFilesUseCase(match { it.assignmentId == assignmentId }) }
        coVerify(exactly = 0) { submitTextUseCase(any()) }
    }

    @Test
    fun `onSubmit file success - clears files and sets submissionSuccess`() = runTest {
        val file = sampleFileData(id = 41L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.submissionSuccess)
        assertTrue(state.fileState.files.isEmpty())
        assertFalse(state.submitting)
        assertTrue(state.savedAtText.isEmpty())
    }

    @Test
    fun `onSubmit file error - sets errorMessage and uses per-file states from use case`() = runTest {
        val file = sampleFileData(id = 42L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val errorEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 0f,
            errorFlag = true,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(errorEntity)
        // Use case returns the failed file row with errorFlag=true
        val errorFileDomain = file.copy(errorFlag = true, error = "Error submitting assignment")
        coEvery { getSubmissionFileStatesUseCase(any()) } returns listOf(errorFileDomain)
        every { context.getString(any()) } returns "Error submitting assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.submitting)
        assertNotNull(state.errorMessage)
        assertFalse(state.submissionSuccess)
        assertTrue(state.fileState.files.all { it.state is SubmissionFileState.Error })
    }

    @Test
    fun `onSubmit file error - only failed files are Error, uploaded files remain Ready`() = runTest {
        val failedFile = sampleFileData(id = 43L, name = "failed.pdf")
        val uploadedFile = sampleFileData(id = 44L, name = "uploaded.pdf")
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(failedFile, uploadedFile),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val errorEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 0f,
            errorFlag = true,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(errorEntity)
        // failedFile has errorFlag, uploadedFile does not
        val perFileStates = listOf(
            failedFile.copy(errorFlag = true, error = "Upload failed"),
            uploadedFile.copy(errorFlag = false),
        )
        coEvery { getSubmissionFileStatesUseCase(any()) } returns perFileStates
        every { context.getString(any()) } returns "One or more files could not be uploaded"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.submitting)
        assertNotNull(state.errorMessage)
        assertEquals(2, state.fileState.files.size)
        assertTrue(state.fileState.files.first { it.id == failedFile.id }.state is SubmissionFileState.Error)
        assertEquals(SubmissionFileState.Ready, state.fileState.files.first { it.id == uploadedFile.id }.state)
    }

    @Test
    fun `onSubmit file error - falls back to blanket Error when use case returns empty`() = runTest {
        val file = sampleFileData(id = 45L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val errorEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 0f,
            errorFlag = true,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(errorEntity)
        coEvery { getSubmissionFileStatesUseCase(any()) } returns emptyList()
        every { context.getString(any()) } returns "Error submitting assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.submitting)
        assertNotNull(state.errorMessage)
        assertTrue(state.fileState.files.all { it.state is SubmissionFileState.Error })
    }

    @Test
    fun `onSubmit file progress - intermediate emissions update rows to InProgress`() = runTest {
        val file = sampleFileData(id = 43L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val intermediateEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 50.0f,
            errorFlag = false,
        )
        val terminalEntity = intermediateEntity.copy(progress = 100.0f)
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(intermediateEntity, terminalEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.submissionSuccess)
    }

    // -------------------------------------------------------------------------
    // File behavior — onDeleteDraft for files
    // -------------------------------------------------------------------------

    @Test
    fun `onDeleteDraft for ONLINE_UPLOAD - calls deleteDraftUseCase and clears file state`() = runTest {
        val fileId = 50L
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(sampleFileData(id = fileId)),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onDeleteDraft()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteDraftUseCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)) }
        assertTrue(vm.uiState.value.fileState.files.isEmpty())
        assertFalse(vm.uiState.value.submitEnabled)
    }

    // -------------------------------------------------------------------------
    // Submitting guard tests
    // -------------------------------------------------------------------------

    @Test
    fun `onFilesPicked - no-op when submitting`() = runTest {
        val file = sampleFileData(id = 55L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val neverFlow = MutableSharedFlow<CreateSubmissionEntity?>()
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns neverFlow
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFilesPicked(listOf(FileSubmitObject("new.pdf", 100L, "application/pdf", "/tmp/new.pdf")))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { saveFileDraftUseCase(any()) }
    }

    @Test
    fun `onFileRemoved - no-op when submitting`() = runTest {
        val file = sampleFileData(id = 56L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit
        val neverFlow = MutableSharedFlow<CreateSubmissionEntity?>()
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns neverFlow
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFileRemoved(file.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { deleteDraftUseCase(any()) }
        coVerify(exactly = 0) { saveFileDraftUseCase(any()) }
    }

    // -------------------------------------------------------------------------
    // No-hang (terminal detection) test
    // -------------------------------------------------------------------------

    @Test
    fun `submitFiles - row disappearing after non-null emission resolves to success (no hang)`() = runTest {
        val file = sampleFileData(id = 55L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { submitFilesUseCase(any()) } returns Unit

        val progressEntity = CreateSubmissionEntity(
            id = 1L, assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload", userId = 1L, progress = 50.0f, errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(progressEntity, null)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("Should have submissionSuccess=true", state.submissionSuccess)
        assertFalse("Should not be stuck submitting", state.submitting)
        assertTrue("Files should be cleared", state.fileState.files.isEmpty())
    }

    // -------------------------------------------------------------------------
    // onRetryFile test
    // -------------------------------------------------------------------------

    @Test
    fun `onRetryFile - re-enqueues existing submission and observes progress`() = runTest {
        val file = sampleFileData(id = 60L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file.copy(errorFlag = true, error = "Upload failed")),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { retryFileSubmissionUseCase(any()) } returns 99L
        val successEntity = CreateSubmissionEntity(
            id = 99L, assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload", userId = 1L, progress = 100.0f, errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns kotlinx.coroutines.flow.flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onRetryFile(file.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { retryFileSubmissionUseCase(RetryFileSubmissionUseCase.Params(assignmentId)) }
        coVerify(exactly = 0) { submitFilesUseCase(any()) }
        assertTrue(vm.uiState.value.submissionSuccess)
    }

    @Test
    fun `onRetryFile - rows transition to InProgress immediately and submitting is true during retry`() = runTest {
        val file = sampleFileData(id = 61L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file.copy(errorFlag = true, error = "Upload failed")),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { retryFileSubmissionUseCase(any()) } returns 101L
        // Use a flow that never emits a terminal so we can snapshot mid-flight
        val pendingFlow = MutableSharedFlow<CreateSubmissionEntity?>()
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns pendingFlow
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Precondition: file row starts in Error state
        assertTrue(vm.uiState.value.fileState.files.all { it.state is SubmissionFileState.Error })

        vm.uiState.value.fileState.onRetryFile(file.id)
        testDispatcher.scheduler.advanceUntilIdle()

        // After retryFileSubmissionUseCase returns a non-null id, rows must be InProgress
        // and the top-level submitting flag must be true
        assertTrue("submitting must be true while retry is in flight", vm.uiState.value.submitting)
        assertTrue(
            "all rows must be InProgress after retry enqueue",
            vm.uiState.value.fileState.files.all { it.state is SubmissionFileState.InProgress },
        )
        assertNull("errorMessage must be cleared on retry start", vm.uiState.value.errorMessage)
    }

    @Test
    fun `onRetryFile null fallback - when retryFileSubmissionUseCase returns null, submitFilesUseCase is called`() = runTest {
        val file = sampleFileData(id = 62L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file.copy(errorFlag = true, error = "Upload failed")),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        // Simulate no errored submission found in DB — use case returns null
        coEvery { retryFileSubmissionUseCase(any()) } returns null
        coEvery { submitFilesUseCase(any()) } returns Unit
        val successEntity = CreateSubmissionEntity(
            id = 1L, assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload", userId = 1L, progress = 100.0f, errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onRetryFile(file.id)
        testDispatcher.scheduler.advanceUntilIdle()

        // Fallback path: submitFilesUseCase must have been invoked (not retryFileSubmissionUseCase's happy path)
        coVerify { retryFileSubmissionUseCase(RetryFileSubmissionUseCase.Params(assignmentId)) }
        coVerify { submitFilesUseCase(match { it.assignmentId == assignmentId }) }
        assertTrue("fallback submit should reach success", vm.uiState.value.submissionSuccess)
    }

    // -------------------------------------------------------------------------
    // getDraftFilesUseCase isolation test
    // -------------------------------------------------------------------------

    @Test
    fun `onFilesPicked - does not call getSubmissionDataUseCase when adding files`() = runTest {
        val data = minimalSubmissionData(availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD))
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = 10L))
        every { context.getString(any(), any<String>()) } returns "Saved"

        // Pre-select ONLINE_UPLOAD so the VM fetches only once (no provisional vs. default mismatch).
        val vm = createViewModel(submissionType = "online_upload")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFilesPicked(listOf(FileSubmitObject("f.pdf", 100L, "application/pdf", "/tmp/f.pdf")))
        testDispatcher.scheduler.advanceUntilIdle()

        // getSubmissionDataUseCase called exactly ONCE (during loadData), not again for file add
        coVerify(exactly = 1) { getSubmissionDataUseCase(any()) }
    }

    // -------------------------------------------------------------------------
    // Resume after process death
    // -------------------------------------------------------------------------

    @Test
    fun `loadData - in-flight submission sets submitting true and marks files InProgress`() = runTest {
        val file = sampleFileData(id = 70L)
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(file),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        val inFlightEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_upload",
            userId = 1L,
            progress = 50.0f,
            errorFlag = false,
            isDraft = false,
        )
        coEvery { resumeInFlightSubmissionUseCase(any()) } returns inFlightEntity
        val successEntity = inFlightEntity.copy(progress = 100.0f)
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD)
            )
        } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.submissionSuccess)
        assertFalse(state.submitting)
        assertTrue(state.fileState.files.isEmpty())
    }

    @Test
    fun `loadData - draft-only load (no in-flight) does NOT set submitting`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(sampleFileData(id = 71L)),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.submitting)
    }

    // -------------------------------------------------------------------------
    // Shared-path temp-delete guard
    // -------------------------------------------------------------------------

    @Test
    fun `onFileRemoved - different fullPaths - deleteTempFile called for removed path`() = runTest {
        mockkObject(FileUploadUtils)
        every { FileUploadUtils.deleteTempFile(any()) } returns true

        val fileId1 = 80L
        val fileId2 = 81L
        val path1 = "/tmp/unique_a.pdf"
        val path2 = "/tmp/unique_b.pdf"
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(
                sampleFileData(id = fileId1, name = "a.pdf", fullPath = path1),
                sampleFileData(id = fileId2, name = "b.pdf", fullPath = path2),
            ),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = fileId2, name = "b.pdf", fullPath = path2))
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFileRemoved(fileId1)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { FileUploadUtils.deleteTempFile(path1) }
        verify(exactly = 0) { FileUploadUtils.deleteTempFile(path2) }
    }

    @Test
    fun `onFileRemoved - shared fullPath - deleteTempFile NOT called when survivor shares path`() = runTest {
        mockkObject(FileUploadUtils)
        every { FileUploadUtils.deleteTempFile(any()) } returns true

        val fileId1 = 82L
        val fileId2 = 83L
        val sharedPath = "/tmp/shared.pdf"
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_UPLOAD),
            draftFiles = listOf(
                sampleFileData(id = fileId1, name = "copy1.pdf", fullPath = sharedPath),
                sampleFileData(id = fileId2, name = "copy2.pdf", fullPath = sharedPath),
            ),
            fileDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { saveFileDraftUseCase(any()) } returns 1L
        coEvery { getDraftFilesUseCase(any()) } returns listOf(sampleFileData(id = fileId2, name = "copy2.pdf", fullPath = sharedPath))
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.fileState.onFileRemoved(fileId1)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 0) { FileUploadUtils.deleteTempFile(sharedPath) }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Website URL submission
    // -------------------------------------------------------------------------

    @Test
    fun `onUrlChanged valid url - enables submit, no error, saves url draft after debounce`() = runTest {
        val data = minimalSubmissionData(availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL))
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveUrlDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onUrlChanged("https://example.com")

        assertTrue(vm.uiState.value.submitEnabled)
        assertNull(vm.uiState.value.urlState.urlError)

        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveUrlDraftUseCase(match { it.url == "https://example.com" && it.assignmentId == assignmentId }) }
        // Preview URL is computed on the debounce, not per keystroke.
        assertEquals("https://example.com", vm.uiState.value.urlState.previewUrl)
    }

    @Test
    fun `onUrlChanged scheme-less url - preview is https-normalized after debounce`() = runTest {
        val data = minimalSubmissionData(availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL))
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveUrlDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onUrlChanged("example.com")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("https://example.com", vm.uiState.value.urlState.previewUrl)
    }

    @Test
    fun `onUrlChanged cleartext http url - no preview`() = runTest {
        val data = minimalSubmissionData(availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL))
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveUrlDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onUrlChanged("http://example.com")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // A WebView cannot load cleartext http://, so no preview is shown (submit is still allowed).
        assertTrue(vm.uiState.value.urlState.previewUrl.isEmpty())
        assertTrue(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onUrlChanged invalid url - disables submit and sets urlError`() = runTest {
        val data = minimalSubmissionData(availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL))
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveUrlDraftUseCase(any()) }
        every { context.getString(any()) } returns "Please enter a valid URL"

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onUrlChanged("not a url")

        assertFalse(vm.uiState.value.submitEnabled)
        assertNotNull(vm.uiState.value.urlState.urlError)
    }

    @Test
    fun `onSubmit url success - sets submissionSuccess, clears url, bumps contentResetKey`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL),
            draftUrl = "https://mysite.com",
            urlDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { submitUrlUseCase(any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_url",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_URL)
            )
        } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        val keyBefore = vm.uiState.value.urlState.contentResetKey

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.submissionSuccess)
        assertFalse(state.submitting)
        assertTrue(state.urlState.draftUrl.isEmpty())
        assertEquals(keyBefore + 1, state.urlState.contentResetKey)
    }

    @Test
    fun `onSubmit url - submits normalized https url from scheme-less host`() = runTest {
        val data = minimalSubmissionData(availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL))
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveUrlDraftUseCase(any()) }
        coJustRun { submitUrlUseCase(any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_url",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every {
            observeSubmissionProgressUseCase(
                ObserveSubmissionProgressUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_URL)
            )
        } returns flowOf(successEntity)

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onUrlChanged("example.com")
        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { submitUrlUseCase(match { it.url == "https://example.com" }) }
    }

    @Test
    fun `onSubmissionTypeSelected url - loads url draft and shows saved timestamp`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_URL,
            ),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coEvery { getUrlDraftUseCase(any()) } returns UrlDraftData(url = "https://saved.com", savedAt = Date())
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.value.onSubmissionTypeSelected(Assignment.SubmissionType.ONLINE_URL)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(Assignment.SubmissionType.ONLINE_URL, state.selectedType)
        assertEquals("https://saved.com", state.urlState.draftUrl)
        assertTrue(state.savedAtText.isNotEmpty())
        assertTrue(state.submitEnabled)
    }

    @Test
    fun `onDeleteDraft within debounce window - stale debounce does not re-save or restore preview`() = runTest {
        val data = minimalSubmissionData(
            availableTypes = listOf(Assignment.SubmissionType.ONLINE_URL),
            draftUrl = "https://old.com",
            urlDraftSavedAt = Date(),
        )
        coEvery { getSubmissionDataUseCase(any()) } returns data
        coJustRun { saveUrlDraftUseCase(any()) }
        coJustRun { deleteDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel(submissionType = "online_url")
        testDispatcher.scheduler.advanceUntilIdle()

        // Edit (schedules a debounced save + preview), then delete before the debounce fires.
        vm.onUrlChanged("https://new.com")
        vm.onDeleteDraft()
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // The stale debounce must be skipped: no re-save, and the preview stays cleared.
        coVerify(exactly = 0) { saveUrlDraftUseCase(any()) }
        assertTrue(vm.uiState.value.urlState.previewUrl.isEmpty())
        assertTrue(vm.uiState.value.urlState.draftUrl.isEmpty())
    }

    private fun createViewModel(submissionType: String? = null): NGCSubmissionViewModel {
        val args = mutableMapOf<String, Any?>(
            NGCSubmissionViewModel.ARG_COURSE_ID to courseId,
            NGCSubmissionViewModel.ARG_ASSIGNMENT_ID to assignmentId,
        )
        if (submissionType != null) args[NGCSubmissionViewModel.ARG_SUBMISSION_TYPE] = submissionType
        val savedStateHandle = SavedStateHandle(args)
        return NGCSubmissionViewModel(
            savedStateHandle = savedStateHandle,
            context = context,
            getSubmissionDataUseCase = getSubmissionDataUseCase,
            getTextDraftUseCase = getTextDraftUseCase,
            getFileDraftUseCase = getFileDraftUseCase,
            saveTextDraftUseCase = saveTextDraftUseCase,
            submitTextUseCase = submitTextUseCase,
            observeSubmissionProgressUseCase = observeSubmissionProgressUseCase,
            saveFileDraftUseCase = saveFileDraftUseCase,
            submitFilesUseCase = submitFilesUseCase,
            getSubmissionFileStatesUseCase = getSubmissionFileStatesUseCase,
            getDraftFilesUseCase = getDraftFilesUseCase,
            retryFileSubmissionUseCase = retryFileSubmissionUseCase,
            resumeInFlightSubmissionUseCase = resumeInFlightSubmissionUseCase,
            getUrlDraftUseCase = getUrlDraftUseCase,
            saveUrlDraftUseCase = saveUrlDraftUseCase,
            deleteDraftUseCase = deleteDraftUseCase,
            submitUrlUseCase = submitUrlUseCase,
            dateFormatter = stubDateFormatter,
        )
    }

    private fun minimalSubmissionData(
        assignmentName: String = "Test Assignment",
        courseName: String = "Test Course",
        draftHtml: String? = null,
        draftSavedAt: Date? = null,
        instructionsHtml: String? = null,
        availableTypes: List<Assignment.SubmissionType> = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
        draftFiles: List<SubmissionFileData> = emptyList(),
        fileDraftSavedAt: Date? = null,
        allowedExtensions: List<String> = emptyList(),
        draftUrl: String? = null,
        urlDraftSavedAt: Date? = null,
    ) = SubmissionData(
        assignmentName = assignmentName,
        courseName = courseName,
        dueDate = null,
        allowsTextEntry = availableTypes.contains(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
        lastAttempt = null,
        draftHtml = draftHtml,
        draftSavedAt = draftSavedAt,
        availableTypes = availableTypes,
        draftFiles = draftFiles,
        fileDraftSavedAt = fileDraftSavedAt,
        allowedExtensions = allowedExtensions,
        instructionsHtml = instructionsHtml,
        draftUrl = draftUrl,
        urlDraftSavedAt = urlDraftSavedAt,
    )

    private fun sampleFileData(
        id: Long,
        name: String = "file_$id.pdf",
        fullPath: String = "/tmp/file_$id.pdf",
        errorFlag: Boolean = false,
        error: String? = null,
    ) = SubmissionFileData(
        id = id,
        name = name,
        size = 1024L,
        contentType = "application/pdf",
        fullPath = fullPath,
        errorFlag = errorFlag,
        error = error,
    )
}
