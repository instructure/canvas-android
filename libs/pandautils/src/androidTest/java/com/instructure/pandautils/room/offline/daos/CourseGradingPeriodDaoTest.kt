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
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CourseGradingPeriodEntity
import com.instructure.pandautils.room.offline.entities.GradingPeriodEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseGradingPeriodDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var courseGradingPeriodDao: CourseGradingPeriodDao
    private lateinit var gradingPeriodDao: GradingPeriodDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseGradingPeriodDao = db.courseGradingPeriodDao()
        gradingPeriodDao = db.gradingPeriodDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(id = 1L)))
            courseDao.insert(CourseEntity(Course(id = 2L)))
            gradingPeriodDao.insert(GradingPeriodEntity(GradingPeriod(id = 1L)))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityByCourseId() = runTest {
        val courseGradingPeriodEntity = CourseGradingPeriodEntity(1L, 1L)
        val courseGradingPeriodEntity2 = CourseGradingPeriodEntity(2L, 1L)
        courseGradingPeriodDao.insert(courseGradingPeriodEntity)
        courseGradingPeriodDao.insert(courseGradingPeriodEntity2)

        val result = courseGradingPeriodDao.findByCourseId(1)

        Assert.assertEquals(listOf(courseGradingPeriodEntity), result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        val courseGradingPeriodEntity = CourseGradingPeriodEntity(3L, 1L)

        courseGradingPeriodDao.insert(courseGradingPeriodEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testGradingPeriodForeignKey() = runTest {
        val courseGradingPeriodEntity = CourseGradingPeriodEntity(1L, 2L)

        courseGradingPeriodDao.insert(courseGradingPeriodEntity)
    }

    @Test
    fun testCourseCascade() = runTest {
        val courseGradingPeriodEntity = CourseGradingPeriodEntity(1L, 1L)

        courseGradingPeriodDao.insert(courseGradingPeriodEntity)

        courseDao.delete(CourseEntity(Course(1L)))

        val result = courseGradingPeriodDao.findByCourseId(1L)

        assert(result.isEmpty())
    }

    @Test
    fun testGradingPeriodCascade() = runTest {
        val courseGradingPeriodEntity = CourseGradingPeriodEntity(1L, 1L)

        courseGradingPeriodDao.insert(courseGradingPeriodEntity)

        gradingPeriodDao.delete(GradingPeriodEntity(GradingPeriod(1L)))

        val result = courseGradingPeriodDao.findByCourseId(1L)

        assert(result.isEmpty())
    }
}