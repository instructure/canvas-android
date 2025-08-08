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
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CustomGradeStatusEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomGradeStatusDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var customGradeStatusDao: CustomGradeStatusDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        customGradeStatusDao = db.customGradeStatusDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1)))
            courseDao.insert(CourseEntity(Course(2)))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityByCourseId() = runTest {
        val customGradeStatusEntity1 = CustomGradeStatusEntity("1", "Status 1", 1)
        val customGradeStatusEntity2 = CustomGradeStatusEntity("2", "Status 2", 1)
        val customGradeStatusEntity3 = CustomGradeStatusEntity("2", "Status 2", 2)

        customGradeStatusDao.insertAll(listOf(customGradeStatusEntity1, customGradeStatusEntity2, customGradeStatusEntity3))

        val result = customGradeStatusDao.getStatusesForCourse(1)

        Assert.assertEquals(listOf(customGradeStatusEntity1, customGradeStatusEntity2), result)
    }

    @Test
    fun testCourseCascade() = runTest {
        val customGradeStatusEntity = CustomGradeStatusEntity("1", "Status 1", 1)

        customGradeStatusDao.insertAll(listOf(customGradeStatusEntity))

        courseDao.delete(CourseEntity(Course(1)))

        val result = customGradeStatusDao.getStatusesForCourse(1)

        Assert.assertTrue(result.isEmpty())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        val customGradeStatusEntity = CustomGradeStatusEntity("1", "Status 1", 3)

        customGradeStatusDao.insertAll(listOf(customGradeStatusEntity))
    }
}

