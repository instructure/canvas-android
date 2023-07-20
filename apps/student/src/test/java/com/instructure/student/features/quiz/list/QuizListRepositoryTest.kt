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
package com.instructure.student.features.quiz.list

import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizListRepositoryTest {

    private val networkDataSource: QuizListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: QuizListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = QuizListRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Test
    fun `Get course quizzes first page if device is online`() = runTest {
        val expected = listOf(Quiz(id = 1L))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.loadQuizzes(any(), any(), any()) } returns expected

        val result = repository.loadQuizzes("courses", 1L, false)

        assertEquals(expected, result)
    }

    @Test
    fun `Get course quizzes first page if device is offline`() = runTest {
        val expected = listOf(Quiz(id = 1L))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.loadQuizzes(any(), any(), any()) } returns expected

        val result = repository.loadQuizzes("courses", 1L, false)

        assertEquals(expected, result)
    }
}