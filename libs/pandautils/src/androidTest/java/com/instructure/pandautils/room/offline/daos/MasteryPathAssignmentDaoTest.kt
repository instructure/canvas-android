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
import com.instructure.canvasapi2.models.AssignmentSet
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.canvasapi2.models.MasteryPathAssignment
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentSetEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathAssignmentEntity
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
class MasteryPathAssignmentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var masteryPathAssignmentDao: MasteryPathAssignmentDao
    private lateinit var assignmentSetDao: AssignmentSetDao
    private lateinit var masteryPathDao: MasteryPathDao
    private lateinit var moduleItemDao: ModuleItemDao
    private lateinit var moduleObjectDao: ModuleObjectDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        masteryPathAssignmentDao = db.masteryPathAssignmentDao()
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
    fun testFindByAssignmentSetId() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))
        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 2), 1 ))

        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 1))
        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 2))

        assignmentSetDao.insert(AssignmentSetEntity(AssignmentSet(id = 1), 1))
        assignmentSetDao.insert(AssignmentSetEntity(AssignmentSet(id = 2), 2))

        masteryPathAssignmentDao.insert(
            MasteryPathAssignmentEntity(MasteryPathAssignment(id = 1, assignmentSetId = 1))
        )

        masteryPathAssignmentDao.insert(
            MasteryPathAssignmentEntity(MasteryPathAssignment(id = 2, assignmentSetId = 2))
        )

        val result = masteryPathAssignmentDao.findByAssignmentSetId(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(1, result[0].id)
    }

    @Test
    fun testCascadeWhenAssignmentSetIsDeleted() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(id = 1), 1))

        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 1), 1 ))
        moduleItemDao.insert(ModuleItemEntity(ModuleItem(id = 2), 1 ))

        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 1))
        masteryPathDao.insert(MasteryPathEntity(MasteryPath(), 2))

        assignmentSetDao.insert(AssignmentSetEntity(AssignmentSet(id = 1), 1))
        assignmentSetDao.insert(AssignmentSetEntity(AssignmentSet(id = 2), 2))

        masteryPathAssignmentDao.insert(
            MasteryPathAssignmentEntity(MasteryPathAssignment(id = 1, assignmentSetId = 1))
        )

        masteryPathAssignmentDao.insert(
            MasteryPathAssignmentEntity(MasteryPathAssignment(id = 2, assignmentSetId = 2))
        )

        val result = masteryPathAssignmentDao.findByAssignmentSetId(1)
        Assert.assertEquals(1, result.size)
        Assert.assertEquals(1, result[0].id)

        assignmentSetDao.delete(AssignmentSetEntity(AssignmentSet(id = 1), 1))

        val resultAferDelete = masteryPathAssignmentDao.findByAssignmentSetId(1)
        Assert.assertEquals(0, resultAferDelete.size)
    }
}