/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.room.appdatabase.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.todolist.filter.DateRangeSelection
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToDoFilterDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var toDoFilterDao: ToDoFilterDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        toDoFilterDao = db.toDoFilterDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndFindByUser() = runTest {
        val filter = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true,
            calendarEvents = true,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.ONE_WEEK.name,
            futureDateRange = DateRangeSelection.TWO_WEEKS.name
        )

        toDoFilterDao.insert(filter)

        val foundFilter = toDoFilterDao.findByUser("test.instructure.com", 123L)

        assertEquals(filter.copy(id = 1), foundFilter)
    }

    @Test
    fun findByUserReturnsNullWhenNoMatch() = runTest {
        val filter = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true
        )

        toDoFilterDao.insert(filter)

        val foundFilter = toDoFilterDao.findByUser("other.instructure.com", 123L)

        assertNull(foundFilter)
    }

    @Test
    fun insertReplacesExistingFilterWithSameId() = runTest {
        val filter1 = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true,
            calendarEvents = false
        )

        val filter2 = filter1.copy(
            id = 1,
            userDomain = "other.instructure.com",
            userId = 456L,
            personalTodos = false,
            calendarEvents = true
        )

        toDoFilterDao.insert(filter1)
        toDoFilterDao.insert(filter2)

        val foundFilter1 = toDoFilterDao.findByUser("test.instructure.com", 123L)
        val foundFilter2 = toDoFilterDao.findByUser("other.instructure.com", 456L)

        assertNull(foundFilter1)
        assertEquals(filter2, foundFilter2)
    }

    @Test
    fun insertOrUpdateCreatesNewFilterWhenNoExisting() = runTest {
        val filter = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true,
            showCompleted = true
        )

        toDoFilterDao.insertOrUpdate(filter)

        val foundFilter = toDoFilterDao.findByUser("test.instructure.com", 123L)

        assertEquals(filter.copy(id = 1), foundFilter)
    }

    @Test
    fun insertOrUpdateUpdatesExistingFilter() = runTest {
        val filter1 = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = false,
            calendarEvents = false,
            showCompleted = false,
            favoriteCourses = false,
            pastDateRange = DateRangeSelection.ONE_WEEK.name,
            futureDateRange = DateRangeSelection.ONE_WEEK.name
        )

        toDoFilterDao.insert(filter1)

        val filter2 = filter1.copy(
            id = 1,
            personalTodos = true,
            calendarEvents = true,
            showCompleted = true,
            pastDateRange = DateRangeSelection.THREE_WEEKS.name
        )

        toDoFilterDao.insertOrUpdate(filter2)

        val foundFilter = toDoFilterDao.findByUser("test.instructure.com", 123L)

        assertEquals(filter2, foundFilter)
    }

    @Test
    fun deleteByUserRemovesFilter() = runTest {
        val filter1 = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true
        )

        val filter2 = ToDoFilterEntity(
            userDomain = "other.instructure.com",
            userId = 456L,
            personalTodos = false
        )

        toDoFilterDao.insert(filter1)
        toDoFilterDao.insert(filter2)

        toDoFilterDao.deleteByUser("test.instructure.com", 123L)

        val foundFilter1 = toDoFilterDao.findByUser("test.instructure.com", 123L)
        val foundFilter2 = toDoFilterDao.findByUser("other.instructure.com", 456L)

        assertNull(foundFilter1)
        assertEquals(filter2.copy(id = 2), foundFilter2)
    }

    @Test
    fun deleteByUserDoesNothingWhenNoMatch() = runTest {
        val filter = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true
        )

        toDoFilterDao.insert(filter)

        toDoFilterDao.deleteByUser("other.instructure.com", 999L)

        val foundFilter = toDoFilterDao.findByUser("test.instructure.com", 123L)

        assertEquals(filter.copy(id = 1), foundFilter)
    }

    @Test
    fun multipleFiltersForDifferentUsers() = runTest {
        val filter1 = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L,
            personalTodos = true,
            pastDateRange = DateRangeSelection.ONE_WEEK.name
        )

        val filter2 = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 456L,
            personalTodos = false,
            pastDateRange = DateRangeSelection.TWO_WEEKS.name
        )

        val filter3 = ToDoFilterEntity(
            userDomain = "other.instructure.com",
            userId = 123L,
            personalTodos = true,
            pastDateRange = DateRangeSelection.THREE_WEEKS.name
        )

        toDoFilterDao.insert(filter1)
        toDoFilterDao.insert(filter2)
        toDoFilterDao.insert(filter3)

        val found1 = toDoFilterDao.findByUser("test.instructure.com", 123L)
        val found2 = toDoFilterDao.findByUser("test.instructure.com", 456L)
        val found3 = toDoFilterDao.findByUser("other.instructure.com", 123L)

        assertEquals(filter1.copy(id = 1), found1)
        assertEquals(filter2.copy(id = 2), found2)
        assertEquals(filter3.copy(id = 3), found3)
    }

    @Test
    fun testDefaultValues() = runTest {
        val filter = ToDoFilterEntity(
            userDomain = "test.instructure.com",
            userId = 123L
        )

        toDoFilterDao.insert(filter)

        val foundFilter = toDoFilterDao.findByUser("test.instructure.com", 123L)

        assertEquals(false, foundFilter?.personalTodos)
        assertEquals(false, foundFilter?.calendarEvents)
        assertEquals(false, foundFilter?.showCompleted)
        assertEquals(false, foundFilter?.favoriteCourses)
        assertEquals("ONE_WEEK", foundFilter?.pastDateRange)
        assertEquals("ONE_WEEK", foundFilter?.futureDateRange)
    }
}