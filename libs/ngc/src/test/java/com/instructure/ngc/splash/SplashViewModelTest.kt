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

package com.instructure.ngc.splash

import android.content.Context
import android.content.SharedPreferences
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.ngc.features.splash.SplashViewModel
import com.instructure.pandautils.domain.usecase.splash.LoadSplashDataUseCase
import com.instructure.pandautils.domain.usecase.splash.SetupPendoTrackingUseCase
import com.instructure.pandautils.domain.usecase.splash.SplashData
import com.instructure.pandautils.utils.ColorKeeper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class SplashViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val loadSplashDataUseCase: LoadSplashDataUseCase = mockk(relaxed = true)
    private val setupPendoTrackingUseCase: SetupPendoTrackingUseCase = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val sharedPrefs: SharedPreferences = mockk(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        ContextKeeper.appContext = context
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state shows loading`() = runTest {
        coEvery { loadSplashDataUseCase(any()) } coAnswers {
            // Simulate delay to capture initial state
            kotlinx.coroutines.delay(100)
            SplashData(null, null, null, null)
        }

        val viewModel = getViewModel()

        // Initial state before data loads
        assertTrue(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.initialDataLoaded)
    }

    @Test
    fun `Successful data load updates UI state`() = runTest {
        val user = User(id = 123L, name = "Test User")
        val colors = CanvasColor(mapOf("course_1" to "#FF0000"))
        val theme = createCanvasTheme()
        val splashData = SplashData(user, colors, theme, true)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.effectiveLocale } returns "en"
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.initialDataLoaded)
        assertEquals(theme, viewModel.uiState.value.themeToApply)
    }

    @Test
    fun `User info is saved when user is present in splash data`() = runTest {
        val user = User(id = 123L, name = "Test User")
        val splashData = SplashData(user, null, null, null)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.effectiveLocale } returns "en"
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { apiPrefs.user = user }
    }

    @Test
    fun `Colors are added to cache when present in splash data`() = runTest {
        val colors = CanvasColor(mapOf("course_1" to "#FF0000"))
        val splashData = SplashData(null, colors, null, null)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { colorKeeper.addToCache(colors) }
    }

    @Test
    fun `canBecomeUser is set from splash data when null in apiPrefs`() = runTest {
        val splashData = SplashData(null, null, null, true)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { apiPrefs.canBecomeUser = true }
    }

    @Test
    fun `canBecomeUser is true for siteadmin domain`() = runTest {
        val splashData = SplashData(null, null, null, false)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "siteadmin.instructure.com"

        getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { apiPrefs.canBecomeUser = true }
    }

    @Test
    fun `canBecomeUser is not updated when already set in apiPrefs`() = runTest {
        val splashData = SplashData(null, null, null, true)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns false
        every { apiPrefs.domain } returns "test.instructure.com"

        getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 0) { apiPrefs.canBecomeUser = any() }
    }

    @Test
    fun `Pendo tracking is set up after data load`() = runTest {
        val splashData = SplashData(null, null, null, null)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { setupPendoTrackingUseCase(Unit) }
    }

    @Test
    fun `Error state is set when data load fails`() = runTest {
        coEvery { loadSplashDataUseCase(any()) } throws RuntimeException("Network error")

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertTrue(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.initialDataLoaded)
    }

    @Test
    fun `onThemeApplied clears themeToApply`() = runTest {
        val theme = createCanvasTheme()
        val splashData = SplashData(null, null, theme, null)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(theme, viewModel.uiState.value.themeToApply)

        viewModel.onThemeApplied()

        assertNull(viewModel.uiState.value.themeToApply)
    }

    @Test
    fun `Theme is not set in UI state when null in splash data`() = runTest {
        val splashData = SplashData(null, null, null, null)

        coEvery { loadSplashDataUseCase(any()) } returns splashData
        every { apiPrefs.canBecomeUser } returns null
        every { apiPrefs.domain } returns "test.instructure.com"

        val viewModel = getViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.themeToApply)
    }

    private fun getViewModel() = SplashViewModel(
        context,
        loadSplashDataUseCase,
        setupPendoTrackingUseCase,
        apiPrefs,
        colorKeeper
    )

    private fun createCanvasTheme(
        brand: String = "test_brand",
        fontColorDark: String = "#000000",
        button: String = "#FF0000",
        buttonText: String = "#FFFFFF",
        primary: String = "#0000FF",
        primaryText: String = "#FFFFFF",
        accent: String = "#00FF00",
        logoUrl: String = "https://example.com/logo.png",
        mobileLogoBackground: String = "#FFFFFF"
    ) = CanvasTheme(
        brand = brand,
        fontColorDark = fontColorDark,
        button = button,
        buttonText = buttonText,
        primary = primary,
        primaryText = primaryText,
        accent = accent,
        logoUrl = logoUrl,
        mobileLogoBackground = mobileLogoBackground
    )
}
