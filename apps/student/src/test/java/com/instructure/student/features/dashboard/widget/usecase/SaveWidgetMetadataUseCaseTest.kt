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

package com.instructure.student.features.dashboard.widget.usecase

import com.instructure.student.features.dashboard.widget.WidgetMetadata
import com.instructure.student.features.dashboard.widget.repository.WidgetMetadataRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SaveWidgetMetadataUseCaseTest {

    private val repository: WidgetMetadataRepository = mockk(relaxed = true)
    private lateinit var useCase: SaveWidgetMetadataUseCase

    @Before
    fun setup() {
        useCase = SaveWidgetMetadataUseCase(repository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute saves metadata to repository`() = runTest {
        val metadata = WidgetMetadata("widget1", "Widget 1", 0, true)

        useCase(metadata)

        coVerify { repository.saveMetadata(metadata) }
    }

    @Test
    fun `execute saves metadata with correct properties`() = runTest {
        val metadata = WidgetMetadata("test-widget", "Test Widget", 5, false)

        useCase(metadata)

        coVerify {
            repository.saveMetadata(
                match {
                    it.id == "test-widget" &&
                    it.title == "Test Widget" &&
                    it.position == 5 &&
                    !it.isVisible
                }
            )
        }
    }
}