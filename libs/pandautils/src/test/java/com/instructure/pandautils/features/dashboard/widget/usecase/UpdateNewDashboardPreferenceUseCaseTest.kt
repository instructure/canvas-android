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

package com.instructure.pandautils.features.dashboard.widget.usecase

import com.google.gson.Gson
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateNewDashboardPreferenceUseCaseTest {

    private val repository: WidgetConfigDataRepository = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var useCase: UpdateNewDashboardPreferenceUseCase

    @Before
    fun setUp() {
        useCase = UpdateNewDashboardPreferenceUseCase(repository, gson)
    }

    @Test
    fun `sets newDashboardEnabled to false when no existing config`() = runTest {
        coEvery { repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL) } returns null

        useCase(UpdateNewDashboardPreferenceUseCase.Params(false))

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, capture(jsonSlot)) }

        val saved = gson.fromJson(jsonSlot.captured, GlobalConfig::class.java)
        assertFalse(saved.newDashboardEnabled)
    }

    @Test
    fun `sets newDashboardEnabled to true when no existing config`() = runTest {
        coEvery { repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL) } returns null

        useCase(UpdateNewDashboardPreferenceUseCase.Params(true))

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, capture(jsonSlot)) }

        val saved = gson.fromJson(jsonSlot.captured, GlobalConfig::class.java)
        assertTrue(saved.newDashboardEnabled)
    }

    @Test
    fun `updates existing config to false preserving other fields`() = runTest {
        val customColor = 0xFF00FF00.toInt()
        val existingConfig = GlobalConfig(backgroundColor = customColor, newDashboardEnabled = true).toJson()
        coEvery { repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL) } returns existingConfig

        useCase(UpdateNewDashboardPreferenceUseCase.Params(false))

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, capture(jsonSlot)) }

        val saved = gson.fromJson(jsonSlot.captured, GlobalConfig::class.java)
        assertFalse(saved.newDashboardEnabled)
        assertTrue(saved.backgroundColor == customColor)
    }

    @Test
    fun `updates existing config to true preserving other fields`() = runTest {
        val customColor = 0xFFFF0000.toInt()
        val existingConfig = GlobalConfig(backgroundColor = customColor, newDashboardEnabled = false).toJson()
        coEvery { repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL) } returns existingConfig

        useCase(UpdateNewDashboardPreferenceUseCase.Params(true))

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, capture(jsonSlot)) }

        val saved = gson.fromJson(jsonSlot.captured, GlobalConfig::class.java)
        assertTrue(saved.newDashboardEnabled)
        assertTrue(saved.backgroundColor == customColor)
    }

    @Test
    fun `handles invalid existing json gracefully`() = runTest {
        coEvery { repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL) } returns "invalid json"

        useCase(UpdateNewDashboardPreferenceUseCase.Params(false))

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, capture(jsonSlot)) }

        val saved = gson.fromJson(jsonSlot.captured, GlobalConfig::class.java)
        assertFalse(saved.newDashboardEnabled)
        assertTrue(saved.backgroundColor == GlobalConfig.DEFAULT_COLOR)
    }

    @Test
    fun `existing config without newDashboardEnabled field defaults to true then updates`() = runTest {
        val existingConfig = """{"widgetId":"global","backgroundColor":${GlobalConfig.DEFAULT_COLOR}}"""
        coEvery { repository.getConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL) } returns existingConfig

        useCase(UpdateNewDashboardPreferenceUseCase.Params(false))

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL, capture(jsonSlot)) }

        val saved = gson.fromJson(jsonSlot.captured, GlobalConfig::class.java)
        assertFalse(saved.newDashboardEnabled)
    }
}