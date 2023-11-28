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

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.features.modules.progression.ModuleProgressionRepository
import com.instructure.teacher.features.modules.progression.ModuleProgressionViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ModuleProgressionViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ModuleProgressionViewModel

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository = mockk(relaxed = true)
    private val repository: ModuleProgressionRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns Course(id = 1L)
        every { savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID) } returns 1L
    }

    private fun createViewModel() {
        viewModel = ModuleProgressionViewModel(savedStateHandle, resources, repository, discussionRouteHelperRepository)
    }

    @Test
    fun `Load error`() {
        val expected = "An unexpected error occurred."

        every { resources.getString(R.string.errorOccurred) } returns expected

        coEvery { repository.getModulesWithItems(any(), any()) } throws IllegalStateException()

        createViewModel()

        Assert.assertEquals(ViewState.Error(expected), viewModel.state.value)
        Assert.assertEquals(expected, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Load success`() {
        coEvery { repository.getModulesWithItems(any(), any()) } returns listOf(
            ModuleObject(items = listOf(ModuleItem(id = 1L, type = "Page", pageUrl = "pageUrl")))
        )

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
    }
}
