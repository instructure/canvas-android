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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.ngc.features.submission.domain.DeleteTextDraftUseCase
import com.instructure.ngc.features.submission.domain.GetSubmissionDataUseCase
import com.instructure.ngc.features.submission.domain.ObserveSubmissionProgressUseCase
import com.instructure.ngc.features.submission.domain.SaveTextDraftUseCase
import com.instructure.ngc.features.submission.domain.SubmissionData
import com.instructure.ngc.features.submission.domain.SubmitTextUseCase
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
    private val saveTextDraftUseCase: SaveTextDraftUseCase = mockk()
    private val deleteTextDraftUseCase: DeleteTextDraftUseCase = mockk()
    private val submitTextUseCase: SubmitTextUseCase = mockk()
    private val observeSubmissionProgressUseCase: ObserveSubmissionProgressUseCase = mockk()

    // Stub date formatter so tests never call DateFormat.is24HourFormat(context).
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // Initial load
    // -------------------------------------------------------------------------

    @Test
    fun `Initial load - loading state is true before data arrives`() = runTest {
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } coAnswers {
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
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
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
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        every { context.getString(any(), any<String>()) } returns "Saved Jun 14, 9:33 PM"

        val initialKey = createViewModel().let { vm ->
            testDispatcher.scheduler.advanceUntilIdle()
            val state = vm.uiState.value
            assertEquals("<p>Draft text</p>", state.draftHtml)
            assertTrue(state.savedAtText.isNotEmpty())
            assertTrue(state.submitEnabled)
            state.contentResetKey
        }

        // Key must be > 0 (bumped once on successful load).
        assertTrue(initialKey > 0)
    }

    @Test
    fun `Initial load - submitEnabled is false when no draft`() = runTest {
        val data = minimalSubmissionData(draftHtml = null, draftSavedAt = null)
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.submitEnabled)
        assertTrue(vm.uiState.value.savedAtText.isEmpty())
    }

    @Test
    fun `Initial load - sets loadError on failure`() = runTest {
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } throws RuntimeException("network error")
        every { context.getString(any()) } returns "Failed to load assignment"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertNotNull(state.loadError)
        assertNotNull(state.onRetry)
    }

    // -------------------------------------------------------------------------
    // onTextChanged — immediate UiState update
    // -------------------------------------------------------------------------

    @Test
    fun `onTextChanged non-blank - updates draftHtml and submitEnabled immediately`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { saveTextDraftUseCase(any(), any(), any(), any()) }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>Hello world</p>")

        // draftHtml and submitEnabled update synchronously (no debounce on those).
        assertEquals("<p>Hello world</p>", vm.uiState.value.draftHtml)
        assertTrue(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onTextChanged blank html - clears submitEnabled immediately`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>Old draft</p>", draftSavedAt = Date())
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { deleteTextDraftUseCase(any()) }
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
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { saveTextDraftUseCase(any(), any(), any(), any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19, 10:00 AM"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>Hello world</p>")
        // saveTextDraftUseCase not called yet (within debounce window).
        coVerify(exactly = 0) { saveTextDraftUseCase(any(), any(), any(), any()) }

        // Advance past the 400 ms debounce.
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveTextDraftUseCase(any(), eq(assignmentId), any(), eq("<p>Hello world</p>")) }
        assertEquals("Saved Jun 19, 10:00 AM", vm.uiState.value.savedAtText)
    }

    @Test
    fun `onTextChanged blank html - deletes draft and clears savedAtText after debounce`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>Old draft</p>", draftSavedAt = Date())
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { deleteTextDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Send only a <br> which is considered blank
        vm.onTextChanged("<br>")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.savedAtText.isEmpty())
        assertFalse(vm.uiState.value.submitEnabled)
        coVerify { deleteTextDraftUseCase(assignmentId) }
    }

    @Test
    fun `onTextChanged empty string - deletes draft after debounce`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { deleteTextDraftUseCase(any()) }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteTextDraftUseCase(assignmentId) }
        assertFalse(vm.uiState.value.submitEnabled)
    }

    @Test
    fun `onTextChanged - only latest value is persisted when called rapidly`() = runTest {
        val data = minimalSubmissionData()
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { saveTextDraftUseCase(any(), any(), any(), any()) }

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onTextChanged("<p>A</p>")
        vm.onTextChanged("<p>AB</p>")
        vm.onTextChanged("<p>ABC</p>")
        advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // Debounce means only the last value triggers a save.
        coVerify(exactly = 1) { saveTextDraftUseCase(any(), any(), any(), any()) }
        coVerify { saveTextDraftUseCase(any(), eq(assignmentId), any(), eq("<p>ABC</p>")) }
    }

    // -------------------------------------------------------------------------
    // onDeleteDraft
    // -------------------------------------------------------------------------

    @Test
    fun `onDeleteDraft - clears draftHtml, savedAtText, submitEnabled, and bumps contentResetKey`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>Draft</p>", draftSavedAt = Date())
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { deleteTextDraftUseCase(any()) }
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val keyBefore = vm.uiState.value.contentResetKey

        vm.onDeleteDraft()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.draftHtml.isEmpty())
        assertTrue(state.savedAtText.isEmpty())
        assertFalse(state.submitEnabled)
        assertEquals(keyBefore + 1, state.contentResetKey)
        coVerify { deleteTextDraftUseCase(assignmentId) }
    }

    // -------------------------------------------------------------------------
    // onSubmit — success path
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmit success - progress 100 sets submissionSuccess, clears draft, and bumps contentResetKey`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { submitTextUseCase(any(), any(), any(), any(), any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every { observeSubmissionProgressUseCase(assignmentId) } returns flowOf(successEntity)
        every { context.getString(any(), any<String>()) } returns "Saved Jun 19"

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val keyBefore = vm.uiState.value.contentResetKey

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.submissionSuccess)
        assertFalse(state.submitting)
        assertTrue(state.draftHtml.isEmpty())
        assertTrue(state.savedAtText.isEmpty())
        assertFalse(state.submitEnabled)
        assertEquals(keyBefore + 1, state.contentResetKey)
    }

    @Test
    fun `onSubmit - reads current draftHtml from UiState`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { saveTextDraftUseCase(any(), any(), any(), any()) }
        coJustRun { submitTextUseCase(any(), any(), any(), any(), any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every { observeSubmissionProgressUseCase(assignmentId) } returns flowOf(successEntity)

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate user typing more before submitting (bypass debounce for the direct field update).
        vm.onTextChanged("<p>Updated text</p>")

        vm.onSubmit()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { submitTextUseCase(any(), eq(assignmentId), any(), eq("<p>Updated text</p>"), any()) }
    }

    // -------------------------------------------------------------------------
    // onSubmit — error path
    // -------------------------------------------------------------------------

    @Test
    fun `onSubmit error - errorFlag sets errorMessage`() = runTest {
        val data = minimalSubmissionData(draftHtml = "<p>My submission</p>")
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { submitTextUseCase(any(), any(), any(), any(), any()) }
        val errorEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 0f,
            errorFlag = true,
        )
        every { observeSubmissionProgressUseCase(assignmentId) } returns flowOf(errorEntity)
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
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coEvery { submitTextUseCase(any(), any(), any(), any(), any()) } throws RuntimeException("submit failed")
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
        coEvery { getSubmissionDataUseCase(courseId, assignmentId) } returns data
        coJustRun { submitTextUseCase(any(), any(), any(), any(), any()) }
        val successEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = "online_text_entry",
            userId = 1L,
            progress = 100.0f,
            errorFlag = false,
        )
        every { observeSubmissionProgressUseCase(assignmentId) } returns flowOf(successEntity)
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
    // Helpers
    // -------------------------------------------------------------------------

    private fun createViewModel(): NGCSubmissionViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                NGCSubmissionViewModel.ARG_COURSE_ID to courseId,
                NGCSubmissionViewModel.ARG_ASSIGNMENT_ID to assignmentId,
            )
        )
        return NGCSubmissionViewModel(
            savedStateHandle = savedStateHandle,
            context = context,
            getSubmissionDataUseCase = getSubmissionDataUseCase,
            saveTextDraftUseCase = saveTextDraftUseCase,
            deleteTextDraftUseCase = deleteTextDraftUseCase,
            submitTextUseCase = submitTextUseCase,
            observeSubmissionProgressUseCase = observeSubmissionProgressUseCase,
            dateFormatter = stubDateFormatter,
        )
    }

    private fun minimalSubmissionData(
        assignmentName: String = "Test Assignment",
        courseName: String = "Test Course",
        draftHtml: String? = null,
        draftSavedAt: Date? = null,
    ) = SubmissionData(
        assignmentName = assignmentName,
        courseName = courseName,
        dueDate = null,
        allowsTextEntry = true,
        lastAttempt = null,
        draftHtml = draftHtml,
        draftSavedAt = draftSavedAt,
    )
}
