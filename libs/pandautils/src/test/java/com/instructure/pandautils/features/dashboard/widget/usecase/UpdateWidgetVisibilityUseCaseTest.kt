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
class UpdateWidgetVisibilityUseCaseTest {

    private val repository: WidgetMetadataRepository = mockk(relaxed = true)

    private lateinit var useCase: UpdateWidgetVisibilityUseCase

    @Before
    fun setUp() {
        useCase = UpdateWidgetVisibilityUseCase(repository)
    }

    @Test
    fun testUpdateVisibilityToTrue() = runTest {
        val params = UpdateWidgetVisibilityUseCase.Params("widget1", true)

        useCase(params)

        coVerify { repository.updateVisibility("widget1", true) }
    }

    @Test
    fun testUpdateVisibilityToFalse() = runTest {
        val params = UpdateWidgetVisibilityUseCase.Params("widget1", false)

        useCase(params)

        coVerify { repository.updateVisibility("widget1", false) }
    }
}