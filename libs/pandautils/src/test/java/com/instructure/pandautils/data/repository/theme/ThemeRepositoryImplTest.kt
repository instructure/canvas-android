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

package com.instructure.pandautils.data.repository.theme

import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ThemeRepositoryImplTest {

    private val themeApi: ThemeAPI.ThemeInterface = mockk(relaxed = true)

    private lateinit var repository: ThemeRepositoryImpl

    @Before
    fun setup() {
        repository = ThemeRepositoryImpl(themeApi)
    }

    @Test
    fun `getTheme returns success result from API`() = runTest {
        val theme = createCanvasTheme()
        coEvery { themeApi.getTheme(any()) } returns DataResult.Success(theme)

        val result = repository.getTheme(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals(theme, (result as DataResult.Success).data)
    }

    @Test
    fun `getTheme returns fail result from API`() = runTest {
        coEvery { themeApi.getTheme(any()) } returns DataResult.Fail()

        val result = repository.getTheme(forceRefresh = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getTheme passes forceRefresh false to RestParams`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery { themeApi.getTheme(capture(paramsSlot)) } returns DataResult.Success(createCanvasTheme())

        repository.getTheme(forceRefresh = false)

        coVerify { themeApi.getTheme(any()) }
        assertEquals(false, paramsSlot.captured.isForceReadFromNetwork)
    }

    @Test
    fun `getTheme passes forceRefresh true to RestParams`() = runTest {
        val paramsSlot = slot<RestParams>()
        coEvery { themeApi.getTheme(capture(paramsSlot)) } returns DataResult.Success(createCanvasTheme())

        repository.getTheme(forceRefresh = true)

        coVerify { themeApi.getTheme(any()) }
        assertEquals(true, paramsSlot.captured.isForceReadFromNetwork)
    }

    @Test
    fun `getTheme returns theme with brand value`() = runTest {
        val theme = createCanvasTheme(brand = "my_brand")
        coEvery { themeApi.getTheme(any()) } returns DataResult.Success(theme)

        val result = repository.getTheme(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals("my_brand", (result as DataResult.Success).data.brand)
    }

    @Test
    fun `getTheme returns theme with primary color`() = runTest {
        val theme = createCanvasTheme(primary = "#123456")
        coEvery { themeApi.getTheme(any()) } returns DataResult.Success(theme)

        val result = repository.getTheme(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        assertEquals("#123456", (result as DataResult.Success).data.primary)
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