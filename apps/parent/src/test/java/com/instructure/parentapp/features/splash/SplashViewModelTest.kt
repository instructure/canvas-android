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
 */

package com.instructure.parentapp.features.splash

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.parentapp.BuildConfig
import io.heap.autocapture.ViewAutocaptureSDK
import io.heap.core.Heap
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class SplashViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val repository: SplashRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val heap: Heap = mockk(relaxed = true)
    private val viewAutoCaptureSDK: ViewAutocaptureSDK = mockk(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

    private lateinit var viewModel: SplashViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<Long>(Const.QR_CODE_MASQUERADE_ID) } returns 0L
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context
        mockkStatic(Heap::class)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Load and store initial data successfully`() = runTest {
        val user = User(id = 1L)
        coEvery { repository.getSelf() } returns user

        val colors = CanvasColor()
        coEvery { repository.getColors() } returns colors

        val theme = CanvasTheme("", "", "", "", "", "", "", "")
        coEvery { repository.getTheme() } returns theme

        val students = User(id = 2L)
        coEvery { repository.getStudents() } returns listOf(students)

        createViewModel()

        coVerify { apiPrefs.user = user }
        coVerify { colorKeeper.addToCache(colors) }

        val events = mutableListOf<SplashAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(SplashAction.InitialDataLoadingFinished, events.last())
        assertEquals(SplashAction.ApplyTheme(theme), events.first())
    }

    @Test
    fun `Load and store initial data fails`() = runTest {
        coEvery { repository.getSelf() } throws Exception()

        createViewModel()

        val events = mutableListOf<SplashAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(SplashAction.InitialDataLoadingFinished, events.last())
    }

    @Test
    fun `No observed students navigates to not a parent screen`() = runTest {
        val user = User(id = 1L)
        coEvery { repository.getSelf() } returns user

        val colors = CanvasColor()
        coEvery { repository.getColors() } returns colors

        val theme = CanvasTheme("", "", "", "", "", "", "", "")
        coEvery { repository.getTheme() } returns theme

        coEvery { repository.getStudents() } returns emptyList()

        createViewModel()

        val events = mutableListOf<SplashAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(SplashAction.NavigateToNotAParentScreen, events.last())
    }

    @Test
    fun `No observed students finishes data loading and navigates to app if the user can masquerade`() = runTest {
        every { apiPrefs.canBecomeUser } returns true
        val user = User(id = 1L)
        coEvery { repository.getSelf() } returns user

        val colors = CanvasColor()
        coEvery { repository.getColors() } returns colors

        val theme = CanvasTheme("", "", "", "", "", "", "", "")
        coEvery { repository.getTheme() } returns theme

        coEvery { repository.getStudents() } returns emptyList()

        createViewModel()

        val events = mutableListOf<SplashAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(SplashAction.InitialDataLoadingFinished, events.last())
    }

    @Test
    fun `User stored and locale changed`() = runTest {
        val user = User(id = 1L, effective_locale = "en")
        coEvery { repository.getSelf() } returns user
        every { apiPrefs.user = any() } answers {
            every { apiPrefs.effectiveLocale } returns user.effective_locale.orEmpty()
        }

        createViewModel()

        val events = mutableListOf<SplashAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(SplashAction.LocaleChanged, events.first())
    }

    @Test
    fun `Set canBecomeUser when the domain is siteadmin`() = runTest {
        every { apiPrefs.domain } returns "siteadmin.whatever.com"
        every { apiPrefs.canBecomeUser } returns null
        coEvery { repository.getBecomeUserPermission() } returns false

        createViewModel()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList()
        }

        verify { apiPrefs.canBecomeUser = true }
    }

    @Test
    fun `Set canBecomeUser by the result of the canBecomeUserApiCall`() = runTest {
        every { apiPrefs.domain } returns "whatever.com"
        every { apiPrefs.canBecomeUser } returns null
        coEvery { repository.getBecomeUserPermission() } returns true

        createViewModel()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList()
        }

        verify { apiPrefs.canBecomeUser = true }
    }

    @Test
    fun `Do not request permission and set canBecomeUser when masquerading from a QR code`() = runTest {
        every { apiPrefs.domain } returns "whatever.com"
        every { apiPrefs.canBecomeUser } returns null
        every { savedStateHandle.get<Long>(Const.QR_CODE_MASQUERADE_ID) } returns 1L

        createViewModel()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList()
        }

        coVerify(exactly = 0) { repository.getBecomeUserPermission() }
        verify(exactly = 0) { apiPrefs.canBecomeUser = any() }
    }

    @Test
    fun `Do not request permission and set canBecomeUser when it is already set`() = runTest {
        every { apiPrefs.domain } returns "whatever.com"
        every { apiPrefs.canBecomeUser } returns true

        createViewModel()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList()
        }

        coVerify(exactly = 0) { repository.getBecomeUserPermission() }
        verify(exactly = 0) { apiPrefs.canBecomeUser = any() }
    }

    @Test
    fun `Send usage metrics enabled`() = runTest {
        coEvery { repository.getSendUsageMetrics() } returns true

        createViewModel()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList()
        }

        verify { heap.startRecording(context, BuildConfig.HEAP_APP_ID) }
    }

    @Test
    fun `Send usage metrics disabled`() = runTest {
        coEvery { repository.getSendUsageMetrics() } returns false

        createViewModel()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList()
        }

        verify { heap.stopRecording() }
    }

    private fun createViewModel() {
        viewModel = SplashViewModel(
            context = context,
            repository = repository,
            apiPrefs = apiPrefs,
            colorKeeper = colorKeeper,
            heap = heap,
            viewAutocaptureSDK = viewAutoCaptureSDK,
            savedStateHandle = savedStateHandle
        )
    }
}
