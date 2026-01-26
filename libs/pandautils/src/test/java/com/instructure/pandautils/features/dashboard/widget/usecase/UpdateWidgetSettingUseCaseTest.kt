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
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateWidgetSettingUseCaseTest {

    private val repository: WidgetConfigDataRepository = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var useCase: UpdateWidgetSettingUseCase

    @Before
    fun setUp() {
        useCase = UpdateWidgetSettingUseCase(repository, gson)
    }

    @Test
    fun testUpdateBooleanSetting() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val existingConfig = """{"widgetId":"welcome","showGreeting":true}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "showGreeting", false)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"showGreeting\":false"))
    }

    @Test
    fun testUpdateStringSetting() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val existingConfig = """{"widgetId":"welcome","title":"Hello"}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "title", "Welcome")
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"title\":\"Welcome\""))
    }

    @Test
    fun testUpdateIntSetting() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val existingConfig = """{"widgetId":"welcome","backgroundColor":123456}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "backgroundColor", 789012)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"backgroundColor\":789012"))
    }

    @Test
    fun testUpdateNumberSetting() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val existingConfig = """{"widgetId":"welcome","value":1.5}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "value", 2.5)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"value\":2.5"))
    }

    @Test
    fun testUpdateSettingWithNoExistingConfig() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        coEvery { repository.getConfigJson(widgetId) } returns null

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "showGreeting", false)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"showGreeting\":false"))
    }

    @Test
    fun testUpdateSettingPreservesOtherSettings() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val existingConfig = """{"widgetId":"welcome","showGreeting":true,"backgroundColor":123456}"""
        coEvery { repository.getConfigJson(widgetId) } returns existingConfig

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "showGreeting", false)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"showGreeting\":false"))
        assertTrue(savedJson.contains("\"backgroundColor\":123456"))
    }

    @Test
    fun testUpdateSettingWithInvalidExistingConfig() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        coEvery { repository.getConfigJson(widgetId) } returns "invalid json"

        val params = UpdateWidgetSettingUseCase.Params(widgetId, "showGreeting", false)
        useCase(params)

        val jsonSlot = slot<String>()
        coVerify { repository.saveConfigJson(widgetId, capture(jsonSlot)) }

        val savedJson = jsonSlot.captured
        assertTrue(savedJson.contains("\"showGreeting\":false"))
    }
}
