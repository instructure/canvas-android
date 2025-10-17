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

package com.instructure.pandautils.room.offline.facade

import androidx.room.withTransaction
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.AssignmentOverrideDao
import com.instructure.pandautils.room.offline.daos.ScheduleItemAssignmentOverrideDao
import com.instructure.pandautils.room.offline.daos.ScheduleItemDao
import com.instructure.pandautils.room.offline.entities.AssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.ScheduleItemAssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.ScheduleItemEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScheduleItemFacadeTest {
    private val scheduleItemDao: ScheduleItemDao = mockk(relaxed = true)
    private val assignmentOverrideDao: AssignmentOverrideDao = mockk(relaxed = true)
    private val scheduleItemAssignmentOverrideDao: ScheduleItemAssignmentOverrideDao = mockk(relaxed = true)
    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)

    private lateinit var scheduleItemFacade: ScheduleItemFacade

    @Before
    fun setup() {
        scheduleItemFacade = ScheduleItemFacade(
            scheduleItemDao,
            assignmentOverrideDao,
            scheduleItemAssignmentOverrideDao,
            assignmentFacade,
            offlineDatabase
        )

        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { offlineDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Insert schedule item`() = runTest {
        val scheduleItem = ScheduleItem(
            itemId = "event_1",
            assignment = Assignment(id = 1L),
            assignmentOverrides = listOf(
                AssignmentOverride(id = 1L)
            )
        )

        scheduleItemFacade.insertScheduleItems(listOf(scheduleItem), 1L)

        coVerify(exactly = 1) {
            scheduleItemDao.insert(ScheduleItemEntity(scheduleItem, 1L))
            assignmentOverrideDao.insert(AssignmentOverrideEntity(scheduleItem.assignmentOverrides?.first()!!))
            scheduleItemAssignmentOverrideDao.insert(ScheduleItemAssignmentOverrideEntity(1L, "event_1"))
        }
    }

    @Test
    fun `Find by item type`() = runTest {
        val assignment = Assignment(id = 1L)
        val assignmentOverrides = listOf(AssignmentOverride(id = 1L))
        val scheduleItem = ScheduleItem(
            itemId = "event_1",
            assignment = assignment,
            assignmentOverrides = assignmentOverrides
        )
        coEvery { scheduleItemDao.findByItemType(any(), any()) } returns listOf(
            ScheduleItemEntity(scheduleItem, 1L)
        )

        coEvery { assignmentFacade.getAssignmentById(any()) } returns assignment
        coEvery { scheduleItemAssignmentOverrideDao.findByScheduleItemId(any()) } returns listOf(ScheduleItemAssignmentOverrideEntity(1L, "event_1"))
        coEvery { assignmentOverrideDao.findByIds(any()) } returns listOf(AssignmentOverrideEntity(assignmentOverrides.first()))

        val result = scheduleItemFacade.findByItemType(listOf("course_1"), CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName)

        assertEquals(listOf(scheduleItem), result)

        coVerify {
            scheduleItemDao.findByItemType(listOf("course_1"), CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName)
            assignmentFacade.getAssignmentById(1L)
            scheduleItemAssignmentOverrideDao.findByScheduleItemId("event_1")
            assignmentOverrideDao.findByIds(listOf(1L))
        }
    }
}