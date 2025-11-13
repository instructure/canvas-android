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

package com.instructure.pandautils.features.dashboard.widget.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetMetadataDaoTest {

    private lateinit var database: WidgetDatabase
    private lateinit var dao: WidgetMetadataDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WidgetDatabase::class.java).build()
        dao = database.widgetMetadataDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun upsertMetadata_insertsNewEntity() = runTest {
        val entity = WidgetMetadataEntity("widget1", 0, true)

        dao.upsertMetadata(entity)

        val result = dao.observeMetadata("widget1").first()
        assertEquals("widget1", result?.widgetId)
        assertEquals(0, result?.position)
        assertEquals(true, result?.isVisible)
    }

    @Test
    fun upsertMetadata_updatesExistingEntity() = runTest {
        val entity1 = WidgetMetadataEntity("widget1", 0, true)
        dao.upsertMetadata(entity1)

        val entity2 = WidgetMetadataEntity("widget1", 1, false)
        dao.upsertMetadata(entity2)

        val result = dao.observeMetadata("widget1").first()
        assertEquals(1, result?.position)
        assertEquals(false, result?.isVisible)
    }

    @Test
    fun observeAllMetadata_returnsOrderedByPosition() = runTest {
        dao.upsertMetadata(WidgetMetadataEntity("widget3", 2, true))
        dao.upsertMetadata(WidgetMetadataEntity("widget1", 0, true))
        dao.upsertMetadata(WidgetMetadataEntity("widget2", 1, true))

        val result = dao.observeAllMetadata().first()

        assertEquals(3, result.size)
        assertEquals("widget1", result[0].widgetId)
        assertEquals("widget2", result[1].widgetId)
        assertEquals("widget3", result[2].widgetId)
    }

    @Test
    fun observeMetadata_returnsNullForNonExistent() = runTest {
        val result = dao.observeMetadata("nonexistent").first()

        assertNull(result)
    }

    @Test
    fun updatePosition_changesPosition() = runTest {
        dao.upsertMetadata(WidgetMetadataEntity("widget1", 0, true))

        dao.updatePosition("widget1", 5)

        val result = dao.observeMetadata("widget1").first()
        assertEquals(5, result?.position)
    }

    @Test
    fun updateVisibility_changesVisibility() = runTest {
        dao.upsertMetadata(WidgetMetadataEntity("widget1", 0, true))

        dao.updateVisibility("widget1", false)

        val result = dao.observeMetadata("widget1").first()
        assertEquals(false, result?.isVisible)
    }

    @Test
    fun deleteMetadata_removesEntity() = runTest {
        val entity = WidgetMetadataEntity("widget1", 0, true)
        dao.upsertMetadata(entity)

        dao.deleteMetadata(entity)

        val result = dao.observeMetadata("widget1").first()
        assertNull(result)
    }

    @Test
    fun observeAllMetadata_returnsEmptyListWhenEmpty() = runTest {
        val result = dao.observeAllMetadata().first()

        assertEquals(0, result.size)
    }
}