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
package com.instructure.pandautils.room.appdatabase.daos

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardFileUploadDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dashboardFileUploadDao: DashboardFileUploadDao

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dashboardFileUploadDao = db.dashboardFileUploadDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getCorrectDataForUserId() = runTest {
        val uploadForUser1 = DashboardFileUploadEntity("1", 1, title = "Upload", null, null, null, null, null)
        val upload2ForUser1 = DashboardFileUploadEntity("2", 1, title = "Upload 2", null, null, null, null, null)
        val uploadForUser2 = DashboardFileUploadEntity("3", 2, title = "We don't need this", null, null, null, null, null)
        dashboardFileUploadDao.insert(uploadForUser1)
        dashboardFileUploadDao.insert(upload2ForUser1)
        dashboardFileUploadDao.insert(uploadForUser2)

        val result = dashboardFileUploadDao.getAllForUser(1)

        result.observeForever {
            Assert.assertEquals(2, it.size)
            Assert.assertEquals(uploadForUser1, it.get(0))
            Assert.assertEquals(upload2ForUser1, it.get(1))
        }
    }

    @Test
    fun deleteByWorkerIdDeletesCorrectItem() = runTest {
        val uploadForUser1 = DashboardFileUploadEntity("1", 1, title = "Upload", null, null, null, null, null)
        val uploadForUser2 = DashboardFileUploadEntity("3", 2, title = "We don't need this", null, null, null, null, null)
        dashboardFileUploadDao.insert(uploadForUser1)
        dashboardFileUploadDao.insert(uploadForUser2)

        dashboardFileUploadDao.deleteByWorkerId("1")
        val result = dashboardFileUploadDao.getAllForUser(1)

        result.observeForever {
            Assert.assertEquals(0, it.size)
        }
    }
}