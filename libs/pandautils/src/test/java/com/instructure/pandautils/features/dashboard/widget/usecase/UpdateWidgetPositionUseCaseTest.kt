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

import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateWidgetPositionUseCaseTest {

    private val repository: WidgetMetadataRepository = mockk(relaxed = true)

    private lateinit var useCase: UpdateWidgetPositionUseCase

    @Before
    fun setUp() {
        useCase = UpdateWidgetPositionUseCase(repository)
    }

    @Test
    fun testUpdatePosition() = runTest {
        val params = UpdateWidgetPositionUseCase.Params("widget1", 5)

        useCase(params)

        coVerify { repository.updatePosition("widget1", 5) }
    }

    @Test
    fun testUpdatePositionToZero() = runTest {
        val params = UpdateWidgetPositionUseCase.Params("widget2", 0)

        useCase(params)

        coVerify { repository.updatePosition("widget2", 0) }
    }

    @Test
    fun testUpdatePositionToLargeNumber() = runTest {
        val params = UpdateWidgetPositionUseCase.Params("widget3", 100)

        useCase(params)

        coVerify { repository.updatePosition("widget3", 100) }
    }
}