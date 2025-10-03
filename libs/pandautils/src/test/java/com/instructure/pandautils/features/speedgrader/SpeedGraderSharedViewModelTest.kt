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

package com.instructure.pandautils.features.speedgrader

import com.instructure.testutils.ViewModelTestRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeedGraderSharedViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private lateinit var viewModel: SpeedGraderSharedViewModel

    private val errorHolder: SpeedGraderErrorHolder = mockk()

    @Before
    fun setUp() {
        every { errorHolder.events } returns MutableSharedFlow()
        viewModel = SpeedGraderSharedViewModel(errorHolder)
    }

    @Test
    fun `Enable view pager emits correctly`() = runTest {
        val job = launch {
            viewModel.enableViewPager(true)
        }

        val result = viewModel.viewPagerEnabled.first()
        assertEquals(true, result)

        job.cancel()
    }
}
