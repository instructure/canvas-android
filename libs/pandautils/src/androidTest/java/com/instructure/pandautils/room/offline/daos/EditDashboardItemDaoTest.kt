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
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.EditDashboardItemEntity
import com.instructure.pandautils.room.offline.entities.EnrollmentState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditDashboardItemDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var editDashboardItemDao: EditDashboardItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        editDashboardItemDao = db.editDashboardItemDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindByEnrollmentStateAndOrderByPosition() = runTest {
        val item1 = EditDashboardItemEntity(courseId = 1, name = "Course 1", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 2)
        val item2 = EditDashboardItemEntity(courseId = 2, name = "Course 2", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 1)
        val item3 = EditDashboardItemEntity(courseId = 3, name = "Course 3", isFavorite = true, enrollmentState = EnrollmentState.PAST, position = 3)

        editDashboardItemDao.insertAll(listOf(item1, item2, item3))

        val result = editDashboardItemDao.findByEnrollmentState(EnrollmentState.CURRENT)
        assertEquals(listOf(item2, item1), result)
    }

    @Test
    fun testUpdateItemsDropsAllPreviousItems() = runTest {
        val item1 = EditDashboardItemEntity(courseId = 1, name = "Course 1", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 2)
        val item2 = EditDashboardItemEntity(courseId = 2, name = "Course 2", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 1)
        val item3 = EditDashboardItemEntity(courseId = 3, name = "Course 3", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 3)

        editDashboardItemDao.insertAll(listOf(item1, item2))

        val result = editDashboardItemDao.findByEnrollmentState(EnrollmentState.CURRENT)
        assertEquals(listOf(item2, item1), result)

        editDashboardItemDao.updateEntities(listOf(item3, item1))

        val updatedResult = editDashboardItemDao.findByEnrollmentState(EnrollmentState.CURRENT)
        assertEquals(listOf(item1, item3), updatedResult)
    }

    @Test
    fun testInsertReplace() = runTest {
        val item = EditDashboardItemEntity(courseId = 1, name = "Course 1", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 2)
        val updated = EditDashboardItemEntity(courseId = 1, name = "Course Updated", isFavorite = true, enrollmentState = EnrollmentState.CURRENT, position = 2)

        editDashboardItemDao.insertAll(listOf(item))
        editDashboardItemDao.insertAll(listOf(updated))

        val result = editDashboardItemDao.findByEnrollmentState(EnrollmentState.CURRENT)

        assertEquals(updated, result[0])
    }
}