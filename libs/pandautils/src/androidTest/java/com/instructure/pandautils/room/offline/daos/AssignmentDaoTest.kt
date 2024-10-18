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
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssignmentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        assignmentDao = db.assignmentDao()
        assignmentGroupDao = db.assignmentGroupDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val assignmentGroupEntity = AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L)
        assignmentGroupDao.insert(assignmentGroupEntity)

        val assignmentEntity =
            AssignmentEntity(Assignment(id = 1L, name = "assignmentEntity", assignmentGroupId = 1L, courseId = 1L), null, null, null, null)
        val updated = assignmentEntity.copy(name = "updated")

        assignmentDao.insert(assignmentEntity)
        assignmentDao.insert(updated)

        val result = assignmentDao.findById(1L)

        assertEquals(updated.name, result?.name)
    }

    @Test
    fun testFindById() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val assignmentGroupEntity = AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L)
        assignmentGroupDao.insert(assignmentGroupEntity)

        val assignmentEntity =
            AssignmentEntity(Assignment(id = 1L, assignmentGroupId = 1L, courseId = 1L), null, null, null, null)
        val assignmentEntity2 =
            AssignmentEntity(Assignment(id = 2L, assignmentGroupId = 1L, courseId = 1L), null, null, null, null)
        assignmentDao.insert(assignmentEntity)
        assignmentDao.insert(assignmentEntity2)

        val result = assignmentDao.findById(2L)

        assertEquals(assignmentEntity2.id, result?.id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testAssignmentGroupForeignKey() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val assignmentEntity =
            AssignmentEntity(Assignment(id = 1L, assignmentGroupId = 1L, courseId = 1L), null, null, null, null)
        assignmentDao.insert(assignmentEntity)
    }

    @Test
    fun testCourseCascade() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val assignmentGroupEntity = AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L)
        assignmentGroupDao.insert(assignmentGroupEntity)

        val assignmentEntity =
            AssignmentEntity(Assignment(id = 1L, assignmentGroupId = 1L, courseId = 1L), null, null, null, null)
        assignmentDao.insert(assignmentEntity)

        courseDao.delete(courseEntity)

        val result = assignmentDao.findById(1L)

        assertNull(result)
    }

    @Test
    fun testAssignmentGroupCascade() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val assignmentGroupEntity = AssignmentGroupEntity(AssignmentGroup(id = 1L), 1L)
        assignmentGroupDao.insert(assignmentGroupEntity)

        val assignmentEntity = AssignmentEntity(
            Assignment(id = 1L, assignmentGroupId = 1L, courseId = 1L),
            null, null, null, null
        )
        assignmentDao.insertOrUpdate(assignmentEntity)

        assignmentGroupDao.delete(assignmentGroupEntity)

        val result = assignmentDao.findById(1L)

        assertNull(result)
    }
}