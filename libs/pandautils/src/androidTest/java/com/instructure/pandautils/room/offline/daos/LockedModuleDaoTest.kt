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
class LockedModuleDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var lockedModuleDao: LockedModuleDao
    private lateinit var courseDao: CourseDao
    private lateinit var pageDao: PageDao
    private lateinit var lockInfoDao: LockInfoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        lockedModuleDao = db.lockedModuleDao()
        courseDao = db.courseDao()
        pageDao = db.pageDao()
        lockInfoDao = db.lockInfoDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1L)))
            pageDao.insert(PageEntity(Page(1L), 1L))
            lockInfoDao.insert(LockInfoEntity(LockInfo(contextModule = LockedModule(1L)), pageId = 1L))
            lockInfoDao.insert(LockInfoEntity(LockInfo(contextModule = LockedModule(2L)), pageId = 1L))
            lockInfoDao.insert(LockInfoEntity(LockInfo(contextModule = LockedModule(3L)), pageId = 1L))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        val expected = LockedModuleEntity(LockedModule(id = 1), 1L)

        lockedModuleDao.insert(expected)
        lockedModuleDao.insert(LockedModuleEntity(LockedModule(id = 2), 2L))
        lockedModuleDao.insert(LockedModuleEntity(LockedModule(id = 3), 3L))

        val result = lockedModuleDao.findById(1)

        Assert.assertEquals(expected, result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testLockInfoForeignKey() = runTest {
        lockedModuleDao.insert(LockedModuleEntity(LockedModule(id = 4), 4L))
    }

    @Test
    fun testLockInfoCascade() = runTest {
        val rowId = lockInfoDao.insert(LockInfoEntity(LockInfo(contextModule = LockedModule(1))))
        val id = lockInfoDao.findByRowId(rowId)?.id ?: 0

        lockedModuleDao.insert(LockedModuleEntity(LockedModule(id = 1), id))

        lockInfoDao.delete(LockInfoEntity(id, null, null, null, null, null, null))

        val result = lockedModuleDao.findById(1)

        Assert.assertNull(result)
    }
}
