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

package com.instructure.pandautils.features.dashboard.widget.repository

import com.instructure.pandautils.features.dashboard.widget.db.WidgetConfigDao
import com.instructure.pandautils.features.dashboard.widget.db.WidgetConfigEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WidgetConfigDataRepositoryTest {

    private val dao: WidgetConfigDao = mockk(relaxed = true)

    private lateinit var repository: WidgetConfigDataRepository

    @Before
    fun setup() {
        repository = WidgetConfigDataRepository(dao)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `observeConfigJson returns config json from dao`() = runTest {
        val widgetId = "widget1"
        val configJson = """{"key":"value"}"""
        val entity = WidgetConfigEntity(widgetId, configJson)
        coEvery { dao.observeConfig(widgetId) } returns flowOf(entity)

        val result = repository.observeConfigJson(widgetId).first()

        assertEquals(configJson, result)
    }

    @Test
    fun `observeConfigJson returns null when dao returns null`() = runTest {
        val widgetId = "widget1"
        coEvery { dao.observeConfig(widgetId) } returns flowOf(null)

        val result = repository.observeConfigJson(widgetId).first()

        assertNull(result)
    }

    @Test
    fun `getConfigJson returns config json from dao`() = runTest {
        val widgetId = "widget1"
        val configJson = """{"key":"value"}"""
        val entity = WidgetConfigEntity(widgetId, configJson)
        coEvery { dao.getConfig(widgetId) } returns entity

        val result = repository.getConfigJson(widgetId)

        assertEquals(configJson, result)
    }

    @Test
    fun `getConfigJson returns null when dao returns null`() = runTest {
        val widgetId = "widget1"
        coEvery { dao.getConfig(widgetId) } returns null

        val result = repository.getConfigJson(widgetId)

        assertNull(result)
    }

    @Test
    fun `saveConfigJson saves entity with correct data`() = runTest {
        val widgetId = "widget1"
        val configJson = """{"key":"value"}"""

        repository.saveConfigJson(widgetId, configJson)

        coVerify {
            dao.upsertConfig(WidgetConfigEntity(widgetId, configJson))
        }
    }

    @Test
    fun `saveConfigJson saves entity with different widget id and json`() = runTest {
        val widgetId = "widget2"
        val configJson = """{"foo":"bar"}"""

        repository.saveConfigJson(widgetId, configJson)

        coVerify {
            dao.upsertConfig(WidgetConfigEntity(widgetId, configJson))
        }
    }
}