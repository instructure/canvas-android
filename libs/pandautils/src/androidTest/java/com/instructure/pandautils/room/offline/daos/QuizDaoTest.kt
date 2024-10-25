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
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var quizDao: QuizDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        quizDao = db.quizDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val quizEntities = listOf(Quiz(id = 1L, title = "Quiz 1"), Quiz(id = 2L, title = "Quiz 2")).map { QuizEntity(it, 1L) }
        val expectedQuizEntity = quizEntities[1]

        quizEntities.forEach {
            quizDao.insert(it)
        }

        val result = quizDao.findById(2L)

        assertEquals(expectedQuizEntity.title, result?.title)
    }

    @Test
    fun testFindByCourseId() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        courseDao.insert(CourseEntity(Course(2L)))
        val quizzes = listOf(Quiz(id = 1L, title = "Quiz 1"), Quiz(id = 2L, title = "Quiz 2"), Quiz(id = 3L, title = "Quiz 3"))
        val quizEntities = listOf(QuizEntity(quizzes[0], 2L), QuizEntity(quizzes[1], 1L), QuizEntity(quizzes[2], 2L))
        val expectedQuizEntities = quizEntities.filter { it.courseId == 2L }

        quizEntities.forEach { quizDao.insert(it) }

        val result = quizDao.findByCourseId(2L)
        assertEquals(expectedQuizEntities.map { it.title }, result.map { it.title })
    }

    @Test
    fun testInsertReplace() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))
        val quizzes = listOf(Quiz(id = 1L, title = "Quiz 1"), Quiz(id = 1L, title = "Quiz 2"))
        val expectedTitle = quizzes[1].title

        quizDao.insert(QuizEntity(quizzes[0], 1L))
        quizDao.insert(QuizEntity(quizzes[1], 1L))

        val result = quizDao.findById(1L)

        assertEquals(expectedTitle, result?.title)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testForeignKeyConstraint() = runTest {
        quizDao.insert(QuizEntity(Quiz(id = 1L), 1L))
    }

    @Test
    fun testDeleteAndInsertAll() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))

        val quizzes = listOf(Quiz(id = 1L, title = "Quiz 1"), Quiz(id = 1L, title = "Quiz 2")).map { QuizEntity(it, 1L) }
        quizDao.insertAll(quizzes)

        val expected = listOf(Quiz(id = 3L, title = "Quiz 3"), Quiz(id = 4L, title = "Quiz 4")).map { QuizEntity(it, 1L) }
        quizDao.deleteAndInsertAll(expected, 1L)

        val result = quizDao.findByCourseId(1L)
        assertEquals(expected, result)
    }

    @Test
    fun testDeleteAllByCourseId() = runTest {
        courseDao.insert(CourseEntity(Course(1L)))

        val quizEntity = QuizEntity(Quiz(id = 1L, title = "Quiz 1"), 1L)
        quizDao.insert(quizEntity)

        val result = quizDao.findByCourseId(1L)

        assertEquals(listOf(quizEntity), result)

        quizDao.deleteAllByCourseId(1L)

        val deletedResult = quizDao.findByCourseId(1L)

        Assert.assertTrue(deletedResult.isEmpty())
    }
}