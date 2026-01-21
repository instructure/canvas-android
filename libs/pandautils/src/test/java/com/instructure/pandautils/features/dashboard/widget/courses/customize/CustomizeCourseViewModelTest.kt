/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courses.customize

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.course.SetCourseColorParams
import com.instructure.pandautils.domain.usecase.course.SetCourseColorUseCase
import com.instructure.pandautils.domain.usecase.course.SetCourseNicknameParams
import com.instructure.pandautils.domain.usecase.course.SetCourseNicknameUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CustomizeCourseViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val setCourseNicknameUseCase: SetCourseNicknameUseCase = mockk(relaxed = true)
    private val setCourseColorUseCase: SetCourseColorUseCase = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val localBroadcastManager: LocalBroadcastManager = mockk(relaxed = true)
    private val customizeCourseBehavior: CustomizeCourseBehavior = mockk(relaxed = true)

    private lateinit var viewModel: CustomizeCourseViewModel

    private val testCourse = Course(
        id = 1L,
        name = "My Nickname",
        originalName = "Original Course Name",
        courseCode = "CS101",
        imageUrl = "https://example.com/image.jpg"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        ContextKeeper.appContext = mockk(relaxed = true)

        every { savedStateHandle.get<Course>(Const.COURSE) } returns testCourse
        every { customizeCourseBehavior.shouldShowColorOverlay() } returns true
        every { resources.getColor(any(), any()) } returns 0xFF0000FF.toInt()
        every { resources.getString(R.string.errorOccurred) } returns "An error occurred"

        coEvery { setCourseNicknameUseCase(any()) } returns Unit
        coEvery { setCourseColorUseCase(any()) } returns Unit

        viewModel = CustomizeCourseViewModel(
            savedStateHandle = savedStateHandle,
            setCourseNicknameUseCase = setCourseNicknameUseCase,
            setCourseColorUseCase = setCourseColorUseCase,
            resources = resources,
            colorKeeper = colorKeeper,
            localBroadcastManager = localBroadcastManager,
            customizeCourseBehavior = customizeCourseBehavior
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has correct course information`() {
        val state = viewModel.uiState.value

        assertEquals(testCourse.id, state.courseId)
        assertEquals(testCourse.name, state.courseName)
        assertEquals(testCourse.courseCode, state.courseCode)
        assertEquals(testCourse.imageUrl, state.imageUrl)
        assertEquals(testCourse.name, state.nickname)
        assertFalse(state.isLoading)
        assertFalse(state.shouldNavigateBack)
        assertNull(state.errorMessage)
    }

    @Test
    fun `initial state has showColorOverlay from behavior`() {
        every { customizeCourseBehavior.shouldShowColorOverlay() } returns false

        val viewModel = CustomizeCourseViewModel(
            savedStateHandle = savedStateHandle,
            setCourseNicknameUseCase = setCourseNicknameUseCase,
            setCourseColorUseCase = setCourseColorUseCase,
            resources = resources,
            colorKeeper = colorKeeper,
            localBroadcastManager = localBroadcastManager,
            customizeCourseBehavior = customizeCourseBehavior
        )

        assertFalse(viewModel.uiState.value.showColorOverlay)
    }

    @Test
    fun `onNicknameChanged updates nickname in state`() {
        val state = viewModel.uiState.value

        state.onNicknameChanged("New Nickname")

        assertEquals("New Nickname", viewModel.uiState.value.nickname)
    }

    @Test
    fun `onColorSelected updates selectedColor in state`() {
        val state = viewModel.uiState.value
        val newColor = 0xFF00FF00.toInt()

        state.onColorSelected(newColor)

        assertEquals(newColor, viewModel.uiState.value.selectedColor)
    }

    @Test
    fun `onDone calls setCourseNicknameUseCase when nickname changed`() = runTest {
        val state = viewModel.uiState.value
        state.onNicknameChanged("New Nickname")

        state.onDone()
        advanceUntilIdle()

        coVerify {
            setCourseNicknameUseCase(SetCourseNicknameParams(testCourse.id, "New Nickname"))
        }
    }

    @Test
    fun `onDone does not call setCourseNicknameUseCase when nickname unchanged`() = runTest {
        val state = viewModel.uiState.value

        state.onDone()
        advanceUntilIdle()

        coVerify(exactly = 0) {
            setCourseNicknameUseCase(any())
        }
    }

    @Test
    fun `onDone always calls setCourseColorUseCase`() = runTest {
        val state = viewModel.uiState.value
        val newColor = 0xFF00FF00.toInt()
        state.onColorSelected(newColor)

        state.onDone()
        advanceUntilIdle()

        coVerify {
            setCourseColorUseCase(SetCourseColorParams(testCourse.contextId, newColor))
        }
    }

    @Test
    fun `onDone adds color to cache`() = runTest {
        val state = viewModel.uiState.value
        val newColor = 0xFF00FF00.toInt()
        state.onColorSelected(newColor)

        state.onDone()
        advanceUntilIdle()

        verify {
            colorKeeper.addToCache(testCourse.contextId, newColor)
        }
    }

    @Test
    fun `onDone sends broadcast`() = runTest {
        val state = viewModel.uiState.value

        state.onDone()
        advanceUntilIdle()

        verify {
            localBroadcastManager.sendBroadcast(any())
        }
    }

    @Test
    fun `onDone sets shouldNavigateBack to true on success`() = runTest {
        val state = viewModel.uiState.value

        state.onDone()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.shouldNavigateBack)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onDone sets error message on failure`() = runTest {
        coEvery { setCourseColorUseCase(any()) } throws RuntimeException("Network error")

        val state = viewModel.uiState.value

        state.onDone()
        advanceUntilIdle()

        assertEquals("An error occurred", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.shouldNavigateBack)
    }

    @Test
    fun `onDone sets isLoading to true while processing`() = runTest {
        coEvery { setCourseColorUseCase(any()) } coAnswers {
            assertTrue(viewModel.uiState.value.isLoading)
        }

        val state = viewModel.uiState.value

        state.onDone()
        advanceUntilIdle()
    }

    @Test
    fun `onNavigationHandled resets shouldNavigateBack`() {
        val state = viewModel.uiState.value

        state.onDone()

        state.onNavigationHandled()

        assertFalse(viewModel.uiState.value.shouldNavigateBack)
    }

    @Test
    fun `onErrorHandled resets errorMessage`() = runTest {
        coEvery { setCourseColorUseCase(any()) } throws RuntimeException("Network error")

        val state = viewModel.uiState.value

        state.onDone()
        advanceUntilIdle()

        state.onErrorHandled()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `initial state has empty nickname when course has no original name`() {
        val courseWithoutOriginalName = testCourse.copy(originalName = null)
        every { savedStateHandle.get<Course>(Const.COURSE) } returns courseWithoutOriginalName

        val viewModel = CustomizeCourseViewModel(
            savedStateHandle = savedStateHandle,
            setCourseNicknameUseCase = setCourseNicknameUseCase,
            setCourseColorUseCase = setCourseColorUseCase,
            resources = resources,
            colorKeeper = colorKeeper,
            localBroadcastManager = localBroadcastManager,
            customizeCourseBehavior = customizeCourseBehavior
        )

        assertEquals("", viewModel.uiState.value.nickname)
    }
}