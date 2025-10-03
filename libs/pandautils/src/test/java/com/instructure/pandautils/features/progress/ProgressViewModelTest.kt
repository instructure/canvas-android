/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.progress

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ProgressViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    private val stateHandle: SavedStateHandle = mockk(relaxed = true)
    private val progressApi: ProgressAPI.ProgressInterface = mockk(relaxed = true)
    private val progressPreferences: ProgressPreferences = mockk(relaxed = true)

    private lateinit var viewModel: ProgressViewModel

    @Before
    fun setup() {

        every { stateHandle.get<Long>("progressId") } returns 1L
        every { stateHandle.get<String>("title") } returns "Title"
        every { stateHandle.get<String>("progressTitle") } returns "Publishing"
        every { stateHandle.get<String>("note") } returns "Note"
    }

    fun teardown() {
    }

    @Test
    fun `Emit progress updates`() {
        var progress = Progress(
            id = 1L,
            workflowState = "running",
            completion = 0f
        )
        var expectedState = ProgressUiState(
            title = "Title",
            progressTitle = "Publishing",
            progress = 0f,
            note = "Note",
            state = ProgressState.RUNNING,
        )

        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(progress)

        viewModel = createViewModel()
        assertEquals(expectedState, viewModel.uiState.value)

        progress = progress.copy(completion = 11.11f)
        expectedState = expectedState.copy(progress = 11.11f)

        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(progress)

        testDispatcher.scheduler.advanceTimeBy(510)
        assertEquals(expectedState, viewModel.uiState.value)

        progress = progress.copy(completion = 100f, workflowState = "completed")
        expectedState = expectedState.copy(progress = 100f, state = ProgressState.COMPLETED)

        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(progress)

        testDispatcher.scheduler.advanceTimeBy(510)
        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Emit success state`() {
        val progress = Progress(
            id = 1L,
            workflowState = "completed",
            completion = 100f
        )
        val expectedState = ProgressUiState(
            title = "Title",
            progressTitle = "Publishing",
            progress = 100f,
            note = "Note",
            state = ProgressState.COMPLETED,
        )

        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(progress)

        viewModel = createViewModel()

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Emit failed state on exception`() {
        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Fail()

        viewModel = createViewModel()

        assertEquals(ProgressState.FAILED, viewModel.uiState.value.state)
    }

    @Test
    fun `Cancel event sends close action`() = runTest {
        val progress = Progress(
            id = 1L,
            workflowState = "failed",
            completion = 11.11f
        )
        coEvery { progressApi.getProgress(any(), any()) } returns DataResult.Success(progress)
        coEvery {
            progressApi.cancelProgress(
                any(),
                any()
            )
        } returns DataResult.Success(progress.copy(workflowState = "failed"))

        viewModel = createViewModel()

        viewModel.handleAction(ProgressAction.Cancel)
        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        assert(events.last() is ProgressViewModelAction.Close)

        coVerify {
            progressApi.cancelProgress("1", any())
        }
    }

    private fun createViewModel() = ProgressViewModel(stateHandle, progressApi, progressPreferences)

}
