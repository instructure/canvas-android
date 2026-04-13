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

package com.instructure.pandautils.domain.usecase.splash

import com.instructure.canvasapi2.models.BecomeUserPermission
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.theme.ThemeRepository
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadSplashDataUseCaseTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val themeRepository: ThemeRepository = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var useCase: LoadSplashDataUseCase

    @Before
    fun setup() {
        useCase = LoadSplashDataUseCase(userRepository, themeRepository, featureFlagProvider)
    }

    @Test
    fun `Returns user when getSelf succeeds`() = runTest {
        val user = User(id = 123L, name = "Test User")
        coEvery { userRepository.getSelf(any()) } returns DataResult.Success(user)
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertEquals(user, result.user)
    }

    @Test
    fun `Returns null user when getSelf fails`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertNull(result.user)
    }

    @Test
    fun `Returns colors when getColors succeeds`() = runTest {
        val colors = CanvasColor(mapOf("course_1" to "#FF0000"))
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Success(colors)
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertEquals(colors, result.colors)
    }

    @Test
    fun `Returns null colors when getColors fails`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertNull(result.colors)
    }

    @Test
    fun `Returns theme when getTheme succeeds`() = runTest {
        val theme = createCanvasTheme()
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Success(theme)
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertEquals(theme, result.theme)
    }

    @Test
    fun `Returns null theme when getTheme fails`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertNull(result.theme)
    }

    @Test
    fun `Returns canBecomeUser when getBecomeUserPermission succeeds`() = runTest {
        val permission = BecomeUserPermission(becomeUser = true)
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Success(permission)

        val result = useCase(LoadSplashDataUseCase.Params())

        assertTrue(result.canBecomeUser!!)
    }

    @Test
    fun `Returns null canBecomeUser when getBecomeUserPermission fails`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        val result = useCase(LoadSplashDataUseCase.Params())

        assertNull(result.canBecomeUser)
    }

    @Test
    fun `Fetches environment feature flags`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        useCase(LoadSplashDataUseCase.Params())

        coVerify { featureFlagProvider.fetchEnvironmentFeatureFlags() }
    }

    @Test
    fun `Does not throw when fetchEnvironmentFeatureFlags fails`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()
        coEvery { featureFlagProvider.fetchEnvironmentFeatureFlags() } throws RuntimeException("Network error")

        val result = useCase(LoadSplashDataUseCase.Params())

        // Should complete without throwing
        assertNull(result.user)
    }

    @Test
    fun `Passes forceRefresh parameter to repositories`() = runTest {
        coEvery { userRepository.getSelf(any()) } returns DataResult.Fail()
        coEvery { userRepository.getColors(any()) } returns DataResult.Fail()
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Fail()
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Fail()

        useCase(LoadSplashDataUseCase.Params(forceRefresh = true))

        coVerify { userRepository.getSelf(forceRefresh = true) }
        coVerify { userRepository.getColors(forceRefresh = true) }
        coVerify { themeRepository.getTheme(forceRefresh = true) }
        coVerify { userRepository.getBecomeUserPermission(forceRefresh = true) }
    }

    @Test
    fun `Returns all data when all repository calls succeed`() = runTest {
        val user = User(id = 123L, name = "Test User")
        val colors = CanvasColor(mapOf("course_1" to "#FF0000"))
        val theme = createCanvasTheme()
        val permission = BecomeUserPermission(becomeUser = true)

        coEvery { userRepository.getSelf(any()) } returns DataResult.Success(user)
        coEvery { userRepository.getColors(any()) } returns DataResult.Success(colors)
        coEvery { themeRepository.getTheme(any()) } returns DataResult.Success(theme)
        coEvery { userRepository.getBecomeUserPermission(any()) } returns DataResult.Success(permission)

        val result = useCase(LoadSplashDataUseCase.Params())

        assertEquals(user, result.user)
        assertEquals(colors, result.colors)
        assertEquals(theme, result.theme)
        assertTrue(result.canBecomeUser!!)
    }

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