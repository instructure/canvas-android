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
class WidgetConfigDaoTest {

    private lateinit var database: WidgetDatabase
    private lateinit var dao: WidgetConfigDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WidgetDatabase::class.java).build()
        dao = database.widgetConfigDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun upsertConfig_insertsNewEntity() = runTest {
        val entity = WidgetConfigEntity("widget1", """{"key": "value"}""")

        dao.upsertConfig(entity)

        val result = dao.observeConfig("widget1").first()
        assertEquals("widget1", result?.widgetId)
        assertEquals("""{"key": "value"}""", result?.configJson)
    }

    @Test
    fun upsertConfig_updatesExistingEntity() = runTest {
        val entity1 = WidgetConfigEntity("widget1", """{"key": "value1"}""")
        dao.upsertConfig(entity1)

        val entity2 = WidgetConfigEntity("widget1", """{"key": "value2"}""")
        dao.upsertConfig(entity2)

        val result = dao.observeConfig("widget1").first()
        assertEquals("""{"key": "value2"}""", result?.configJson)
    }

    @Test
    fun observeConfig_returnsNullForNonExistent() = runTest {
        val result = dao.observeConfig("nonexistent").first()

        assertNull(result)
    }

    @Test
    fun deleteConfig_removesEntity() = runTest {
        val entity = WidgetConfigEntity("widget1", """{"key": "value"}""")
        dao.upsertConfig(entity)

        dao.deleteConfig(entity)

        val result = dao.observeConfig("widget1").first()
        assertNull(result)
    }
}