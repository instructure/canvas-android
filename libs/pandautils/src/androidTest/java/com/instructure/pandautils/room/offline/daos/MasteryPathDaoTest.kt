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
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathEntity
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MasteryPathDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var masteryPathDao: MasteryPathDao
    private lateinit var moduleItemDao: ModuleItemDao
    private lateinit var moduleObjectDao: ModuleObjectDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        masteryPathDao = db.masteryPathDao()
        moduleItemDao = db.moduleItemDao()
        moduleObjectDao = db.moduleObjectDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))
        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 2), 1 ))

        masteryPathDao.insert(MasteryPathEntity(MasteryPath(isLocked = true), 1))
        masteryPathDao.insert(MasteryPathEntity(MasteryPath(isLocked = false), 2))

        val result = masteryPathDao.findById(1)

        Assert.assertEquals(1L, result?.id)
        Assert.assertTrue(result!!.isLocked)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testModuleItemForeignKeyRequired() = runTest {
        masteryPathDao.insert(MasteryPathEntity(MasteryPath(isLocked = true), 1))
    }

    @Test
    fun testCascadeWhenModuleItemDeleted() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))

        masteryPathDao.insert(MasteryPathEntity(MasteryPath(isLocked = true), 1))

        val result = masteryPathDao.findById(1)
        Assert.assertEquals(1L, result?.id)
        Assert.assertTrue(result!!.isLocked)

        moduleItemDao.delete(ModuleItemEntity(ModuleItem(id = 1), 1 ))

        val resultAfterDelete = masteryPathDao.findById(1)
        Assert.assertNull(resultAfterDelete)
    }
}