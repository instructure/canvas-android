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
package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.QuizContextDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.QuizContextEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizFacadeTest {

    private val quizDao: QuizDao = mockk(relaxed = true)
    private val quizContextDao: QuizContextDao = mockk(relaxed = true)

    private val quizFacade = QuizFacade(quizDao, quizContextDao)

    @Test
    fun `Get quizzes as api model`() = runTest {
        val expectedQuizzes = listOf(Quiz(id = 1L), Quiz(id = 2L))
        val quizContextEntities = listOf(QuizContextEntity("courses", 1L, 1L), QuizContextEntity("courses", 1L, 2L))

        coEvery { quizContextDao.findByContext(any(), any()) } returns quizContextEntities
        coEvery { quizDao.findById(1L) } returns QuizEntity(expectedQuizzes[0])
        coEvery { quizDao.findById(2L) } returns QuizEntity(expectedQuizzes[1])

        val result = quizFacade.getQuizzesByContext("courses", 1L)
        assertEquals(expectedQuizzes, result)
    }

    @Test
    fun `Dao insert functions are called`() = runTest {
        val quizzes = listOf(Quiz(id = 1L), Quiz(id = 2L))

        coEvery { quizDao.insert(any()) } just Runs
        coEvery { quizContextDao.insert(any()) } just Runs

        quizzes.forEach { quiz ->
            quizFacade.insertQuiz("courses", 1L, quiz)
        }

        coVerify(exactly = 2) { quizDao.insert(any()) }
        coVerify(exactly = 2) { quizContextDao.insert(any()) }
    }
}