/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.CheckpointEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckpointDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var checkpointDao: CheckpointDao
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        checkpointDao = db.checkpointDao()
        assignmentDao = db.assignmentDao()
        assignmentGroupDao = db.assignmentGroupDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndFind() = runTest {
        setupAssignment(1L, 1L)

        val checkpoint = CheckpointEntity(
            assignmentId = 1L,
            name = "Checkpoint 1",
            tag = "reply_to_topic",
            pointsPossible = 10.0,
            dueAt = "2025-10-15T23:59:59Z",
            onlyVisibleToOverrides = false,
            lockAt = null,
            unlockAt = null
        )

        checkpointDao.insert(checkpoint)

        val result = checkpointDao.findByAssignmentId(1L)
        assertEquals(1, result.size)
        assertEquals("Checkpoint 1", result[0].name)
        assertEquals("reply_to_topic", result[0].tag)
        assertEquals(10.0, result[0].pointsPossible)
    }

    @Test
    fun testInsertMultipleCheckpoints() = runTest {
        setupAssignment(1L, 1L)

        val checkpoint1 = CheckpointEntity(
            assignmentId = 1L,
            name = "Reply to Topic",
            tag = "reply_to_topic",
            pointsPossible = 5.0,
            dueAt = "2025-10-15T23:59:59Z",
            onlyVisibleToOverrides = false,
            lockAt = null,
            unlockAt = null
        )

        val checkpoint2 = CheckpointEntity(
            assignmentId = 1L,
            name = "Required Replies",
            tag = "reply_to_entry",
            pointsPossible = 5.0,
            dueAt = "2025-10-20T23:59:59Z",
            onlyVisibleToOverrides = false,
            lockAt = null,
            unlockAt = null
        )

        checkpointDao.insertAll(listOf(checkpoint1, checkpoint2))

        val result = checkpointDao.findByAssignmentId(1L)
        assertEquals(2, result.size)
        assertTrue(result.any { it.tag == "reply_to_topic" })
        assertTrue(result.any { it.tag == "reply_to_entry" })
    }

    @Test
    fun testDeleteByAssignmentId() = runTest {
        setupAssignment(1L, 1L)

        val checkpoint = CheckpointEntity(
            assignmentId = 1L,
            name = "Checkpoint 1",
            tag = "reply_to_topic",
            pointsPossible = 10.0,
            dueAt = "2025-10-15T23:59:59Z",
            onlyVisibleToOverrides = false,
            lockAt = null,
            unlockAt = null
        )

        checkpointDao.insert(checkpoint)
        checkpointDao.deleteByAssignmentId(1L)

        val result = checkpointDao.findByAssignmentId(1L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCascadeDelete() = runTest {
        setupAssignment(1L, 1L)

        val checkpoint = CheckpointEntity(
            assignmentId = 1L,
            name = "Checkpoint 1",
            tag = "reply_to_topic",
            pointsPossible = 10.0,
            dueAt = "2025-10-15T23:59:59Z",
            onlyVisibleToOverrides = false,
            lockAt = null,
            unlockAt = null
        )

        checkpointDao.insert(checkpoint)

        val assignmentEntity = assignmentDao.findById(1L)!!
        assignmentDao.delete(assignmentEntity)

        val result = checkpointDao.findByAssignmentId(1L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testToApiModel() {
        val checkpointEntity = CheckpointEntity(
            assignmentId = 1L,
            name = "Reply to Topic",
            tag = "reply_to_topic",
            pointsPossible = 10.0,
            dueAt = "2025-10-15T23:59:59Z",
            onlyVisibleToOverrides = true,
            lockAt = "2025-10-22T23:59:59Z",
            unlockAt = "2025-10-10T00:00:00Z"
        )

        val checkpoint = checkpointEntity.toApiModel()

        assertEquals("Reply to Topic", checkpoint.name)
        assertEquals("reply_to_topic", checkpoint.tag)
        assertEquals(10.0, checkpoint.pointsPossible)
        assertEquals("2025-10-15T23:59:59Z", checkpoint.dueAt)
        assertEquals(true, checkpoint.onlyVisibleToOverrides)
        assertEquals("2025-10-22T23:59:59Z", checkpoint.lockAt)
        assertEquals("2025-10-10T00:00:00Z", checkpoint.unlockAt)
    }

    @Test
    fun testConstructorFromApiModel() {
        val checkpoint = Checkpoint(
            name = "Reply to Topic",
            tag = "reply_to_topic",
            pointsPossible = 10.0,
            dueAt = "2025-10-15T23:59:59Z",
            overrides = null,
            onlyVisibleToOverrides = true,
            lockAt = "2025-10-22T23:59:59Z",
            unlockAt = "2025-10-10T00:00:00Z"
        )

        val entity = CheckpointEntity(checkpoint, 1L)

        assertEquals(1L, entity.assignmentId)
        assertEquals("Reply to Topic", entity.name)
        assertEquals("reply_to_topic", entity.tag)
        assertEquals(10.0, entity.pointsPossible)
        assertEquals("2025-10-15T23:59:59Z", entity.dueAt)
        assertEquals(true, entity.onlyVisibleToOverrides)
        assertEquals("2025-10-22T23:59:59Z", entity.lockAt)
        assertEquals("2025-10-10T00:00:00Z", entity.unlockAt)
    }

    private suspend fun setupAssignment(assignmentId: Long, courseId: Long) {
        val courseEntity = CourseEntity(Course(id = courseId))
        courseDao.insert(courseEntity)

        val assignmentGroupEntity = AssignmentGroupEntity(AssignmentGroup(id = 1L), courseId)
        assignmentGroupDao.insert(assignmentGroupEntity)

        val assignmentEntity = AssignmentEntity(
            Assignment(id = assignmentId, name = "Test Assignment", assignmentGroupId = 1L, courseId = courseId),
            null, null, null, null
        )
        assignmentDao.insert(assignmentEntity)
    }
}
