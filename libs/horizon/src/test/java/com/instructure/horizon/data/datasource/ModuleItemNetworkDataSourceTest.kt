/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ModuleItemNetworkDataSourceTest {

    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)
    private val dataSource = ModuleItemNetworkDataSource(moduleApi)

    private val courseId = 100L

    private fun item(
        id: Long,
        title: String = "Item $id",
        moduleId: Long = 1L,
        completed: Boolean? = null,
    ): ModuleItem = ModuleItem(
        id = id,
        moduleId = moduleId,
        title = title,
        type = "Assignment",
        completionRequirement = completed?.let {
            ModuleCompletionRequirement(type = ModuleItem.MUST_SUBMIT, completed = it)
        },
    )

    private fun module(id: Long, items: List<ModuleItem>, itemCount: Int = items.size): ModuleObject =
        ModuleObject(id = id, name = "Module $id", items = items, itemCount = itemCount)

    @Test
    fun `returns first required, incomplete item from first page`() = runTest {
        val items = listOf(
            item(1, completed = true),
            item(2, completed = false),
            item(3, completed = false),
        )
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(listOf(module(1, items)))

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertEquals(2L, result?.moduleItemId)
    }

    @Test
    fun `skips items without completionRequirement (optional items)`() = runTest {
        val items = listOf(
            item(1, completed = null),
            item(2, completed = null),
            item(3, completed = false),
        )
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(listOf(module(1, items)))

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertEquals(3L, result?.moduleItemId)
    }

    @Test
    fun `walks to second module when first module is fully completed`() = runTest {
        val firstModule = module(1, listOf(item(1, completed = true), item(2, completed = true)))
        val secondModule = module(2, listOf(item(3, moduleId = 2L, completed = false)))
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(listOf(firstModule, secondModule))

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertEquals(3L, result?.moduleItemId)
    }

    @Test
    fun `paginates module pages until required incomplete item is found`() = runTest {
        val firstPage = listOf(module(1, listOf(item(1, completed = true))))
        val secondPage = listOf(module(2, listOf(item(2, moduleId = 2L, completed = false))))
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(
            firstPage,
            linkHeaders = LinkHeaders(nextUrl = "next-page-url"),
        )
        coEvery {
            moduleApi.getNextPageModuleObjectList("next-page-url", any())
        } returns DataResult.Success(secondPage)

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertEquals(2L, result?.moduleItemId)
        coVerify(exactly = 1) { moduleApi.getNextPageModuleObjectList("next-page-url", any()) }
    }

    @Test
    fun `paginates module items when module reports more items than returned`() = runTest {
        val partialItems = listOf(item(1, completed = true))
        val moduleWithMore = module(1, partialItems, itemCount = 2)
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(listOf(moduleWithMore))
        coEvery {
            moduleApi.getFirstPageModuleItems(any(), eq(courseId), eq(1L), any(), any())
        } returns DataResult.Success(
            listOf(item(1, completed = true), item(2, completed = false)),
        )

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertEquals(2L, result?.moduleItemId)
        coVerify(exactly = 1) { moduleApi.getFirstPageModuleItems(any(), eq(courseId), eq(1L), any(), any()) }
    }

    @Test
    fun `returns null when every required item is completed`() = runTest {
        val items = listOf(item(1, completed = true), item(2, completed = true))
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(listOf(module(1, items)))

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertNull(result)
    }

    @Test
    fun `returns null when no module item has a completionRequirement`() = runTest {
        val items = listOf(item(1, completed = null), item(2, completed = null))
        coEvery {
            moduleApi.getFirstPageModulesWithItems(any(), eq(courseId), any(), any())
        } returns DataResult.Success(listOf(module(1, items)))

        val result = dataSource.getNextModuleItemForCourse(courseId, forceRefresh = false)

        assertNull(result)
    }
}
