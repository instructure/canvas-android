/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.details.frontpage

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class FrontPageViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: FrontPageRepository = mockk(relaxed = true)
    private val parentPrefs: ParentPrefs = mockk(relaxed = true)

    private lateinit var viewModel: FrontPageViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1, 1)
        coEvery { savedStateHandle.get<Long>(Navigation.COURSE_ID) } returns 1
        every { parentPrefs.currentStudent } returns User(shortName = "User 1")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Load front page`() = runTest {
        coEvery { repository.loadFrontPage(1, false) } returns Page(id = 1L, body = "Front Page")

        createViewModel()

        val expected = FrontPageUiState(
            studentColor = 1,
            isLoading = false,
            isError = false,
            htmlContent = "Front Page"
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Error loading front page`() = runTest {
        coEvery { repository.loadFrontPage(1, false) } throws Exception()

        createViewModel()

        val expected = FrontPageUiState(
            studentColor = 1,
            isLoading = false,
            isError = true
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Refresh front page`() = runTest {
        coEvery { repository.loadFrontPage(1, any()) } returns Page(id = 1L, body = "Front Page")

        createViewModel()

        val expected = FrontPageUiState(
            studentColor = 1,
            isLoading = false,
            isError = false,
            htmlContent = "Front Page"
        )

        Assert.assertEquals(expected, viewModel.uiState.value)

        coEvery { repository.loadFrontPage(1, true) } returns Page(id = 2L, body = "Front Page 2")

        viewModel.handleAction(FrontPageAction.Refresh)

        val expectedAfterRefresh = expected.copy(htmlContent = "Front Page 2")

        Assert.assertEquals(expectedAfterRefresh, viewModel.uiState.value)
    }

    private fun createViewModel() {
        viewModel = FrontPageViewModel(savedStateHandle, repository, parentPrefs)
    }
}
