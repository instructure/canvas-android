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
package com.instructure.student.features.quiz.list.datasource

import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.quiz.list.QuizListNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizListNetworkDataSourceTest {
    private val quizApi: QuizAPI.QuizInterface = mockk(relaxed = true)

    private val dataSource = QuizListNetworkDataSource(quizApi)

    @Test
    fun `Quizzes are returned`() = runTest {
        val expected = listOf(Quiz(1L), Quiz(2L))

        coEvery { quizApi.getFirstPageQuizzesList(any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadQuizzes("contextType", 1L, false)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Quizzes Api fail`() = runTest {
        coEvery { quizApi.getFirstPageQuizzesList(any(), any(), any()) } returns DataResult.Fail()

        dataSource.loadQuizzes("contextType", 1L, false)
    }
}