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
import com.instructure.canvasapi2.models.AssignmentSet
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentSetEntity
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
class AssignmentSetDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var assignmentSetDao: AssignmentSetDao
    private lateinit var masteryPathDao: MasteryPathDao
    private lateinit var moduleItemDao: ModuleItemDao
    private lateinit var moduleObjectDao: ModuleObjectDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        assignmentSetDao = db.assignmentSetDao()
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
    fun testFindByMasteryPathId() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))
        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 2), 1 ))

        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 1))
        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 2))

        val entities = listOf(
            AssignmentSetEntity(AssignmentSet(id = 1, createdAt = "2020-01-01T00:00:00Z"), 1),
            AssignmentSetEntity(AssignmentSet(id = 2, createdAt = "2021-01-01T00:00:00Z"), 2),
            AssignmentSetEntity(AssignmentSet(id = 3, createdAt = "2022-01-01T00:00:00Z"), 1),
        )
        entities.forEach {
            assignmentSetDao.insert(it)
        }

        val result = assignmentSetDao.findByMasteryPathId(1)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(1, result[0].id)
        Assert.assertEquals(3, result[1].id)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testMasteryPathForeignKeyRequired() = runTest {
        assignmentSetDao.insert(
            AssignmentSetEntity(
                AssignmentSet(
                    id = 1,
                    createdAt = "2020-01-01T00:00:00Z"
                ), 1
            )
        )
    }

    @Test
    fun testCascadeWhenMasteryPathDeleted() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))

        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 1))

        val entities = listOf(
            AssignmentSetEntity(AssignmentSet(id = 1, createdAt = "2020-01-01T00:00:00Z"), 1),
            AssignmentSetEntity(AssignmentSet(id = 3, createdAt = "2022-01-01T00:00:00Z"), 1),
        )
        entities.forEach {
            assignmentSetDao.insert(it)
        }

        val result = assignmentSetDao.findByMasteryPathId(1)
        Assert.assertEquals(2, result.size)

        masteryPathDao.delete(MasteryPathEntity(MasteryPath(), 1))
        val resultAfterDelete = assignmentSetDao.findByMasteryPathId(1)
        Assert.assertEquals(0, resultAfterDelete.size)
    }
}