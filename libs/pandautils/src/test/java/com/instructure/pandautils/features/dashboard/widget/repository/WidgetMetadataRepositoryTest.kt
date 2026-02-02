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

import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.db.WidgetMetadataDao
import com.instructure.pandautils.features.dashboard.widget.db.WidgetMetadataEntity
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

class WidgetMetadataRepositoryTest {

    private val dao: WidgetMetadataDao = mockk(relaxed = true)
    private lateinit var repository: WidgetMetadataRepository

    @Before
    fun setup() {
        repository = WidgetMetadataRepositoryImpl(dao)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `observeAllMetadata returns mapped metadata from dao`() = runTest {
        val entities = listOf(
            WidgetMetadataEntity("widget1", 0, true, true),
            WidgetMetadataEntity("widget2", 1, false, false)
        )
        coEvery { dao.observeAllMetadata() } returns flowOf(entities)

        val result = repository.observeAllMetadata().first()

        assertEquals(2, result.size)
        assertEquals("widget1", result[0].id)
        assertEquals(0, result[0].position)
        assertEquals(true, result[0].isVisible)
        assertEquals(true, result[0].isEditable)
        assertEquals("widget2", result[1].id)
        assertEquals(1, result[1].position)
        assertEquals(false, result[1].isVisible)
        assertEquals(false, result[1].isEditable)
    }

    @Test
    fun `observeAllMetadata returns empty list when dao returns empty`() = runTest {
        coEvery { dao.observeAllMetadata() } returns flowOf(emptyList())

        val result = repository.observeAllMetadata().first()

        assertEquals(0, result.size)
    }

    @Test
    fun `saveMetadata calls dao with mapped entity`() = runTest {
        val metadata = WidgetMetadata("widget1", 0, true, true)

        repository.saveMetadata(metadata)

        coVerify {
            dao.upsertMetadata(
                WidgetMetadataEntity("widget1", 0, true, true)
            )
        }
    }

    @Test
    fun `updatePosition calls dao with correct parameters`() = runTest {
        repository.updatePosition("widget1", 5)

        coVerify { dao.updatePosition("widget1", 5) }
    }

    @Test
    fun `updateVisibility calls dao with correct parameters`() = runTest {
        repository.updateVisibility("widget1", false)

        coVerify { dao.updateVisibility("widget1", false) }
    }

    @Test
    fun `saveMetadata preserves all metadata properties`() = runTest {
        val metadata = WidgetMetadata("test-widget", 3, false, false)

        repository.saveMetadata(metadata)

        coVerify {
            dao.upsertMetadata(
                match {
                    it.widgetId == "test-widget" &&
                    it.position == 3 &&
                    it.isVisible == false &&
                    it.isEditable == false
                }
            )
        }
    }

    @Test
    fun `swapPositions calls dao with correct parameters`() = runTest {
        repository.swapPositions("widget1", "widget2")

        coVerify { dao.swapPositions("widget1", "widget2") }
    }
}