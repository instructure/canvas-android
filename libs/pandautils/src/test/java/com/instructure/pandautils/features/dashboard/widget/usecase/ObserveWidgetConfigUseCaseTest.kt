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
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ObserveWidgetConfigUseCaseTest {

    private val repository: WidgetConfigDataRepository = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var useCase: ObserveWidgetConfigUseCase

    @Before
    fun setUp() {
        useCase = ObserveWidgetConfigUseCase(repository, gson)
    }

    @Test
    fun testObserveConfigWithExistingJson() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val configJson = """{"widgetId":"welcome","showGreeting":true,"backgroundColor":123456}"""
        coEvery { repository.observeConfigJson(widgetId) } returns flowOf(configJson)

        val result = useCase(widgetId).first()

        assertEquals(2, result.size)
        assertEquals("showGreeting", result[0].key)
        assertEquals(true, result[0].value)
        assertEquals(SettingType.BOOLEAN, result[0].type)
        assertEquals("backgroundColor", result[1].key)
        assertEquals(SettingType.COLOR, result[1].type)
    }

    @Test
    fun testObserveConfigWithNoExistingJson() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        coEvery { repository.observeConfigJson(widgetId) } returns flowOf(null)

        val result = useCase(widgetId).first()

        assertEquals(2, result.size)
        assertEquals("showGreeting", result[0].key)
        assertEquals(true, result[0].value)
        assertEquals(SettingType.BOOLEAN, result[0].type)
    }

    @Test
    fun testObserveConfigWithInvalidJson() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        coEvery { repository.observeConfigJson(widgetId) } returns flowOf("invalid json")

        val result = useCase(widgetId).first()

        assertEquals(2, result.size)
    }

    @Test
    fun testObserveConfigForUnknownWidget() = runTest {
        val widgetId = "unknown_widget"
        coEvery { repository.observeConfigJson(widgetId) } returns flowOf(null)

        val result = useCase(widgetId).first()

        assertEquals(0, result.size)
    }

    @Test
    fun testObserveConfigReturnsEmptyForWidgetWithNoSettings() = runTest {
        val widgetId = "widget_with_no_settings"
        coEvery { repository.observeConfigJson(widgetId) } returns flowOf(null)

        val result = useCase(widgetId).first()

        assertEquals(0, result.size)
    }

    @Test
    fun testObserveConfigUpdatesOnJsonChange() = runTest {
        val widgetId = WidgetMetadata.WIDGET_ID_WELCOME
        val configJson1 = """{"widgetId":"welcome","showGreeting":true,"backgroundColor":123456}"""
        val configJson2 = """{"widgetId":"welcome","showGreeting":false,"backgroundColor":789012}"""
        coEvery { repository.observeConfigJson(widgetId) } returns flowOf(configJson1, configJson2)

        val results = mutableListOf<List<com.instructure.pandautils.features.dashboard.customize.WidgetSettingItem>>()
        useCase(widgetId).collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(true, results[0][0].value)
        assertEquals(false, results[1][0].value)
    }
}