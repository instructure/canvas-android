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

package com.instructure.student.features.dashboard.widget.repository

import com.instructure.student.features.dashboard.widget.WidgetConfig
import com.instructure.student.features.dashboard.widget.db.WidgetConfigDao
import com.instructure.student.features.dashboard.widget.db.WidgetConfigEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BaseWidgetConfigRepositoryTest {

    private val dao: WidgetConfigDao = mockk(relaxed = true)
    private lateinit var repository: TestWidgetConfigRepository

    @Before
    fun setup() {
        repository = TestWidgetConfigRepository(dao)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `observeConfig returns deserialized config from dao`() = runTest {
        val entity = WidgetConfigEntity("widget1", """{"data":"value"}""")
        coEvery { dao.observeConfig("widget1") } returns flowOf(entity)

        val result = repository.observeConfig("widget1").first()

        assertEquals("widget1", result.widgetId)
        assertEquals("value", result.data)
    }

    @Test
    fun `observeConfig returns default when entity is null`() = runTest {
        coEvery { dao.observeConfig("widget1") } returns flowOf(null)

        val result = repository.observeConfig("widget1").first()

        assertEquals("widget1", result.widgetId)
        assertEquals("default", result.data)
    }

    @Test
    fun `observeConfig returns default when deserialization fails`() = runTest {
        val entity = WidgetConfigEntity("widget1", """invalid json""")
        coEvery { dao.observeConfig("widget1") } returns flowOf(entity)

        val result = repository.observeConfig("widget1").first()

        // Note: TestWidgetConfigRepository.deserializeConfig returns null for invalid JSON,
        // which triggers getDefaultConfig() to be used
        assertEquals("widget1", result.widgetId)
        assertEquals("default", result.data)
    }

    @Test
    fun `saveConfig serializes and saves to dao`() = runTest {
        val config = TestWidgetConfig("widget1", "test-value")

        repository.saveConfig(config)

        coVerify {
            dao.upsertConfig(
                match {
                    it.widgetId == "widget1" &&
                    it.configJson == """{"data":"test-value"}"""
                }
            )
        }
    }

    @Test
    fun `deleteConfig removes entity from dao`() = runTest {
        val entity = WidgetConfigEntity("widget1", """{"data":"value"}""")
        val flow = flowOf(entity)
        coEvery { dao.observeConfig("widget1") } returns flow

        repository.deleteConfig("widget1")

        coVerify { dao.deleteConfig(entity) }
    }

    @Test
    fun `deleteConfig does nothing when entity does not exist`() = runTest {
        coEvery { dao.observeConfig("widget1") } returns flowOf(null)

        repository.deleteConfig("widget1")

        coVerify(exactly = 0) { dao.deleteConfig(any()) }
    }

    // Test implementations
    data class TestWidgetConfig(
        override val widgetId: String,
        val data: String
    ) : WidgetConfig {
        override fun toJson(): String = """{"data":"$data"}"""
    }

    class TestWidgetConfigRepository(dao: WidgetConfigDao) :
        BaseWidgetConfigRepository<TestWidgetConfig>(dao) {

        override fun deserializeConfig(json: String): TestWidgetConfig? {
            return try {
                // Simple JSON parsing - expects {"data":"value"}
                if (!json.contains("{") || !json.contains("}")) return null
                val data = json.substringAfter("\":\"").substringBefore("\"}")
                if (data.isEmpty() || data == json) return null
                TestWidgetConfig("widget1", data)
            } catch (e: Exception) {
                null
            }
        }

        override fun getDefaultConfig(): TestWidgetConfig {
            return TestWidgetConfig("widget1", "default")
        }
    }
}