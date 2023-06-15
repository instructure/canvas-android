/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.ScheduleItemEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ScheduleItemDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var scheduleItemDao: ScheduleItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        scheduleItemDao = db.scheduleItemDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val scheduleItemEntity = ScheduleItemEntity(ScheduleItem(itemId = "event_1", title = "schedule item"))
        val updated = scheduleItemEntity.copy(title = "updated")

        scheduleItemDao.insert(scheduleItemEntity)
        scheduleItemDao.insert(updated)

        val result = scheduleItemDao.findById("event_1")

        assertEquals(updated, result)
    }

    @Test
    fun testFindById() = runTest {
        val scheduleItemEntity = ScheduleItemEntity(ScheduleItem(itemId = "event_1", title = "schedule item"))
        val scheduleItemEntity2 = ScheduleItemEntity(ScheduleItem(itemId = "event_2", title = "schedule item"))

        scheduleItemDao.insert(scheduleItemEntity)
        scheduleItemDao.insert(scheduleItemEntity2)

        val result = scheduleItemDao.findById("event_2")

        assertEquals(scheduleItemEntity2, result)
    }

    @Test
    fun testFindByItemType() = runTest {
        val assignmentEvent = ScheduleItemEntity(ScheduleItem(itemId = "event_1", title = "schedule item", type = "assignment", contextCode = "course_1"))
        val assignmentEvent2 = ScheduleItemEntity(ScheduleItem(itemId = "event_2", title = "schedule item", type = "assignment", contextCode = "course_1"))
        val calendarEvent = ScheduleItemEntity(ScheduleItem(itemId = "event_3", title = "schedule item", type = "calendar", contextCode = "course_1"))

        scheduleItemDao.insert(assignmentEvent)
        scheduleItemDao.insert(assignmentEvent2)
        scheduleItemDao.insert(calendarEvent)

        val result = scheduleItemDao.findByItemType(listOf("course_1"), "assignment")

        assertEquals(listOf(assignmentEvent, assignmentEvent2), result)
    }
}