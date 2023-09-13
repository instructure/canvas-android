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
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.LockInfoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LockInfoDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var lockInfoDao: LockInfoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        lockInfoDao = db.lockInfoDao()
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
}