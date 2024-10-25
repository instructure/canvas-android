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
package com.instructure.student.features.modules.progression.datasource

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test

class ModuleProgressionNetworkDataSourceTest {

    private val moduleApi: ModuleAPI.ModuleInterface = mockk()
    private val quizApi: QuizAPI.QuizInterface = mockk()

    private val dataSource = ModuleProgressionNetworkDataSource(moduleApi, quizApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getFirstPageModuleItems fails`() = runTest {
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.getAllModuleItems(Course(1), 1, true)
    }

    @Test
    fun `Return successful result from getAllModuleItems`() = runTest {
        val firstPageModuleItems = listOf(ModuleItem(id = 1))
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Success(firstPageModuleItems)

        val result = dataSource.getAllModuleItems(Course(1), 1, true)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(firstPageModuleItems.first(), result[0])
    }

    @Test
    fun `Return successful result from depaginated getAllModuleItems`() = runTest {
        val firstPageModuleItems = listOf(ModuleItem(id = 1))
        val nextPageModuleItems = listOf(ModuleItem(id = 2))
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Success(firstPageModuleItems, linkHeaders = LinkHeaders(nextUrl = "nextUrl"))
        coEvery { moduleApi.getNextPageModuleItemList("nextUrl", any()) } returns DataResult.Success(nextPageModuleItems)

        val result = dataSource.getAllModuleItems(Course(1), 1, true)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(firstPageModuleItems.first(), result[0])
        Assert.assertEquals(nextPageModuleItems.first(), result[1])
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getModuleItemSequence fails`() = runTest {
        coEvery { moduleApi.getModuleItemSequence(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.getModuleItemSequence(Course(1), "Page", "1", true)
    }

    @Test
    fun `Return successful result from getModuleItemSequence`() = runTest {
        val moduleItemSequence = ModuleItemSequence(items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 2))))
        coEvery { moduleApi.getModuleItemSequence(any(), any(), any(), any(), any()) } returns DataResult.Success(moduleItemSequence)

        val result = dataSource.getModuleItemSequence(Course(1), "Page", "1", true)

        Assert.assertEquals(1, result.items!!.size)
        Assert.assertEquals(ModuleItem(2), result.items!!.first().current)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when getDetailedQuiz fails`() = runTest {
        coEvery { quizApi.getDetailedQuizByUrl(any(), any()) } returns DataResult.Fail()

        dataSource.getDetailedQuiz("url", 1, true)
    }

    @Test
    fun `Return successful result from getDetailedQuiz`() = runTest {
        val quiz = Quiz(id = 1, title = "Quiz")
        coEvery { quizApi.getDetailedQuizByUrl(any(), any()) } returns DataResult.Success(quiz)

        val result = dataSource.getDetailedQuiz("url", 1, true)

        Assert.assertEquals(1, result.id)
        Assert.assertEquals("Quiz", result.title)
    }

    @Test
    fun `markAsNotDone returns data result from the api call`() = runTest {
        val dataResult = DataResult.Success(mockk<ResponseBody>())
        coEvery { moduleApi.markModuleItemAsNotDone(any(), any(), any(), any(), any()) } returns dataResult

        val result = dataSource.markAsNotDone(Course(1), ModuleItem(id = 1, moduleId = 2))

        coVerify { moduleApi.markModuleItemAsNotDone("courses", 1, 2, 1, any()) }
        Assert.assertEquals(dataResult, result)
    }

    @Test
    fun `markAsDone returns data result from the api call`() = runTest {
        val dataResult = DataResult.Success(mockk<ResponseBody>())
        coEvery { moduleApi.markModuleItemAsDone(any(), any(), any(), any(), any()) } returns dataResult

        val result = dataSource.markAsDone(Course(1), ModuleItem(id = 1, moduleId = 2))

        coVerify { moduleApi.markModuleItemAsDone("courses", 1, 2, 1, any()) }
        Assert.assertEquals(dataResult, result)
    }

    @Test
    fun `markAsRead returns data result from the api call`() = runTest {
        val dataResult = DataResult.Success(mockk<ResponseBody>())
        coEvery { moduleApi.markModuleItemRead(any(), any(), any(), any(), any()) } returns dataResult

        val result = dataSource.markAsRead(Course(1), ModuleItem(id = 1, moduleId = 2))

        coVerify { moduleApi.markModuleItemRead("courses", 1, 2, 1, any()) }
        Assert.assertEquals(dataResult, result)
    }
}