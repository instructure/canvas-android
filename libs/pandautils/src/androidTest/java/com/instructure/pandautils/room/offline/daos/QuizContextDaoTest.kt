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
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.QuizContextEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class QuizContextDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var quizContextDao: QuizContextDao
    private lateinit var quizDao: QuizDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        quizContextDao = db.quizContextDao()
        quizDao = db.quizDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testForeignKeyConstraint() = runTest {
        val quiz = Quiz(1L)

        val quizContextEntity = QuizContextEntity("course", 1L, quiz)
        quizContextDao.insert(quizContextEntity)
    }

    @Test
    fun testFindByContext() = runTest {
        val quizzes = listOf(
            Quiz(1L),
            Quiz(2L),
            Quiz(3L),
            Quiz(4L),
        )
        quizzes.forEach { quizDao.insert(QuizEntity(it)) }
        val quizContextEntities = listOf(
            QuizContextEntity("course", 1L, quizzes[0]),
            QuizContextEntity("course", 2L, quizzes[1]),
            QuizContextEntity("group", 1L, quizzes[2]),
            QuizContextEntity("course", 1L, quizzes[3]),
        )
        val expectedQuizzes = quizContextEntities.filter { it.contextType == "courses" && it.contextId == 1L }.map { it.quizId }
        quizContextEntities.forEach { quizContextDao.insert(it) }

        val result = quizContextDao.findByContext("courses", 1L).map { it.quizId }

        assertEquals(expectedQuizzes, result)
    }
}