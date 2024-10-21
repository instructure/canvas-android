/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import android.database.sqlite.SQLiteAbortException
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseSettingsDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var courseSettingsDao: CourseSettingsDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseSettingsDao = db.courseSettingsDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val courseSettingsEntity = CourseSettingsEntity(1L, false, false)
        val updated = courseSettingsEntity.copy(courseSummary = true)

        courseSettingsDao.insert(courseSettingsEntity)
        courseSettingsDao.insert(updated)

        val result = courseSettingsDao.findByCourseId(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testFindByCourseId() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        courseDao.insert(CourseEntity(Course(2L)))
        val courseSettingsEntity = CourseSettingsEntity(1L, false, false)
        val courseSettingsEntity2 = CourseSettingsEntity(2L, false, false)

        courseSettingsDao.insert(courseSettingsEntity)
        courseSettingsDao.insert(courseSettingsEntity2)

        val result = courseSettingsDao.findByCourseId(2L)

        assertEquals(courseSettingsEntity2, result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        val courseSettingsEntity = CourseSettingsEntity(1L, false, false)

        courseSettingsDao.insert(courseSettingsEntity)
    }

    @Test
    fun testDeleteCourseDeletesSettings() = runTest {
        val courseEntity = CourseEntity(Course(1L))
        courseDao.insert(courseEntity)

        val courseSettingsEntity = CourseSettingsEntity(1L, false, false)
        courseSettingsDao.insert(courseSettingsEntity)

        courseDao.delete(courseEntity)

        assertNull(courseSettingsDao.findByCourseId(1L))
    }
}