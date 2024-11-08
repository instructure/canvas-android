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
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseFeaturesDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var courseFeaturesDao: CourseFeaturesDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseFeaturesDao = db.courseFeaturesDao()
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
        val courseFeaturesEntity = CourseFeaturesEntity(1, listOf("feature1", "feature2"))
        val courseFeaturesEntity2 = CourseFeaturesEntity(2, listOf("feature3", "feature4"))
        courseFeaturesDao.insert(courseFeaturesEntity)
        courseFeaturesDao.insert(courseFeaturesEntity2)

        val result = courseFeaturesDao.findByCourseId(1)

        Assert.assertEquals(courseFeaturesEntity, result)
    }

    @Test
    fun testCourseCascade() = runTest {
        val courseFeaturesEntity = CourseFeaturesEntity(1, listOf("feature1", "feature2"))

        courseFeaturesDao.insert(courseFeaturesEntity)

        courseDao.delete(CourseEntity(Course(1)))

        val result = courseFeaturesDao.findByCourseId(1)

        Assert.assertNull(result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        val courseFeaturesEntity = CourseFeaturesEntity(3, listOf("feature1", "feature2"))

        courseFeaturesDao.insert(courseFeaturesEntity)
    }
}
