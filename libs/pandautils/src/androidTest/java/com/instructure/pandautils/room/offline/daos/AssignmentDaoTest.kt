/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.room.offline.daos

import android.content.Context
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AssignmentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var courseDao: CourseDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        assignmentDao = db.assignmentDao()
        courseDao = db.courseDao()
        assignmentGroupDao = db.assignmentGroupDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(id = 1L)))
            courseDao.insert(CourseEntity(Course(id = 2L)))
            assignmentGroupDao.insert(AssignmentGroupEntity(AssignmentGroup(id = 1L)))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        val assignmentEntity = AssignmentEntity(
            Assignment(
                id = 1L,
                name = "Assignment 1",
                courseId = 1L,
                assignmentGroupId = 1L
            ),
            null,
            null,
            null,
            null
        )

        val assignmentEntity2 = assignmentEntity.copy(id = 2L, name = "Name 2")

        assignmentDao.insert(assignmentEntity)
        assignmentDao.insert(assignmentEntity2)

        val result = assignmentDao.findById(1L)!!

        Assert.assertEquals(assignmentEntity.id, result.id)
        Assert.assertEquals(assignmentEntity.name, result.name)
        Assert.assertEquals(assignmentEntity.courseId, result.courseId)
        Assert.assertEquals(assignmentEntity.assignmentGroupId, result.assignmentGroupId)
    }

    @Test
    fun testFindByIdReturnsNullIfNotFound() = runTest {
        val assignmentEntity = AssignmentEntity(
            Assignment(
                id = 1L,
                name = "Assignment 1",
                courseId = 1L,
                assignmentGroupId = 1L
            ),
            null,
            null,
            null,
            null
        )

        assignmentDao.insert(assignmentEntity)

        val result = assignmentDao.findById(2L)

        Assert.assertNull(result)
    }

    @Test
    fun testInsertReplace() = runTest {
        val assignmentEntity = AssignmentEntity(
            Assignment(
                id = 1L,
                name = "Assignment 1",
                courseId = 1L,
                assignmentGroupId = 1L
            ),
            null,
            null,
            null,
            null
        )

        assignmentDao.insert(assignmentEntity)

        val updated = assignmentEntity.copy(name = "Assignment updated name")
        assignmentDao.insert(updated)

        val result = assignmentDao.findById(1L)!!

        Assert.assertEquals(updated.id, result.id)
        Assert.assertEquals(updated.name, result.name)
        Assert.assertEquals(updated.courseId, result.courseId)
        Assert.assertEquals(updated.assignmentGroupId, result.assignmentGroupId)
    }

    @Test
    fun testFindByCourseId() = runTest {
        val assignmentEntity = AssignmentEntity(Assignment(id = 1L, name = "Assignment 1", courseId = 1L, assignmentGroupId = 1L), 1L, 1L, 1L, 1L)

        val assignmentEntity2 = assignmentEntity.copy(id = 2L, name = "Name 2", courseId = 2L)

        val assignmentEntity3 = assignmentEntity.copy(id = 3L, name = "Name 3")

        assignmentDao.insert(assignmentEntity)
        assignmentDao.insert(assignmentEntity2)
        assignmentDao.insert(assignmentEntity3)

        val result = assignmentDao.findByCourseId(1L)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(assignmentEntity.id, result.first().id)
        Assert.assertEquals(assignmentEntity.name, result.first().name)
        Assert.assertEquals(assignmentEntity.courseId, result.first().courseId)
        Assert.assertEquals(assignmentEntity.assignmentGroupId, result.first().assignmentGroupId)
    }

    @Test
    fun testFindByCourseIdReturensEmptyListIfNotFound() = runTest {
        val assignmentEntity = AssignmentEntity(Assignment(id = 1L, name = "Assignment 1", courseId = 1L, assignmentGroupId = 1L), 1L, 1L, 1L, 1L)

        val assignmentEntity2 = assignmentEntity.copy(id = 2L, name = "Name 2")

        assignmentDao.insert(assignmentEntity)
        assignmentDao.insert(assignmentEntity2)

        val result = assignmentDao.findByCourseId(2L)

        Assert.assertTrue(result.isEmpty())
    }
}