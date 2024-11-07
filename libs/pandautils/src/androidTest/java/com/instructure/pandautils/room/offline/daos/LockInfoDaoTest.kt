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
class LockInfoDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var lockInfoDao: LockInfoDao
    private lateinit var courseDao: CourseDao
    private lateinit var pageDao: PageDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var moduleContentDetailsDao: ModuleContentDetailsDao
    private lateinit var moduleItemDao: ModuleItemDao
    private lateinit var moduleObjectDao: ModuleObjectDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        lockInfoDao = db.lockInfoDao()
        courseDao = db.courseDao()
        pageDao = db.pageDao()
        assignmentGroupDao = db.assignmentGroupDao()
        assignmentDao = db.assignmentDao()
        moduleContentDetailsDao = db.moduleContentDetailsDao()
        moduleItemDao = db.moduleItemDao()
        moduleObjectDao = db.moduleObjectDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1L)))
            pageDao.insert(PageEntity(Page(1L), 1L))
            pageDao.insert(PageEntity(Page(2L), 1L))
            assignmentGroupDao.insert(AssignmentGroupEntity(AssignmentGroup(1L), 1L))
            assignmentDao.insert(AssignmentEntity(Assignment(1L, assignmentGroupId = 1L), null, null, null, null))
            assignmentDao.insert(AssignmentEntity(Assignment(2L, assignmentGroupId = 1L), null, null, null, null))
            moduleObjectDao.insert(ModuleObjectEntity(ModuleObject(1L), 1L))
            moduleItemDao.insert(ModuleItemEntity(ModuleItem(1L), 1L))
            moduleItemDao.insert(ModuleItemEntity(ModuleItem(2L), 1L))
            moduleContentDetailsDao.insert(ModuleContentDetailsEntity(ModuleContentDetails("Points"), 1L))
            moduleContentDetailsDao.insert(ModuleContentDetailsEntity(ModuleContentDetails("Points 2"), 2L))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByModuleId() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), moduleId = 1))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "2"), moduleId = 2))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "3"), assignmentId = 1))

        val result = lockInfoDao.findByModuleId(1)

        Assert.assertEquals("1", result!!.unlockAt)
    }

    @Test
    fun testFindByAssignmentId() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), moduleId = 1))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "2"), moduleId = 2))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "3"), assignmentId = 1))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "4"), assignmentId = 2))

        val result = lockInfoDao.findByAssignmentId(2)

        Assert.assertEquals("4", result!!.unlockAt)
    }

    @Test
    fun testFindByPageId() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), moduleId = 1))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "2"), moduleId = 2))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "3"), assignmentId = 1))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "4"), assignmentId = 2))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "5"), pageId = 1))
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "6"), pageId = 2))

        val result = lockInfoDao.findByPageId(2)

        Assert.assertEquals("6", result!!.unlockAt)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testModuleForeignKey() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), moduleId = 3))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testAssignmentForeignKey() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), assignmentId = 3))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testPageForeignKey() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), pageId = 3))
    }

    @Test
    fun testModuleCascade() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), moduleId = 1))

        moduleContentDetailsDao.delete(ModuleContentDetailsEntity(ModuleContentDetails("Points"), 1L))

        val result = lockInfoDao.findByModuleId(1)

        Assert.assertNull(result)
    }

    @Test
    fun testAssignmentCascade() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), assignmentId = 1))

        assignmentDao.delete(AssignmentEntity(Assignment(1L, assignmentGroupId = 1L), null, null, null, null))

        val result = lockInfoDao.findByModuleId(1)

        Assert.assertNull(result)
    }

    @Test
    fun testPageCascade() = runTest {
        lockInfoDao.insert(LockInfoEntity(LockInfo(unlockAt = "1"), pageId = 1))

        pageDao.delete(PageEntity(Page(1L), 1L))

        val result = lockInfoDao.findByModuleId(1)

        Assert.assertNull(result)
    }

    @Test
    fun testFindByRowId() = runTest {
        val expected = LockInfoEntity(LockInfo(unlockAt = "1"), assignmentId = 1)

        val rowId = lockInfoDao.insert(expected)

        val result = lockInfoDao.findByRowId(rowId)

        Assert.assertEquals(expected.assignmentId, result?.assignmentId)
    }
}