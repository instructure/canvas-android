/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var reminderDao: ReminderDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        reminderDao = db.reminderDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindItemsByAssignmentId() = runTest {
        val entities = listOf(
            ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", 1000),
            ReminderEntity(2, 2, 1, "htmlUrl2", "Assignment 2", "2 days", 2000),
            ReminderEntity(3, 1, 2, "htmlUrl3", "Assignment 3", "3 days", 3000)
        )
        entities.forEach { reminderDao.insert(it) }

        val result = reminderDao.findByAssignmentIdLiveData(1, 1)
        result.observeForever { }

        Assert.assertEquals(entities.take(1), result.value)
    }

    @Test
    fun testDeleteById() = runTest {
        val entities = listOf(
            ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", 1000),
            ReminderEntity(2, 1, 1, "htmlUrl2", "Assignment 2", "2 days", 2000),
        )
        entities.forEach { reminderDao.insert(it) }

        reminderDao.deleteById(1)

        val result = reminderDao.findByAssignmentIdLiveData(1, 1)
        result.observeForever { }

        Assert.assertEquals(entities.takeLast(1), result.value)
    }

    @Test
    fun testDeletePastReminders() = runTest {
        val entities = listOf(
            ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", 1000),
            ReminderEntity(2, 1, 1, "htmlUrl2", "Assignment 2", "2 days", 2000),
            ReminderEntity(2, 1, 1, "htmlUrl2", "Assignment 2", "2 days", 3000)
        )
        entities.forEach { reminderDao.insert(it) }

        reminderDao.deletePastReminders(2000)

        val result = reminderDao.findByAssignmentIdLiveData(1, 1)
        result.observeForever { }

        Assert.assertEquals(entities.takeLast(1), result.value)
    }

    @Test
    fun testFindItemsByUserId() = runTest {
        val entities = listOf(
            ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", 1000),
            ReminderEntity(2, 1, 3, "htmlUrl2", "Assignment 2", "2 days", 2000),
            ReminderEntity(3, 2, 3, "htmlUrl3", "Assignment 3", "3 days", 3000)
        )
        entities.forEach { reminderDao.insert(it) }

        val result = reminderDao.findByUserId(1)

        Assert.assertEquals(entities.take(2), result)
    }
}