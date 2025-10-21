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
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.GradingPeriodEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradingPeriodDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var gradingPeriodDao: GradingPeriodDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        gradingPeriodDao = db.gradingPeriodDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityById() = runTest {
        val gradingPeriodEntity = GradingPeriodEntity(GradingPeriod(id = 1, "Grading period 1"))
        val gradingPeriodEntity2 = GradingPeriodEntity(GradingPeriod(id = 2, "Grading period 2"))
        gradingPeriodDao.insert(gradingPeriodEntity)
        gradingPeriodDao.insert(gradingPeriodEntity2)

        val result = gradingPeriodDao.findById(1)

        Assert.assertEquals(gradingPeriodEntity, result)
    }

    @Test(expected = IllegalStateException::class)
    fun testFindEntityByIdThrowsExceptionIfNotFound() = runTest {
        val gradingPeriodEntity = GradingPeriodEntity(GradingPeriod(id = 1, "Grading period 1"))
        val gradingPeriodEntity2 = GradingPeriodEntity(GradingPeriod(id = 2, "Grading period 2"))
        gradingPeriodDao.insert(gradingPeriodEntity)
        gradingPeriodDao.insert(gradingPeriodEntity2)

        val result = gradingPeriodDao.findById(3)
    }
}