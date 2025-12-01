/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.usecase

import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveWidgetMetadataUseCaseTest {

    private val repository: WidgetMetadataRepository = mockk(relaxed = true)
    private lateinit var useCase: ObserveWidgetMetadataUseCase

    @Before
    fun setup() {
        useCase = ObserveWidgetMetadataUseCase(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns metadata from repository`() = runTest {
        val metadata = listOf(
            WidgetMetadata("widget1", 0, true),
            WidgetMetadata("widget2", 1, true)
        )
        coEvery { repository.observeAllMetadata() } returns flowOf(metadata)

        val result = useCase(Unit).first()

        assertEquals(2, result.size)
        assertEquals("widget1", result[0].id)
        assertEquals("widget2", result[1].id)
    }

    @Test
    fun `execute returns empty list when repository has no metadata`() = runTest {
        coEvery { repository.observeAllMetadata() } returns flowOf(emptyList())

        val result = useCase(Unit).first()

        assertEquals(0, result.size)
    }
}