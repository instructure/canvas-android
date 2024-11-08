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

package com.instructure.teacher.unit.modules.progression

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.teacher.features.modules.progression.ModuleProgressionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ModuleProgressionRepositoryTest {

    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)

    private val repository = ModuleProgressionRepository(moduleApi)

    @Test
    fun `Get modules successfully returns data`() = runTest {
        val expected = listOf(ModuleObject(id = 1L))

        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any()) } returns DataResult.Success(expected)

        val result = repository.getModulesWithItems(CanvasContext.emptyCourseContext(1L))
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Get modules with pagination successfully returns data`() = runTest {
        val page1 = listOf(ModuleObject(id = 1L))
        val page2 = listOf(ModuleObject(id = 2L))

        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any()) } returns DataResult.Success(
            page1,
            linkHeaders = LinkHeaders(nextUrl = "page_2_url")
        )
        coEvery { moduleApi.getNextPageModuleObjectList("page_2_url", any()) } returns DataResult.Success(page2)

        val result = repository.getModulesWithItems(CanvasContext.emptyCourseContext(1L))
        Assert.assertEquals(page1 + page2, result)
    }

    @Test
    fun `Get modules with items with module items fetch`() = runTest {
        val moduleObject = ModuleObject(id = 1L, itemCount = 2)
        val moduleItems = listOf(ModuleItem(id = 1L), ModuleItem(id = 2L))

        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any()) } returns DataResult.Success(listOf(moduleObject))
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Success(moduleItems)

        val result = repository.getModulesWithItems(CanvasContext.emptyCourseContext(1L))
        Assert.assertEquals(listOf(moduleObject.copy(items = moduleItems)), result)
    }

    @Test
    fun `Get modules with items with module items fetch with pagination`() = runTest {
        val moduleObject = ModuleObject(id = 1L, itemCount = 4)
        val moduleItemsPage1 = listOf(ModuleItem(id = 1L), ModuleItem(id = 2L))
        val moduleItemsPage2 = listOf(ModuleItem(id = 3L), ModuleItem(id = 4L))
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any()) } returns DataResult.Success(listOf(moduleObject))
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Success(
            moduleItemsPage1,
            linkHeaders = LinkHeaders(nextUrl = "page_2_url")
        )
        coEvery { moduleApi.getNextPageModuleItemList(any(), any()) } returns DataResult.Success(moduleItemsPage2)

        val result = repository.getModulesWithItems(CanvasContext.emptyCourseContext(1L))
        Assert.assertEquals(listOf(moduleObject.copy(items = moduleItemsPage1 + moduleItemsPage2)), result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get modules failure throws exception`() = runTest {
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any()) } returns DataResult.Fail()

        repository.getModulesWithItems(CanvasContext.emptyCourseContext(1L))
    }

    @Test
    fun `Get module item sequence successfully returns data`() = runTest {
        val expected = ModuleItemSequence(items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 1L))))

        coEvery { moduleApi.getModuleItemSequence(any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = repository.getModuleItemSequence(CanvasContext.emptyCourseContext(1L), "", "")
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get module item sequence failure throws exception`() = runTest {
        coEvery { moduleApi.getModuleItemSequence(any(), any(), any(), any(), any()) } returns DataResult.Fail()

        repository.getModuleItemSequence(CanvasContext.emptyCourseContext(1L), "", "")
    }
}
