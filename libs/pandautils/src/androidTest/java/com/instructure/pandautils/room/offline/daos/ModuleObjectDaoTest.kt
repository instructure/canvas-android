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
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleObjectDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var moduleObjectDao: ModuleObjectDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        moduleObjectDao = db.moduleObjectDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByCourseIdAndOrderByPosition() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        courseDao.insert(CourseEntity(Course(id = 2)))

        val entities = listOf(
            ModuleObjectEntity(ModuleObject(id = 1), 1),
            ModuleObjectEntity(ModuleObject(id = 2, position = 1), 2),
            ModuleObjectEntity(ModuleObject(id = 3, position = 3), 2),
            ModuleObjectEntity(ModuleObject(id = 4, position = 2), 2)
        )

        moduleObjectDao.insertAll(entities)

        val result = moduleObjectDao.findByCourseId(2)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(2L, result[0].id)
        Assert.assertEquals(4L, result[1].id)
        Assert.assertEquals(3L, result[2].id)
    }

    @Test
    fun testCascadeWhenModuleObjectDeleted() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        val result = moduleObjectDao.findByCourseId(1)
        Assert.assertEquals(1, result.size)

        courseDao.delete(CourseEntity(Course(id = 1)))

        val resultAfterDelete = moduleObjectDao.findByCourseId(1)
        Assert.assertEquals(0, resultAfterDelete.size)
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))

        val entities = listOf(
            ModuleObjectEntity(ModuleObject(id = 1), 1),
            ModuleObjectEntity(ModuleObject(id = 2, position = 1), 1),
            ModuleObjectEntity(ModuleObject(id = 3, position = 3), 1),
            ModuleObjectEntity(ModuleObject(id = 4, position = 2), 1)
        )

        moduleObjectDao.insertAll(entities)

        val result = moduleObjectDao.findById(2)

        Assert.assertEquals(2, result!!.id)
        Assert.assertEquals(1, result.position)
        Assert.assertEquals(1, result.courseId)
    }

    @Test
    fun testFindByIdReturnsNullWhenNotFound() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))

        val entities = listOf(
            ModuleObjectEntity(ModuleObject(id = 1), 1),
            ModuleObjectEntity(ModuleObject(id = 2, position = 1), 1),
            ModuleObjectEntity(ModuleObject(id = 3, position = 3), 1),
            ModuleObjectEntity(ModuleObject(id = 4, position = 2), 1)
        )

        moduleObjectDao.insertAll(entities)

        val result = moduleObjectDao.findById(55)

        Assert.assertNull(result)
    }
}