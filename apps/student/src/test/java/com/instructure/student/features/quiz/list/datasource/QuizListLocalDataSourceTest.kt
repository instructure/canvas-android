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

import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.student.features.quiz.list.QuizListLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QuizListLocalDataSourceTest {
    private val quizDao: QuizDao = mockk(relaxed = true)
    private val courseSettingsDao: CourseSettingsDao = mockk(relaxed = true)

    private val dataSource = QuizListLocalDataSource(quizDao, courseSettingsDao)

    @Test
    fun `Quizzes returned`() = runTest {
        val expected = listOf(Quiz(1L), Quiz(2L))

        coEvery { quizDao.findByCourseId(any()) } returns expected.map { QuizEntity(it, 1L) }

        val result = dataSource.loadQuizzes("course", 1L, false)
        assertEquals(expected, result)
    }

    @Test
    fun `Load course settings successfully returns api model`() = runTest {
        val expected = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseSettingsDao.findByCourseId(any()) } returns CourseSettingsEntity(expected, 1L)

        val result = dataSource.loadCourseSettings(1, true)

        assertEquals(expected, result)
    }
}