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

package com.instructure.pandautils.features.dashboard.widget.forecast

import com.google.gson.Gson
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ObserveForecastConfigUseCaseTest {

    private val repository: WidgetConfigDataRepository = mockk(relaxed = true)
    private val gson = Gson()
    private lateinit var useCase: ObserveForecastConfigUseCase

    @Before
    fun setUp() {
        useCase = ObserveForecastConfigUseCase(repository, gson)
    }

    @Test
    fun `execute returns default config when json is null`() = runTest {
        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(null)

        val result = useCase(Unit).first()

        assertEquals(WidgetMetadata.WIDGET_ID_FORECAST, result.widgetId)
        assertEquals(0xFF2573DF.toInt(), result.backgroundColor)
    }

    @Test
    fun `execute returns parsed config when json is valid`() = runTest {
        val customColor = 0xFF00FF00.toInt()
        val configJson = """{"widgetId":"forecast","backgroundColor":$customColor}"""
        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(configJson)

        val result = useCase(Unit).first()

        assertEquals(WidgetMetadata.WIDGET_ID_FORECAST, result.widgetId)
        assertEquals(customColor, result.backgroundColor)
    }

    @Test
    fun `execute returns default config when json is invalid`() = runTest {
        val invalidJson = "invalid json"
        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(invalidJson)

        val result = useCase(Unit).first()

        assertEquals(WidgetMetadata.WIDGET_ID_FORECAST, result.widgetId)
        assertEquals(0xFF2573DF.toInt(), result.backgroundColor)
    }

    @Test
    fun `execute returns default config when json parsing throws exception`() = runTest {
        val malformedJson = """{"widgetId":"forecast","backgroundColor":"not a number"}"""
        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(malformedJson)

        val result = useCase(Unit).first()

        assertEquals(WidgetMetadata.WIDGET_ID_FORECAST, result.widgetId)
        assertEquals(0xFF2573DF.toInt(), result.backgroundColor)
    }

    @Test
    fun `execute emits updated config when json changes`() = runTest {
        val color1 = 0xFF0000FF.toInt()
        val color2 = 0xFFFF0000.toInt()
        val configJson1 = """{"widgetId":"forecast","backgroundColor":$color1}"""
        val configJson2 = """{"widgetId":"forecast","backgroundColor":$color2}"""

        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(configJson1, configJson2)

        val results = mutableListOf<ForecastConfig>()
        useCase(Unit).collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(color1, results[0].backgroundColor)
        assertEquals(color2, results[1].backgroundColor)
    }

    @Test
    fun `execute returns default config with correct widget id`() = runTest {
        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(null)

        val result = useCase(Unit).first()

        assertEquals(WidgetMetadata.WIDGET_ID_FORECAST, result.widgetId)
    }

    @Test
    fun `execute handles transition from null to valid config`() = runTest {
        val customColor = 0xFF123456.toInt()
        val configJson = """{"widgetId":"forecast","backgroundColor":$customColor}"""

        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(null, configJson)

        val results = mutableListOf<ForecastConfig>()
        useCase(Unit).collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(0xFF2573DF.toInt(), results[0].backgroundColor)
        assertEquals(customColor, results[1].backgroundColor)
    }

    @Test
    fun `execute handles transition from valid config to null`() = runTest {
        val customColor = 0xFF123456.toInt()
        val configJson = """{"widgetId":"forecast","backgroundColor":$customColor}"""

        coEvery { repository.observeConfigJson(WidgetMetadata.WIDGET_ID_FORECAST) } returns flowOf(configJson, null)

        val results = mutableListOf<ForecastConfig>()
        useCase(Unit).collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(customColor, results[0].backgroundColor)
        assertEquals(0xFF2573DF.toInt(), results[1].backgroundColor)
    }
}