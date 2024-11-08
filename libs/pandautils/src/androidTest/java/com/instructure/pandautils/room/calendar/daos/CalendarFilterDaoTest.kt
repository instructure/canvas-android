/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.room.calendar.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.calendar.CalendarFilterDatabase
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarFilterDaoTest {

    private lateinit var db: CalendarFilterDatabase
    private lateinit var calendarFilterDao: CalendarFilterDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CalendarFilterDatabase::class.java).build()
        calendarFilterDao = db.calendarFilterDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndFindingByUserIdAndDomain() = runTest {
        val calendarFilterEntity = CalendarFilterEntity(
            userId = "1", userDomain = "domain.com", filters = setOf("course_1", "course_2")
        )

        val calendarFilterEntity2 = calendarFilterEntity.copy(userId = "2", userDomain = "domain2.com", filters = setOf("course_3"))

        calendarFilterDao.insert(calendarFilterEntity)
        calendarFilterDao.insert(calendarFilterEntity2)

        val foundEntity = calendarFilterDao.findByUserIdAndDomain(1, "domain.com")

        assertEquals(calendarFilterEntity.copy(id = 1), foundEntity)
    }

    @Test
    fun insertAndFindingByUserIdAndDomainAndObserveeId() = runTest {
        val calendarFilterEntity = CalendarFilterEntity(
            userId = "1", userDomain = "domain.com", filters = setOf("course_1", "course_2"), observeeId = 55
        )

        val calendarFilterEntity2 = calendarFilterEntity.copy(userId = "1", userDomain = "domain.com", filters = setOf("course_3"), observeeId = 44)

        calendarFilterDao.insert(calendarFilterEntity)
        calendarFilterDao.insert(calendarFilterEntity2)

        val foundEntity = calendarFilterDao.findByUserIdAndDomainAndObserveeId(1, "domain.com", 55)

        assertEquals(calendarFilterEntity.copy(id = 1), foundEntity)
    }

    @Test
    fun testInsertReplace() = runTest {
        val calendarFilterEntity = CalendarFilterEntity(
            userId = "1", userDomain = "domain.com", filters = setOf("course_1", "course_2")
        )

        val calendarFilterEntity2 = calendarFilterEntity.copy(id = 1, userId = "2", userDomain = "domain2.com", filters = setOf("course_3"))

        calendarFilterDao.insert(calendarFilterEntity)
        calendarFilterDao.insert(calendarFilterEntity2)

        val foundEntity = calendarFilterDao.findByUserIdAndDomain(1, "domain.com")

        assertNull(foundEntity)

        val foundEntity2 = calendarFilterDao.findByUserIdAndDomain(2, "domain2.com")
        assertEquals(calendarFilterEntity2.copy(id = 1), foundEntity2)
    }

    @Test
    fun testInsertOrUpdate() = runTest {
        val calendarFilterEntity = CalendarFilterEntity(
            userId = "1", userDomain = "domain.com", filters = setOf("course_1", "course_2")
        )

        val calendarFilterEntity2 = calendarFilterEntity.copy(id = 1, userId = "2", userDomain = "domain2.com", filters = setOf("course_3"))

        calendarFilterDao.insertOrUpdate(calendarFilterEntity)
        calendarFilterDao.insertOrUpdate(calendarFilterEntity2)

        val foundEntity = calendarFilterDao.findByUserIdAndDomain(1, "domain.com")

        assertNull(foundEntity)

        val foundEntity2 = calendarFilterDao.findByUserIdAndDomain(2, "domain2.com")
        assertEquals(calendarFilterEntity2.copy(id = 1), foundEntity2)
    }
}