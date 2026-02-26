/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.course.details.progress

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CourseProgressRepositoryTest {
    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)

    private val testModules = listOf(
        ModuleObject(
            id = 1L,
            name = "Module 1",
            position = 1,
            items = listOf(
                ModuleItem(
                    id = 101L,
                    title = "Assignment 1",
                    type = "Assignment"
                ),
                ModuleItem(
                    id = 102L,
                    title = "Quiz 1",
                    type = "Quiz"
                )
            )
        ),
        ModuleObject(
            id = 2L,
            name = "Module 2",
            position = 2,
            items = listOf(
                ModuleItem(
                    id = 201L,
                    title = "Page 1",
                    type = "Page"
                )
            )
        )
    )

    @Before
    fun setup() {
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any(), any()) } returns DataResult.Success(
            testModules,
            linkHeaders = LinkHeaders()
        )
    }

    @Test
    fun `getModuleItems returns list of modules with items`() = runTest {
        val repository = getRepository()
        val result = repository.getModuleItems(1L, false)

        assertEquals(2, result.size)
        assertEquals("Module 1", result[0].name)
        assertEquals(2, result[0].items.size)
        assertEquals("Module 2", result[1].name)
        assertEquals(1, result[1].items.size)
        coVerify {
            moduleApi.getFirstPageModulesWithItems(
                CanvasContext.Type.COURSE.apiString,
                1L,
                any(),
                listOf("estimated_durations")
            )
        }
    }

    @Test
    fun `getModuleItems with forceRefresh true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getModuleItems(1L, true)

        coVerify {
            moduleApi.getFirstPageModulesWithItems(
                any(),
                any(),
                match { it.isForceReadFromNetwork },
                any()
            )
        }
    }

    @Test
    fun `getModuleItems with forceRefresh false calls API without force network`() = runTest {
        val repository = getRepository()
        repository.getModuleItems(1L, false)

        coVerify {
            moduleApi.getFirstPageModulesWithItems(
                any(),
                any(),
                match { !it.isForceReadFromNetwork },
                any()
            )
        }
    }

    @Test
    fun `getModuleItems returns empty list when no modules`() = runTest {
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any(), any()) } returns DataResult.Success(
            emptyList(),
            linkHeaders = LinkHeaders()
        )
        val repository = getRepository()
        val result = repository.getModuleItems(1L, false)

        assertEquals(0, result.size)
    }

    private fun getRepository(): CourseProgressRepository {
        return CourseProgressRepository(moduleApi)
    }
}
