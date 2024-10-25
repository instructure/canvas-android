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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.student.features.modules.progression.ModuleItemAsset
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ModuleProgressionLocalDataSourceTest {

    private val moduleFacade: ModuleFacade = mockk(relaxed = true)
    private val quizDao: QuizDao = mockk()

    private val dataSource = ModuleProgressionLocalDataSource(moduleFacade, quizDao)

    @Test
    fun `Get all module items from facade`() = runTest {
        val moduleItems = listOf(ModuleItem(id = 1, title = "1"), ModuleItem(id = 2, title = "2"))
        coEvery { moduleFacade.getModuleItems(any()) } returns moduleItems

        val result = dataSource.getAllModuleItems(Course(1), 1, true)

        coEvery { moduleFacade.getModuleItems(any()) } returns moduleItems
        Assert.assertEquals(moduleItems, result)
    }

    @Test
    fun `Get quiz api model when entity is found in db`() = runTest {
        val quiz = Quiz(id = 1, title = "Quiz 1")
        coEvery { quizDao.findById(1) } returns QuizEntity(quiz, 1L)

        val result = dataSource.getDetailedQuiz("url", 1, true)

        Assert.assertEquals(quiz, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when quiz is not found in db`() = runTest {
        coEvery { quizDao.findById(1) } returns null

        dataSource.getDetailedQuiz("url", 1, true)
    }

    @Test
    fun `Return module item sequence for module item when asset type is module item`() = runTest {
        val moduleItem = ModuleItem(id = 1, title = "1", type = "ModuleItem")
        coEvery { moduleFacade.getModuleItemById(1) } returns moduleItem
        coEvery { moduleFacade.getModuleObjectById(any()) } returns null

        val result = dataSource.getModuleItemSequence(Course(1), ModuleItemAsset.MODULE_ITEM.assetType, "1", true)

        coVerify { moduleFacade.getModuleItemById(1) }
        Assert.assertEquals(1, result.items!!.size)
        Assert.assertEquals(moduleItem, result.items?.first()?.current)
        Assert.assertArrayEquals(emptyArray(), result.modules)
    }

    @Test
    fun `Return module item sequence for page when asset type is page`() = runTest {
        val moduleItem = ModuleItem(id = 1, title = "1", type = "Page", pageUrl = "url")
        coEvery { moduleFacade.getModuleItemForPage("url") } returns moduleItem
        coEvery { moduleFacade.getModuleObjectById(any()) } returns null

        val result = dataSource.getModuleItemSequence(Course(1), ModuleItemAsset.PAGE.assetType, "url", true)

        coVerify { moduleFacade.getModuleItemForPage("url") }
        Assert.assertEquals(1, result.items!!.size)
        Assert.assertEquals(moduleItem, result.items?.first()?.current)
        Assert.assertArrayEquals(emptyArray(), result.modules)
    }

    @Test
    fun `Return module item sequence for other asset type`() = runTest {
        val moduleItem = ModuleItem(id = 1, title = "1", type = "Quiz")
        coEvery { moduleFacade.getModuleItemByAssetIdAndType("Quiz", 2) } returns moduleItem
        coEvery { moduleFacade.getModuleObjectById(any()) } returns null

        val result = dataSource.getModuleItemSequence(Course(1), ModuleItemAsset.QUIZ.assetType, "2", true)

        coVerify { moduleFacade.getModuleItemByAssetIdAndType("Quiz", 2) }
        Assert.assertEquals(1, result.items!!.size)
        Assert.assertEquals(moduleItem, result.items?.first()?.current)
        Assert.assertArrayEquals(emptyArray(), result.modules)
    }

    @Test
    fun `Return module item sequence for other asset type with module object`() = runTest {
        val moduleItem = ModuleItem(id = 1, title = "1", type = "Quiz", moduleId = 44)
        val moduleObject = ModuleObject(id = 44, name = "Module 1", items = listOf(moduleItem))
        coEvery { moduleFacade.getModuleItemByAssetIdAndType("Quiz", 2) } returns moduleItem
        coEvery { moduleFacade.getModuleObjectById(44) } returns moduleObject

        val result = dataSource.getModuleItemSequence(Course(1), ModuleItemAsset.QUIZ.assetType, "2", true)

        Assert.assertEquals(1, result.items!!.size)
        Assert.assertEquals(moduleItem, result.items?.first()?.current)
        Assert.assertEquals(1, result.modules!!.size)
        Assert.assertEquals(moduleObject, result.modules!!.first())
    }
}