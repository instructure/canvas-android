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

package com.instructure.ngc.coursehome

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.ngc.features.coursehome.CourseHomeTab
import com.instructure.ngc.features.coursehome.CourseHomeViewModel
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
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
class CourseHomeViewModelTest {

    private val loadCourseUseCase: LoadCourseUseCase = mockk(relaxed = true)
    private val loadCourseFrontPageUseCase: LoadCourseFrontPageUseCase = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { loadCourseFrontPageUseCase(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state is loading`() = runTest {
        coEvery { loadCourseUseCase(any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            Course(id = 1L, name = "Test Course")
        }

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isError)
    }

    @Test
    fun `Successful course load updates state with course name and image`() = runTest {
        val course = Course(id = 1L, name = "Biology 101", imageUrl = "https://example.com/image.jpg")
        coEvery { loadCourseUseCase(any()) } returns course

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals("Biology 101", state.courseName)
        assertEquals("https://example.com/image.jpg", state.courseImageUrl)
    }

    @Test
    fun `Course without image has null imageUrl`() = runTest {
        val course = Course(id = 1L, name = "Chemistry 201", imageUrl = null)
        coEvery { loadCourseUseCase(any()) } returns course

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Chemistry 201", state.courseName)
        assertNull(state.courseImageUrl)
    }

    @Test
    fun `Failed course load sets error state and records exception`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { loadCourseUseCase(any()) } throws error

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isError)
        verify { crashlytics.recordException(error) }
    }

    @Test
    fun `Failed front page load hides HOME tab without erroring the screen`() = runTest {
        val error = RuntimeException("Front page network error")
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_WIKI,
        )
        coEvery { loadCourseFrontPageUseCase(any()) } throws error

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertFalse(state.availableTabs.contains(CourseHomeTab.HOME))
        verify { crashlytics.recordException(error) }
    }

    @Test
    fun `Default selected tab is HOME when home page exists`() = runTest {
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_WIKI,
        )
        coEvery { loadCourseFrontPageUseCase(any()) } returns Page(body = "<p>Welcome</p>")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CourseHomeTab.HOME, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `onTabSelected updates selected tab`() = runTest {
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_WIKI,
        )
        coEvery { loadCourseFrontPageUseCase(any()) } returns Page(body = "<p>Welcome</p>")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTabSelected(CourseHomeTab.MODULES)
        assertEquals(CourseHomeTab.MODULES, viewModel.uiState.value.selectedTab)

        viewModel.onTabSelected(CourseHomeTab.MY_WORK)
        assertEquals(CourseHomeTab.MY_WORK, viewModel.uiState.value.selectedTab)

        viewModel.onTabSelected(CourseHomeTab.MORE)
        assertEquals(CourseHomeTab.MORE, viewModel.uiState.value.selectedTab)

        viewModel.onTabSelected(CourseHomeTab.HOME)
        assertEquals(CourseHomeTab.HOME, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `HOME tab is available when front page has content`() = runTest {
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_WIKI,
        )
        coEvery { loadCourseFrontPageUseCase(any()) } returns Page(body = "<p>Welcome</p>")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.availableTabs.contains(CourseHomeTab.HOME))
    }

    @Test
    fun `HOME tab is hidden when home page is not a wiki page`() = runTest {
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_MODULES,
        )

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.availableTabs.contains(CourseHomeTab.HOME))
        assertEquals(CourseHomeTab.MODULES, state.selectedTab)
    }

    @Test
    fun `HOME tab is hidden when wiki home page has blank body`() = runTest {
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_WIKI,
        )
        coEvery { loadCourseFrontPageUseCase(any()) } returns Page(body = "")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.availableTabs.contains(CourseHomeTab.HOME))
    }

    @Test
    fun `HOME tab is hidden when front page request returns null`() = runTest {
        coEvery { loadCourseUseCase(any()) } returns Course(
            id = 1L,
            name = "Test",
            homePage = Course.HomePage.HOME_WIKI,
        )
        coEvery { loadCourseFrontPageUseCase(any()) } returns null

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.availableTabs.contains(CourseHomeTab.HOME))
    }

    private fun getViewModel(courseId: Long = 1L): CourseHomeViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(CourseHomeViewModel.ARG_COURSE_ID to courseId))
        return CourseHomeViewModel(
            savedStateHandle,
            loadCourseUseCase,
            loadCourseFrontPageUseCase,
            crashlytics,
        )
    }
}
