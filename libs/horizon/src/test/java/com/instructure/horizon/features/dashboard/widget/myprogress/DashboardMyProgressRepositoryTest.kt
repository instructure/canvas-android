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
package com.instructure.horizon.features.dashboard.widget.myprogress

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.journey.GetWidgetDataQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class DashboardMyProgressRepositoryTest {

    private val getWidgetsManager: GetWidgetsManager = mockk(relaxed = true)

    private lateinit var repository: DashboardMyProgressRepository

    @Before
    fun setup() {
        repository = DashboardMyProgressRepository(getWidgetsManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getLearningStatusData returns data successfully`() = runTest {
        val widgetData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf("module_count_completed" to 5)
            )
        )

        coEvery { getWidgetsManager.getLearningStatusWidgetData(null, false) } returns widgetData

        val result = repository.getLearningStatusData(null, false)

        assertEquals(1, result?.data?.size)
        assertEquals(5, result?.data?.get(0)?.moduleCountCompleted)
        coVerify { getWidgetsManager.getLearningStatusWidgetData(null, false) }
    }

    @Test
    fun `getLearningStatusData with courseId passes courseId to manager`() = runTest {
        val courseId = 123L
        val widgetData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf("module_count_completed" to 10)
            )
        )

        coEvery { getWidgetsManager.getLearningStatusWidgetData(courseId, false) } returns widgetData

        val result = repository.getLearningStatusData(courseId, false)

        assertEquals(1, result?.data?.size)
        assertEquals(10, result?.data?.get(0)?.moduleCountCompleted)
        coVerify { getWidgetsManager.getLearningStatusWidgetData(courseId, false) }
    }

    @Test
    fun `getLearningStatusData with forceNetwork true uses network`() = runTest {
        val widgetData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf("module_count_completed" to 3)
            )
        )

        coEvery { getWidgetsManager.getLearningStatusWidgetData(null, true) } returns widgetData

        val result = repository.getLearningStatusData(null, true)

        assertEquals(1, result?.data?.size)
        assertEquals(3, result?.data?.get(0)?.moduleCountCompleted)
        coVerify { getWidgetsManager.getLearningStatusWidgetData(null, true) }
    }

    @Test(expected = Exception::class)
    fun `getLearningStatusData propagates exceptions`() = runTest {
        coEvery { getWidgetsManager.getLearningStatusWidgetData(null, false) } throws Exception("Network error")

        repository.getLearningStatusData(null, false)
    }

    @Test
    fun `getLearningStatusData handles empty data list`() = runTest {
        val widgetData = GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { getWidgetsManager.getLearningStatusWidgetData(null, false) } returns widgetData

        val result = repository.getLearningStatusData(null, false)

        assertEquals(emptyList<Any>(), result?.data)
    }
}
