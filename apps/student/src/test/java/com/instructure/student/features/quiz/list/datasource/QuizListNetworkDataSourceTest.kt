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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.quiz.list.QuizListNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class QuizListNetworkDataSourceTest {
    private val quizApi: QuizAPI.QuizInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val dataSource = QuizListNetworkDataSource(quizApi, courseApi)

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

    @Test
    fun `Load course settings returns succesful api model`() = runTest {
        val expected = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadCourseSettings(1, true)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Load course settings failure returns null`() = runTest {
        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadCourseSettings(1, true)

        Assert.assertNull(result)
    }
}