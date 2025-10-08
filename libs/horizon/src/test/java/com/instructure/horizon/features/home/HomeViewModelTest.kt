/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.home

import android.content.Context
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.pandautils.utils.LocaleUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val homeRepository: HomeRepository = mockk(relaxed = true)
    private val localeUtils: LocaleUtils = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = User(id = 1L, name = "Test User", locale = "en")
    private val testTheme = CanvasTheme("", "", "", "", "", "", "", "")
    private val testCourses = listOf(
        CourseWithProgress(courseId = 1L, courseName = "Course 1", courseSyllabus = "", progress = 50.0),
        CourseWithProgress(courseId = 2L, courseName = "Course 2", courseSyllabus = "", progress = 75.0)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { apiPrefs.effectiveLocale } returns "en"
        coEvery { homeRepository.getSelf() } returns testUser
        coEvery { homeRepository.getTheme() } returns testTheme
        coEvery { homeRepository.getCourses() } returns testCourses
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test successful data load updates UI state`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value

        assertFalse(state.initialDataLoading)
        assertEquals(testTheme, state.theme)
        verify { apiPrefs.user = testUser }
    }

    @Test
    fun `Test theme is loaded from repository`() = runTest {
        val viewModel = getViewModel()

        coVerify { homeRepository.getTheme() }
        assertEquals(testTheme, viewModel.uiState.value.theme)
    }

    @Test
    fun `Test user is saved to API prefs`() = runTest {
        val viewModel = getViewModel()

        verify { apiPrefs.user = testUser }
    }

    @Test
    fun `Test courses are loaded`() = runTest {
        val viewModel = getViewModel()

        coVerify { homeRepository.getCourses() }
    }

    @Test
    fun `Test locale change triggers app restart`() = runTest {
        every { apiPrefs.effectiveLocale } returnsMany listOf("en", "es")

        val viewModel = getViewModel()

        verify { localeUtils.restartApp(context) }
    }

    @Test
    fun `Test no restart when locale unchanged`() = runTest {
        every { apiPrefs.effectiveLocale } returns "en"

        val viewModel = getViewModel()

        verify(exactly = 0) { localeUtils.restartApp(any()) }
    }

    @Test
    fun `Test failed data load sets loading to false`() = runTest {
        coEvery { homeRepository.getSelf() } throws Exception("Network error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.initialDataLoading)
    }

    @Test
    fun `Test updateShowAiAssist updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateShowAiAssist(true)

        assertTrue(viewModel.uiState.value.showAiAssist)
    }

    @Test
    fun `Test updateShowAiAssist sets AI context with course IDs`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateShowAiAssist(true)

        verify { aiAssistContextProvider.aiAssistContext = match { it.contextSources.size == 2 } }
    }

    @Test
    fun `Test null user does not crash`() = runTest {
        coEvery { homeRepository.getSelf() } returns null

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.initialDataLoading)
        verify(exactly = 0) { apiPrefs.user = any() }
    }

    @Test
    fun `Test null theme does not crash`() = runTest {
        coEvery { homeRepository.getTheme() } returns null

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.initialDataLoading)
        assertNotNull(viewModel.uiState.value)
    }

    private fun getViewModel(): HomeViewModel {
        return HomeViewModel(context, apiPrefs, homeRepository, localeUtils, aiAssistContextProvider)
    }
}
