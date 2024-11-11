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
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
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

@RunWith(AndroidJUnit4::class)
class AssignmentGroupDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        assignmentGroupDao = db.assignmentGroupDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1)))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        val assignmentGroupEntity = AssignmentGroupEntity(
            id = 1L,
            name = "Name 1",
            position = 0,
            groupWeight = 0.0,
            rules = null,
            1L
        )

        val assignmentGroupEntity2 = assignmentGroupEntity.copy(id = 2L, name = "Name 2")

        assignmentGroupDao.insert(assignmentGroupEntity)
        assignmentGroupDao.insert(assignmentGroupEntity2)

        val result = assignmentGroupDao.findById(1L)

        Assert.assertEquals(assignmentGroupEntity, result)
    }

    @Test
    fun testFindByIdReturnsNullIfNotFound() = runTest {
        val assignmentGroupEntity = AssignmentGroupEntity(
            id = 1L,
            name = "Name 1",
            position = 0,
            groupWeight = 0.0,
            rules = null,
            1L
        )

        val assignmentGroupEntity2 = assignmentGroupEntity.copy(id = 2L, name = "Name 2")

        assignmentGroupDao.insert(assignmentGroupEntity)
        assignmentGroupDao.insert(assignmentGroupEntity2)

        val result = assignmentGroupDao.findById(3L)

        Assert.assertNull(result)
    }

    @Test
    fun testInsertReplace() = runTest {
        val assignmentGroupEntity = AssignmentGroupEntity(
            id = 1L,
            name = "Name 1",
            position = 0,
            groupWeight = 0.0,
            rules = null,
            1L
        )

        assignmentGroupDao.insert(assignmentGroupEntity)

        val updated = assignmentGroupEntity.copy(position = 1)
        assignmentGroupDao.insert(updated)

        val result = assignmentGroupDao.findById(1L)

        Assert.assertEquals(updated, result)
    }

    @Test
    fun testDeleteAllByCourseId() = runTest {
        val assignmentGroupEntity = AssignmentGroupEntity(
            id = 1L,
            name = "Name 1",
            position = 0,
            groupWeight = 0.0,
            rules = null,
            1L
        )
        assignmentGroupDao.insert(assignmentGroupEntity)

        val result = assignmentGroupDao.findById(1L)

        Assert.assertEquals(assignmentGroupEntity, result)

        assignmentGroupDao.deleteAllByCourseId(1L)

        val deletedResult = assignmentGroupDao.findById(1L)

        Assert.assertNull(deletedResult)
    }
}