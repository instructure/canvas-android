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
package com.instructure.horizon.features.learn.course.details.lti

import com.instructure.canvasapi2.apis.ExternalToolAPI
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CourseToolsRepositoryTest {
    private val externalToolApi: ExternalToolAPI.ExternalToolInterface = mockk(relaxed = true)

    private val testTools = listOf(
        LTITool(
            id = 1L,
            name = "Tool 1",
            url = "https://tool1.example.com"
        ),
        LTITool(
            id = 2L,
            name = "Tool 2",
            url = "https://tool2.example.com"
        )
    )

    @Before
    fun setup() {
        coEvery { externalToolApi.getExternalToolsForCourses(any(), any()) } returns DataResult.Success(testTools)
    }

    @Test
    fun `getExternalTools returns list of LTI tools`() = runTest {
        val repository = getRepository()
        val result = repository.getExternalTools(1L, false)

        assertEquals(2, result.size)
        assertEquals("Tool 1", result[0].name)
        assertEquals("https://tool1.example.com", result[0].url)
        coVerify { externalToolApi.getExternalToolsForCourses(listOf("course_1"), any()) }
    }

    @Test
    fun `getExternalTools with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getExternalTools(1L, true)

        coVerify {
            externalToolApi.getExternalToolsForCourses(
                any(),
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getExternalTools with forceNetwork false calls API without force network`() = runTest {
        val repository = getRepository()
        repository.getExternalTools(1L, false)

        coVerify {
            externalToolApi.getExternalToolsForCourses(
                any(),
                match { !it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getExternalTools returns empty list when no tools`() = runTest {
        coEvery { externalToolApi.getExternalToolsForCourses(any(), any()) } returns DataResult.Success(emptyList())
        val repository = getRepository()
        val result = repository.getExternalTools(1L, false)

        assertEquals(0, result.size)
    }

    private fun getRepository(): CourseToolsRepository {
        return CourseToolsRepository(externalToolApi)
    }
}
