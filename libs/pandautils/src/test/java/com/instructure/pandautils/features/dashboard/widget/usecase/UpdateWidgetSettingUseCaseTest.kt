/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateWidgetSettingUseCaseTest {

    private val repository: WidgetConfigDataRepository = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var useCase: UpdateWidgetConfigUseCase

    @Before
    fun setUp() {
        useCase = UpdateWidgetConfigUseCase(repository, gson)
    }

    @Test
    fun testUpdateBooleanSetting() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_FORECAST
        val existingConfig = """{"backgroundColor":0}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetConfigUseCase.Params(widgetId, "backgroundColor", 123456)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"backgroundColor\":123456"))
    }

    @Test
    fun testUpdateIntSetting() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_FORECAST
        val existingConfig = """{"backgroundColor":123456}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetConfigUseCase.Params(widgetId, "backgroundColor", 789012)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"backgroundColor\":789012"))
    }

    @Test
    fun testUpdateSettingWithNoExistingConfig() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_FORECAST
        coEvery { repository.getConfigJson(widgetId) } returns null

        val params = UpdateWidgetConfigUseCase.Params(widgetId, "backgroundColor", 999999)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"backgroundColor\":999999"))
    }

    @Test
    fun testUpdateSettingWithInvalidExistingConfig() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_FORECAST
        coEvery { repository.getConfigJson(widgetId) } returns "invalid json"

        val params = UpdateWidgetConfigUseCase.Params(widgetId, "backgroundColor", 111111)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"backgroundColor\":111111"))
    }
}
