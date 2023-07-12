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
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ModuleItemDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var moduleItemDao: ModuleItemDao
    private lateinit var moduleObjectDao: ModuleObjectDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        moduleItemDao = db.moduleItemDao()
        moduleObjectDao = db.moduleObjectDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByModuleIdAndOrderByPosition() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 2), 1))

        val entities = listOf(
            ModuleItemEntity(ModuleItem(id = 1), 1),
            ModuleItemEntity(ModuleItem(id = 2, position = 1), 2),
            ModuleItemEntity(ModuleItem(id = 3, position = 3), 2),
            ModuleItemEntity(ModuleItem(id = 4, position = 2), 2)
        )

        moduleItemDao.insertAll(entities)

        val result = moduleItemDao.findByModuleId(2)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(2L, result[0].id)
        Assert.assertEquals(4L, result[1].id)
        Assert.assertEquals(3L, result[2].id)
    }

    @Test
    fun testCascadeWhenModuleObjectDeleted() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))

        val result = moduleItemDao.findByModuleId(1)
        Assert.assertEquals(1, result.size)

        moduleObjectDao.delete(ModuleObjectEntity(ModuleObject(id = 1), 1))

        val resultAfterDelete = moduleItemDao.findByModuleId(1)
        Assert.assertEquals(0, resultAfterDelete.size)
    }
}