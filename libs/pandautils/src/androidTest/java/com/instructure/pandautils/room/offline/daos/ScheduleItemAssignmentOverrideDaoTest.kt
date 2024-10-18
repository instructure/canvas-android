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
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduleItemAssignmentOverrideDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var scheduleItemDao: ScheduleItemDao
    private lateinit var assignmentOverrideDao: AssignmentOverrideDao
    private lateinit var scheduleItemAssignmentOverrideDao: ScheduleItemAssignmentOverrideDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        scheduleItemDao = db.scheduleItemDao()
        assignmentOverrideDao = db.assignmentOverrideDao()
        scheduleItemAssignmentOverrideDao = db.scheduleItemAssignmentOverrideDao()
        db.courseDao().insert(CourseEntity(Course(id = 1L)))
        db.assignmentGroupDao().insert(AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L))
        db.assignmentDao().insert(
            AssignmentEntity(
                Assignment(id = 1L, courseId = 1L, assignmentGroupId = 1L),
                null,
                null,
                null,
                null
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByScheduleItemId() = runTest {
        val assignmentOverrideEntity = AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L))
        assignmentOverrideDao.insert(assignmentOverrideEntity)

        val scheduleItemEntity = ScheduleItemEntity(ScheduleItem(itemId = "event1"), 1L)
        scheduleItemDao.insert(scheduleItemEntity)

        val expected = ScheduleItemAssignmentOverrideEntity(1L, "event1")
        scheduleItemAssignmentOverrideDao.insert(expected)

        val result = scheduleItemAssignmentOverrideDao.findByScheduleItemId("event1")

        assertEquals(listOf(expected), result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testScheduleItemForeignKey() = runTest {
        val assignmentOverrideEntity = AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L))
        assignmentOverrideDao.insert(assignmentOverrideEntity)

        val expected = ScheduleItemAssignmentOverrideEntity(1L, "event1")
        scheduleItemAssignmentOverrideDao.insert(expected)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testAssignmentOverrideForeignKey() = runTest {
        val scheduleItemEntity = ScheduleItemEntity(ScheduleItem(itemId = "event1"), 1L)
        scheduleItemDao.insert(scheduleItemEntity)

        val expected = ScheduleItemAssignmentOverrideEntity(1L, "event1")
        scheduleItemAssignmentOverrideDao.insert(expected)
    }

    @Test
    fun testAssignmentOverrideCascade() = runTest {
        val assignmentOverrideEntity = AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L))
        assignmentOverrideDao.insert(assignmentOverrideEntity)

        val scheduleItemEntity = ScheduleItemEntity(ScheduleItem(itemId = "event1"), 1L)
        scheduleItemDao.insert(scheduleItemEntity)

        val expected = ScheduleItemAssignmentOverrideEntity(1L, "event1")
        scheduleItemAssignmentOverrideDao.insert(expected)

        assignmentOverrideDao.delete(assignmentOverrideEntity)

        val result = scheduleItemAssignmentOverrideDao.findByScheduleItemId("event1")

        assert(result.isEmpty())
    }

    @Test
    fun testScheduleItemCascade() = runTest {
        val assignmentOverrideEntity = AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L))
        assignmentOverrideDao.insert(assignmentOverrideEntity)

        val scheduleItemEntity = ScheduleItemEntity(ScheduleItem(itemId = "event1"), 1L)
        scheduleItemDao.insert(scheduleItemEntity)

        val expected = ScheduleItemAssignmentOverrideEntity(1L, "event1")
        scheduleItemAssignmentOverrideDao.insert(expected)

        scheduleItemDao.delete(scheduleItemEntity)

        val result = scheduleItemAssignmentOverrideDao.findByScheduleItemId("event1")

        assert(result.isEmpty())
    }
}