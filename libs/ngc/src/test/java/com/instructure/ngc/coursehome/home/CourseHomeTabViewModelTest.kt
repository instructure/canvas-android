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

package com.instructure.ngc.coursehome.home

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Page
import com.instructure.ngc.features.coursehome.CourseHomeViewModel
import com.instructure.ngc.features.coursehome.home.CourseHomeTabViewModel
import com.instructure.pandautils.domain.usecase.pages.LoadCourseFrontPageUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseHomeTabViewModelTest {

    private val loadCourseFrontPageUseCase: LoadCourseFrontPageUseCase = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state is loading`() = runTest {
        coEvery { loadCourseFrontPageUseCase(any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            Page(body = "<p>Hello</p>")
        }

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isError)
        assertNull(viewModel.uiState.value.content)
    }

    @Test
    fun `Successful load exposes front page body`() = runTest {
        coEvery { loadCourseFrontPageUseCase(any()) } returns Page(body = "<p>Welcome</p>")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals("<p>Welcome</p>", state.content)
    }

    @Test
    fun `Blank front page body yields null content`() = runTest {
        coEvery { loadCourseFrontPageUseCase(any()) } returns Page(body = "")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.content)
    }

    @Test
    fun `Null page response yields null content`() = runTest {
        coEvery { loadCourseFrontPageUseCase(any()) } returns null

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.content)
    }

    @Test
    fun `Failed load sets error state and records exception`() = runTest {
        val error = RuntimeException("Boom")
        coEvery { loadCourseFrontPageUseCase(any()) } throws error

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isError)
        assertNull(state.content)
        verify { crashlytics.recordException(error) }
    }

    private fun getViewModel(courseId: Long = 1L): CourseHomeTabViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(CourseHomeViewModel.ARG_COURSE_ID to courseId))
        return CourseHomeTabViewModel(
            savedStateHandle,
            loadCourseFrontPageUseCase,
            crashlytics,
        )
    }
}