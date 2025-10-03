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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.teacher.features.modules.progression.ModuleItemAsset
import com.instructure.teacher.features.modules.progression.ModuleItemViewData
import com.instructure.teacher.features.modules.progression.ModuleProgressionAction
import com.instructure.teacher.features.modules.progression.ModuleProgressionRepository
import com.instructure.teacher.features.modules.progression.ModuleProgressionViewData
import com.instructure.teacher.features.modules.progression.ModuleProgressionViewModel
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ModuleProgressionViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private lateinit var viewModel: ModuleProgressionViewModel

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val resources: Resources = mockk(relaxed = true)
    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository = mockk(relaxed = true)
    private val repository: ModuleProgressionRepository = mockk(relaxed = true)

    private val mockUri = mockk<Uri>(relaxed = true)
    private val mockUriBuilder = mockk<Uri.Builder>(relaxed = true)

    @Before
    fun setup() {

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
        every { mockUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendQueryParameter(any(), any()) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns mockUri
        every { mockUri.toString() } answers { "mockUri" }
        ContextKeeper.appContext = mockk(relaxed = true)

        viewModel = ModuleProgressionViewModel(resources, repository, discussionRouteHelperRepository)
    }

    @Test
    fun `Load error`() {
        val expected = "An unexpected error occurred."

        every { resources.getString(R.string.errorOccurred) } returns expected

        coEvery { repository.getModulesWithItems(any()) } throws IllegalStateException()

        viewModel.loadData(Course(id = 1L), 1L, ModuleItemAsset.MODULE_ITEM.name, "")

        assertEquals(ViewState.Error(expected), viewModel.state.value)
        assertEquals(expected, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Load success with module item id`() {
        val expected = ModuleProgressionViewData(listOf(ModuleItemViewData.Page("pageUrl")), listOf("Name"), 0, 0)

        coEvery { repository.getModulesWithItems(any()) } returns listOf(
            ModuleObject(name = "Name", items = listOf(ModuleItem(id = 1L, type = "Page", pageUrl = "pageUrl")))
        )

        viewModel.loadData(Course(id = 1L), 1L, ModuleItemAsset.MODULE_ITEM.name, "")

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expected, viewModel.data.value)
    }

    @Test
    fun `Load success with asset type and id`() {
        val expected = ModuleProgressionViewData(listOf(ModuleItemViewData.Assignment(1L)), listOf("Name"), 0, 0)

        coEvery { repository.getModuleItemSequence(any(), any(), any()) } returns ModuleItemSequence(
            items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 1L, type = "Assignment", contentId = 1L)))
        )

        coEvery { repository.getModulesWithItems(any()) } returns listOf(
            ModuleObject(name = "Name", items = listOf(ModuleItem(id = 1L, type = "Assignment", contentId = 1L)))
        )

        viewModel.loadData(Course(id = 1L), -1, ModuleItemAsset.ASSIGNMENT.name, "1")

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expected, viewModel.data.value)
    }

    @Test
    fun `Module items mapped correctly`() {
        val expected = ModuleProgressionViewData(
            listOf(
                ModuleItemViewData.Page("pageUrl"),
                ModuleItemViewData.Assignment(1L),
                ModuleItemViewData.Discussion(2L),
                ModuleItemViewData.Quiz(3L),
                ModuleItemViewData.External("mockUri", "Title 1"),
                ModuleItemViewData.External("mockUri", "Title 2"),
                ModuleItemViewData.File("fileUri"),
            ),
            listOf("Module 1", "Module 1", "Module 2", "Module 2", "Module 3", "Module 3", "Module 3"),
            0,
            0
        )

        coEvery { discussionRouteHelperRepository.shouldShowDiscussionRedesign() } returns true

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

        viewModel.loadData(Course(id = 1L), 1L, ModuleItemAsset.MODULE_ITEM.name, "")

        verify { Uri.parse("externalUri1") }
        verify { Uri.parse("externalUri2") }
        verify(exactly = 2) { mockUriBuilder.appendQueryParameter("display", "borderless") }
        assertEquals(expected, viewModel.data.value)
    }

    @Test
    fun `Position calculated correctly`() {
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

        viewModel.loadData(Course(id = 1L), 3L, ModuleItemAsset.MODULE_ITEM.name, "")

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(2, viewModel.data.value?.initialPosition)
    }

    @Test
    fun `Position uses current position after reload when position was changed`() {
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

        viewModel.loadData(Course(id = 1L), 3L, ModuleItemAsset.MODULE_ITEM.name, "")

        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(2, viewModel.data.value?.initialPosition)

        viewModel.setCurrentPosition(3)

        viewModel.loadData(Course(id = 1L), 3L, ModuleItemAsset.MODULE_ITEM.name, "")

        assertEquals(3, viewModel.data.value?.initialPosition)
    }

    @Test
    fun `Redirect if asset is not in modules`() {
        val expected = ModuleProgressionAction.RedirectToAsset(ModuleItemAsset.ASSIGNMENT)

        coEvery { repository.getModuleItemSequence(any(), any(), any()) } returns ModuleItemSequence()

        viewModel.loadData(Course(id = 1L), -1, ModuleItemAsset.ASSIGNMENT.name, "1")

        assertEquals(expected, viewModel.events.value?.peekContent())
    }
}
