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
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindAllEntities() = runTest {
        val courseEntity = CourseEntity(Course(id = 1, "Course 1", "Original Course", "CRS", currentGrade = "0"))
        val courseEntity2 = CourseEntity(Course(id = 2, "Course 2", "Original Course 2", "CRS", currentGrade = "2"))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)

        val result = courseDao.findAll()

        Assert.assertEquals(listOf(courseEntity, courseEntity2), result)
    }

    @Test
    fun testFindEntityById() = runTest {
        val courseEntity = CourseEntity(Course(id = 1, "Course 1", "Original Course", "CRS", currentGrade = "0"))
        val courseEntity2 = CourseEntity(Course(id = 2, "Course 2", "Original Course 2", "CRS", currentGrade = "2"))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)

        val result = courseDao.findById(1)

        Assert.assertEquals(courseEntity, result)
    }

    @Test
    fun testFindEntityByIdReturnsNullIfNotFound() = runTest {
        val courseEntity = CourseEntity(Course(id = 1, "Course 1", "Original Course", "CRS", currentGrade = "0"))
        val courseEntity2 = CourseEntity(Course(id = 2, "Course 2", "Original Course 2", "CRS", currentGrade = "2"))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)

        val result = courseDao.findById(3)

        Assert.assertNull(result)
    }

    @Test
    fun testFindEntitiesByIds() = runTest {
        val courseEntity = CourseEntity(Course(id = 1, "Course 1", "Original Course", "CRS", currentGrade = "0"))
        val courseEntity2 = CourseEntity(Course(id = 2, "Course 2", "Original Course 2", "CRS", currentGrade = "2"))
        val courseEntity3 = CourseEntity(Course(id = 3, "Course 3", "Original Course 3", "CRS", currentGrade = "2"))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)
        courseDao.insert(courseEntity3)

        val result = courseDao.findByIds(setOf(1, 2))

        Assert.assertEquals(listOf(courseEntity, courseEntity2), result)
    }

    @Test
    fun testFindEntitiesByIdReturnsEmptyListNotFound() = runTest {
        val courseEntity = CourseEntity(Course(id = 1, "Course 1", "Original Course", "CRS", currentGrade = "0"))
        val courseEntity2 = CourseEntity(Course(id = 2, "Course 2", "Original Course 2", "CRS", currentGrade = "2"))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)

        val result = courseDao.findByIds(setOf(16, 55))

        Assert.assertEquals(emptyList<CourseEntity>(),  result)
    }

    @Test
    fun testDeleteByIds() = runTest {
        val courseEntity = CourseEntity(Course(id = 1, "Course 1", "Original Course", "CRS", currentGrade = "0"))
        val courseEntity2 = CourseEntity(Course(id = 2, "Course 2", "Original Course 2", "CRS", currentGrade = "2"))
        courseDao.insertOrUpdate(courseEntity)
        courseDao.insertOrUpdate(courseEntity2)

        val result = courseDao.findAll()

        Assert.assertEquals(listOf(courseEntity, courseEntity2), result)

        courseDao.deleteByIds(listOf(1, 2))

        val deletedResult = courseDao.findAll()

        Assert.assertEquals(emptyList<CourseEntity>(), deletedResult)
    }
}