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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.AssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssignmentOverrideDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var assignmentOverrideDao: AssignmentOverrideDao
    private lateinit var assignmentDao: AssignmentDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        assignmentOverrideDao = db.assignmentOverrideDao()
        assignmentDao = db.assignmentDao()
        db.courseDao().insert(CourseEntity(Course(id = 1L)))
        db.assignmentGroupDao().insert(AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val assignmentEntity = AssignmentEntity(Assignment(1L, courseId = 1L, assignmentGroupId = 1L), null, null, null, null)
        assignmentDao.insert(assignmentEntity)

        val assignmentOverrideEntity =
            AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L, title = "assignment override"))
        val updated = assignmentOverrideEntity.copy(title = "updated")

        assignmentOverrideDao.insert(assignmentOverrideEntity)
        assignmentOverrideDao.insert(updated)

        val result = assignmentOverrideDao.findByIds(listOf(1L))

        assertEquals(listOf(updated), result)
    }

    @Test
    fun testFindByIds() = runTest {
        val assignmentEntity = AssignmentEntity(Assignment(1L, courseId = 1L, assignmentGroupId = 1L), null, null, null, null)
        assignmentDao.insert(assignmentEntity)

        val assignmentOverrideEntity =
            AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L, title = "assignment override"))
        val assignmentOverrideEntity2 =
            AssignmentOverrideEntity(AssignmentOverride(id = 2L, assignmentId = 1L, title = "assignment override"))
        val assignmentOverrideEntity3 =
            AssignmentOverrideEntity(AssignmentOverride(id = 3L, assignmentId = 1L, title = "assignment override"))

        assignmentOverrideDao.insert(assignmentOverrideEntity)
        assignmentOverrideDao.insert(assignmentOverrideEntity2)
        assignmentOverrideDao.insert(assignmentOverrideEntity3)

        val result = assignmentOverrideDao.findByIds(listOf(2L, 3L))

        assertEquals(listOf(assignmentOverrideEntity2, assignmentOverrideEntity3), result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testAssignmentForeignKey() = runTest {
        val assignmentOverrideEntity =
            AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L, title = "assignment override"))

        assignmentOverrideDao.insert(assignmentOverrideEntity)
    }

    @Test
    fun testAssignmentCascade() = runTest {
        val assignmentEntity = AssignmentEntity(Assignment(1L, courseId = 1L, assignmentGroupId = 1L), null, null, null, null)
        assignmentDao.insert(assignmentEntity)

        val assignmentOverrideEntity =
            AssignmentOverrideEntity(AssignmentOverride(id = 1L, assignmentId = 1L, title = "assignment override"))

        assignmentOverrideDao.insert(assignmentOverrideEntity)

        assignmentDao.delete(assignmentEntity)

        val result = assignmentOverrideDao.findByIds(listOf(1L))

        assert(result.isEmpty())
    }
}