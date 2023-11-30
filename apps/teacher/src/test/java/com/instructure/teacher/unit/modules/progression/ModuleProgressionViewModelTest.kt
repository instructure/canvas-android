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
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.features.modules.progression.ModuleItemAsset
import com.instructure.teacher.features.modules.progression.ModuleItemViewData
import com.instructure.teacher.features.modules.progression.ModuleProgressionAction
import com.instructure.teacher.features.modules.progression.ModuleProgressionFragment
import com.instructure.teacher.features.modules.progression.ModuleProgressionRepository
import com.instructure.teacher.features.modules.progression.ModuleProgressionViewData
import com.instructure.teacher.features.modules.progression.ModuleProgressionViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
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

    private val mockUri = mockk<Uri>(relaxed = true)
    private val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns mockUri
        every { mockUri.toString() } answers { "mockUri" }

        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns Course(id = 1L)
        every { savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID) } returns 1L
        every { savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_TYPE) } returns ModuleItemAsset.MODULE_ITEM.name
        every { savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_ID) } returns "1"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel = ModuleProgressionViewModel(savedStateHandle, resources, repository, discussionRouteHelperRepository)
    }

    @Test
    fun `Load error`() {
        val expected = "An unexpected error occurred."

        every { resources.getString(R.string.errorOccurred) } returns expected

        coEvery { repository.getModulesWithItems(any()) } throws IllegalStateException()

        createViewModel()

        Assert.assertEquals(ViewState.Error(expected), viewModel.state.value)
        Assert.assertEquals(expected, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Load success with module item id`() {
        val expected = ModuleProgressionViewData(listOf(ModuleItemViewData.Page("pageUrl")), listOf("Name"), 0)

        coEvery { repository.getModulesWithItems(any()) } returns listOf(
            ModuleObject(name = "Name", items = listOf(ModuleItem(id = 1L, type = "Page", pageUrl = "pageUrl")))
        )

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value)
    }

    @Test
    fun `Load success with asset type and id`() {
        val expected = ModuleProgressionViewData(listOf(ModuleItemViewData.Assignment(1L)), listOf("Name"), 0)

        every { savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID) } returns -1L
        every { savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_TYPE) } returns ModuleItemAsset.ASSIGNMENT.name
        every { savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_ID) } returns "1"

        coEvery { repository.getModuleItemSequence(any(), any(), any()) } returns ModuleItemSequence(
            items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 1L, type = "Assignment", contentId = 1L)))
        )

        coEvery { repository.getModulesWithItems(any()) } returns listOf(
            ModuleObject(name = "Name", items = listOf(ModuleItem(id = 1L, type = "Assignment", contentId = 1L)))
        )

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value)
    }

    @Test
    fun `Module items mapped correctly`() {
        val expected = ModuleProgressionViewData(
            listOf(
                ModuleItemViewData.Page("pageUrl"),
                ModuleItemViewData.Assignment(1L),
                ModuleItemViewData.Discussion(true, 2L),
                ModuleItemViewData.Quiz(3L),
                ModuleItemViewData.External("mockUri", "Title 1"),
                ModuleItemViewData.External("mockUri", "Title 2"),
                ModuleItemViewData.File("fileUri"),
            ),
            listOf("Module 1", "Module 1", "Module 2", "Module 2", "Module 3", "Module 3", "Module 3"),
            0
        )

        coEvery { discussionRouteHelperRepository.getEnabledFeaturesForCourse(any(), any()) } returns true

        coEvery { repository.getModulesWithItems(any()) } returns listOf(
            ModuleObject(
                id = 1L, name = "Module 1", items = listOf(
                    ModuleItem(id = 1L, type = "Page", pageUrl = "pageUrl", moduleId = 1L),
                    ModuleItem(id = 2L, type = "Assignment", contentId = 1L, moduleId = 1L)
                )
            ),
            ModuleObject(
                id = 2L, name = "Module 2", items = listOf(
                    ModuleItem(id = 3L, type = "Discussion", contentId = 2L, moduleId = 2L),
                    ModuleItem(id = 4L, type = "Quiz", contentId = 3L, moduleId = 2L)
                )
            ),
            ModuleObject(
                id = 3L, name = "Module 3", items = listOf(
                    ModuleItem(id = 5L, type = "ExternalUrl", htmlUrl = "externalUri1", title = "Title 1", moduleId = 3L),
                    ModuleItem(id = 6L, type = "ExternalTool", htmlUrl = "externalUri2", title = "Title 2", moduleId = 3L),
                    ModuleItem(id = 7L, type = "File", url = "fileUri", moduleId = 3L)
                )
            )
        )

        createViewModel()

        verify { Uri.parse("externalUri1") }
        verify { Uri.parse("externalUri2") }
        verify(exactly = 2) { mockUriBuilder.appendQueryParameter("display", "borderless") }
        Assert.assertEquals(expected, viewModel.data.value)
    }

    @Test
    fun `Initial position calculated correctly`() {
        every { savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID) } returns 3L

        coEvery { repository.getModulesWithItems(any()) } returns listOf(
            ModuleObject(
                id = 1L, name = "Module 1", items = listOf(
                    ModuleItem(id = 1L, type = "Page", pageUrl = "pageUrl", moduleId = 1L),
                    ModuleItem(id = 2L, type = "Assignment", contentId = 1L, moduleId = 1L)
                )
            ),
            ModuleObject(
                id = 2L, name = "Module 2", items = listOf(
                    ModuleItem(id = 3L, type = "Discussion", contentId = 2L, moduleId = 2L),
                    ModuleItem(id = 4L, type = "Quiz", contentId = 3L, moduleId = 2L)
                )
            )
        )

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(2, viewModel.data.value?.initialPosition)
    }

    @Test
    fun `Redirect if asset is not in modules`() {
        val expected = ModuleProgressionAction.RedirectToAsset(ModuleItemAsset.ASSIGNMENT)

        every { savedStateHandle.get<Long>(RouterParams.MODULE_ITEM_ID) } returns -1L
        every { savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_TYPE) } returns ModuleItemAsset.ASSIGNMENT.name
        every { savedStateHandle.get<String>(ModuleProgressionFragment.ASSET_ID) } returns "1"

        coEvery { repository.getModuleItemSequence(any(), any(), any()) } returns ModuleItemSequence()

        createViewModel()

        Assert.assertEquals(expected, viewModel.events.value?.peekContent())
    }
}
