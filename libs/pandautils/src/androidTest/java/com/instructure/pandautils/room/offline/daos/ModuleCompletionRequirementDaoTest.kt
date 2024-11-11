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
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.*
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
class ModuleCompletionRequirementDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var moduleCompletionRequirementDao: ModuleCompletionRequirementDao
    private lateinit var moduleObjectDao: ModuleObjectDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        moduleCompletionRequirementDao = db.moduleCompletionRequirementDao()
        moduleObjectDao = db.moduleObjectDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(id = 1)))
            moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByModuleId() = runTest {
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 2), 1))

        moduleCompletionRequirementDao.insert(
            ModuleCompletionRequirementEntity(
                ModuleCompletionRequirement(id = 1, minScore = 10.0), 1, 1
            )
        )

        moduleCompletionRequirementDao.insert(
            ModuleCompletionRequirementEntity(
                ModuleCompletionRequirement(id = 2, minScore = 20.0), 2, 1
            )
        )

        val result = moduleCompletionRequirementDao.findByModuleId(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(1L, result[0].id)
        Assert.assertEquals(10.0, result[0].minScore, 0.0)
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleCompletionRequirementDao.insert(
            ModuleCompletionRequirementEntity(
                ModuleCompletionRequirement(id = 1, minScore = 10.0), 1, 1
            )
        )

        moduleCompletionRequirementDao.insert(
            ModuleCompletionRequirementEntity(
                ModuleCompletionRequirement(id = 2, minScore = 20.0), 1, 1
            )
        )

        val result = moduleCompletionRequirementDao.findById(1)

        Assert.assertEquals(1L, result!!.id)
        Assert.assertEquals(10.0, result.minScore, 0.0)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        moduleCompletionRequirementDao.insert(
            ModuleCompletionRequirementEntity(ModuleCompletionRequirement(id = 1, minScore = 10.0), 2, 2)
        )
    }

    @Test
    fun testModuleItemCascade() = runTest {
        moduleCompletionRequirementDao.insert(
            ModuleCompletionRequirementEntity(ModuleCompletionRequirement(id = 1, minScore = 10.0), 1, 1)
        )

        courseDao.deleteByIds(listOf(1))

        val result = moduleCompletionRequirementDao.findByModuleId(1)

        Assert.assertTrue(result.isEmpty())
    }
}