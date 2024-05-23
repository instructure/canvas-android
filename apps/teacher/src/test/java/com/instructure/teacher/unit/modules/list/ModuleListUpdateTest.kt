/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.teacher.unit.modules.list

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.teacher.features.modules.list.BulkModuleUpdateAction
import com.instructure.teacher.features.modules.list.ModuleListEffect
import com.instructure.teacher.features.modules.list.ModuleListEvent
import com.instructure.teacher.features.modules.list.ModuleListModel
import com.instructure.teacher.features.modules.list.ModuleListPageData
import com.instructure.teacher.features.modules.list.ModuleListUpdate
import com.instructure.teacher.unit.utils.matchesEffects
import com.instructure.teacher.unit.utils.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNothing
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.Assert
import org.junit.Test
import com.instructure.teacher.R

class ModuleListUpdateTest : Assert() {

    private val initSpec = InitSpec(ModuleListUpdate()::init)
    private val updateSpec = UpdateSpec(ModuleListUpdate()::update)

    private val course = Course()
    private val initModel = ModuleListModel(course)

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst<ModuleListModel, ModuleListEffect>(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `Initializes into a loading state with scrollToItemId`() {
        val itemId = 123L
        val expectedModel = initModel.copy(isLoading = true, scrollToItemId = itemId)
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        initSpec
            .whenInit(initModel.copy(scrollToItemId = itemId))
            .then(
                assertThatFirst<ModuleListModel, ModuleListEffect>(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `PullToRefresh clears data and forces network reload of first page`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(ModuleObject())
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true)
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.PullToRefresh)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `ModuleClicked event emits ShowModuleDetailView effect`() {
        val item = ModuleItem(id = 123L)
        val event = ModuleListEvent.ModuleItemClicked(item.id)
        val expectedEffect = ModuleListEffect.ShowModuleItemDetailView(item, course)
        val model = initModel.copy(
            modules = listOf(ModuleObject(items = listOf(item)))
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `ModuleExpanded event emits MarkModuleExpanded effect`() {
        val moduleId = 123L
        val isExpanded = true
        val event = ModuleListEvent.ModuleExpanded(moduleId, isExpanded)
        val expectedEffect = ModuleListEffect.MarkModuleExpanded(course, moduleId, isExpanded)
        updateSpec
            .given(initModel)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `ModuleItemLoadStatusChanged event adds loadingModuleItemIds`() {
        val model = initModel.copy(
            loadingModuleItemIds = setOf(1L, 2L, 3L)
        )
        val expectedModel = initModel.copy(
            loadingModuleItemIds = setOf(1L, 2L, 3L, 4L, 5L, 6L)
        )
        val event = ModuleListEvent.ModuleItemLoadStatusChanged(setOf(4L, 5L, 6L), true)
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `ModuleItemLoadStatusChanged event removed loadingModuleItemIds`() {
        val model = initModel.copy(
            loadingModuleItemIds = setOf(1L, 2L, 3L, 4L)
        )
        val expectedModel = initModel.copy(
            loadingModuleItemIds = setOf(2L, 4L)
        )
        val event = ModuleListEvent.ModuleItemLoadStatusChanged(setOf(1L, 3L), false)
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `ItemRefreshRequested event emits UpdateModuleItems effect`() {
        val items = listOf(
            ModuleItem(id = 1L, type = "Assignment"),
            ModuleItem(id = 2L, type = "Discussion"),
            ModuleItem(id = 3L, type = "File")
        )
        val model = initModel.copy(
            modules = listOf(ModuleObject(items = items))
        )
        val event = ModuleListEvent.ItemRefreshRequested("Discussion") { it.id in 1L..3L }
        val expectedEffect = ModuleListEffect.UpdateModuleItems(model.course, listOf(items[1]))
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `ItemRefreshRequested event emits no change if model does not have matching items`() {
        val items = listOf(
            ModuleItem(id = 1L, type = "Assignment"),
            ModuleItem(id = 2L, type = "Discussion"),
            ModuleItem(id = 3L, type = "File")
        )
        val model = initModel.copy(
            modules = listOf(ModuleObject(items = items))
        )
        val event = ModuleListEvent.ItemRefreshRequested("Discussion") { it.id == 3L }
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasNothing()
                )
            )
    }

    @Test
    fun `ReplaceModuleItems event replaces module items`() {
        val model = initModel.copy(
            modules = listOf(
                ModuleObject(id = 1L, items = listOf(ModuleItem(id = 100L, moduleId = 1L, title = "M1 Old Item"))),
                ModuleObject(id = 2L, items = listOf(ModuleItem(id = 200L, moduleId = 2L, title = "M2 Old Item"))),
                ModuleObject(id = 3L, items = listOf(ModuleItem(id = 300L, moduleId = 3L, title = "M3 Old Item")))
            )
        )
        val expectedModel = initModel.copy(
            modules = listOf(
                ModuleObject(id = 1L, items = listOf(ModuleItem(id = 100L, moduleId = 1L, title = "M1 New Item"))),
                ModuleObject(id = 2L, items = listOf(ModuleItem(id = 200L, moduleId = 2L, title = "M2 Old Item"))),
                ModuleObject(id = 3L, items = listOf(ModuleItem(id = 300L, moduleId = 3L, title = "M3 New Item")))
            )
        )
        val event = ModuleListEvent.ReplaceModuleItems(
            listOf(
                ModuleItem(id = 100L, moduleId = 1L, title = "M1 New Item"),
                ModuleItem(id = 300L, moduleId = 3L, title = "M3 New Item")
            )
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `RemoveModuleItems event removed module items`() {
        mockkObject(CanvasRestAdapter.Companion)
        every { CanvasRestAdapter.clearCacheUrls(any()) } returns mockk()

        val model = initModel.copy(
            modules = listOf(
                ModuleObject(
                    id = 1L,
                    items = listOf(ModuleItem(id = 100L, moduleId = 1L, type = "File", contentId = 123L))
                ),
                ModuleObject(id = 2L, items = listOf(ModuleItem(id = 200L, moduleId = 2L, title = "M2 Old Item"))),
                ModuleObject(
                    id = 3L,
                    items = listOf(ModuleItem(id = 300L, moduleId = 3L, type = "File", contentId = 123L))
                )
            )
        )
        val expectedModel = initModel.copy(
            modules = listOf(
                ModuleObject(id = 1L),
                ModuleObject(id = 2L, items = listOf(ModuleItem(id = 200L, moduleId = 2L, title = "M2 Old Item"))),
                ModuleObject(id = 3L)
            )
        )
        val event = ModuleListEvent.RemoveModuleItems("File") { it.contentId == 123L }
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel)
                )
            )

        unmockkObject(CanvasRestAdapter.Companion)
    }

    @Test
    fun `PageLoaded event with failed result correctly updates model`() {
        val event = ModuleListEvent.PageLoaded(
            ModuleListPageData(lastPageResult = DataResult.Fail())
        )
        val model = initModel.copy(isLoading = true)
        val expectedModel = initModel.copy(
            isLoading = false,
            pageData = event.pageData
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `PageLoaded event correctly updates model`() {
        val existingData = List(5) { ModuleObject(id = it.toLong()) }
        val newData = List(5) { ModuleObject(id = it.toLong() + 10) }
        val expectedData = existingData + newData
        val event = ModuleListEvent.PageLoaded(
            ModuleListPageData(lastPageResult = DataResult.Success(newData))
        )
        val model = initModel.copy(
            isLoading = true,
            modules = existingData
        )
        val expectedModel = initModel.copy(
            isLoading = false,
            pageData = event.pageData,
            modules = expectedData
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `PageLoaded event correctly updates model and emits effect when scroll-to item is loaded`() {
        val itemId = 123L
        val existingData = listOf(ModuleObject(id = 1))
        val newData = listOf(ModuleObject(id = 2, items = listOf(ModuleItem(itemId))))
        val expectedData = existingData + newData
        val event = ModuleListEvent.PageLoaded(
            ModuleListPageData(lastPageResult = DataResult.Success(newData))
        )
        val model = initModel.copy(
            isLoading = true,
            modules = existingData,
            scrollToItemId = itemId
        )
        val expectedModel = initModel.copy(
            isLoading = false,
            pageData = event.pageData,
            modules = expectedData,
            scrollToItemId = null
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel),
                    matchesEffects(ModuleListEffect.ScrollToItem(itemId))
                )
            )
    }

    @Test
    fun `NextPageRequested event results in no change if already loading`() {
        val event = ModuleListEvent.NextPageRequested
        val model = initModel.copy(isLoading = true)
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    NextMatchers.hasNothing()
                )
            )
    }

    @Test
    fun `NextPageRequested event correctly updates model and emits LoadNextPage event`() {
        val event = ModuleListEvent.NextPageRequested
        val model = initModel.copy(isLoading = false)
        val expectedModel = initModel.copy(isLoading = true)
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModuleListModel, ModuleListEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateModule sets loading for module`() {
        val module = ModuleObject(id = 1L, items = listOf(ModuleItem(id = 100L, moduleId = 1L)))
        val event = ModuleListEvent.BulkUpdateModule(1L, BulkModuleUpdateAction.PUBLISH, true)
        val model = initModel.copy(modules = listOf(module))
        val expectedModel = model.copy(
            loadingModuleItemIds = setOf(1L)
        )
        val expectedEffect = ModuleListEffect.BulkUpdateModules(
            expectedModel.course,
            listOf(1L),
            listOf(1L),
            BulkModuleUpdateAction.PUBLISH,
            true,
            false
        )

        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateModule sets loading for module and items`() {
        val module = ModuleObject(id = 1L, items = listOf(ModuleItem(id = 100L, moduleId = 1L)))
        val event = ModuleListEvent.BulkUpdateModule(1L, BulkModuleUpdateAction.PUBLISH, false)
        val model = initModel.copy(modules = listOf(module))
        val expectedModel = model.copy(
            loadingModuleItemIds = setOf(1L, 100L)
        )
        val expectedEffect = ModuleListEffect.BulkUpdateModules(
            expectedModel.course,
            listOf(1L),
            listOf(1L, 100L),
            BulkModuleUpdateAction.PUBLISH,
            false,
            false
        )

        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateAllModules sets loading for modules`() {
        val modules = listOf(
            ModuleObject(id = 1L, items = listOf(ModuleItem(id = 100L, moduleId = 1L))),
            ModuleObject(
                id = 2L, items = listOf(
                    ModuleItem(id = 200L, moduleId = 2L),
                    ModuleItem(id = 201L, moduleId = 2L)
                )
            )
        )
        val event = ModuleListEvent.BulkUpdateAllModules(BulkModuleUpdateAction.UNPUBLISH, true)
        val model = initModel.copy(modules = modules)
        val expectedModel = model.copy(
            loadingModuleItemIds = setOf(1L, 2L)
        )
        val expectedEffect = ModuleListEffect.BulkUpdateModules(
            expectedModel.course,
            listOf(1L, 2L),
            listOf(1L, 2L),
            BulkModuleUpdateAction.UNPUBLISH,
            true,
            true
        )

        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateAllModules sets loading for modules and items`() {
        val modules = listOf(
            ModuleObject(id = 1L, items = listOf(ModuleItem(id = 100L, moduleId = 1L))),
            ModuleObject(
                id = 2L, items = listOf(
                    ModuleItem(id = 200L, moduleId = 2L),
                    ModuleItem(id = 201L, moduleId = 2L)
                )
            )
        )
        val event = ModuleListEvent.BulkUpdateAllModules(BulkModuleUpdateAction.UNPUBLISH, false)
        val model = initModel.copy(modules = modules)
        val expectedModel = model.copy(
            loadingModuleItemIds = setOf(1L, 100L, 2L, 200L, 201L)
        )
        val expectedEffect = ModuleListEffect.BulkUpdateModules(
            expectedModel.course,
            listOf(1L, 2L),
            listOf(1L, 2L, 100L, 200L, 201L),
            BulkModuleUpdateAction.UNPUBLISH,
            false,
            true
        )

        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess emits refresh effect and clears loading`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(ModuleObject(1L)),
            loadingModuleItemIds = setOf(1L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.onlyModulesPublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(true, BulkModuleUpdateAction.PUBLISH, true))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess publish all modules and items displays correct snackbar`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L, 100L, 2L, 200L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.allModulesAndAllItemsPublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(false, BulkModuleUpdateAction.PUBLISH, true))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess unpublish all modules and items displays correct snackbar`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L, 100L, 2L, 200L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.allModulesAndAllItemsUnpublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(false, BulkModuleUpdateAction.UNPUBLISH, true))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess publish all modules displays correct snackbar`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L, 2L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.onlyModulesPublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(true, BulkModuleUpdateAction.PUBLISH, true))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess publish single module with all items displays correct snackbar`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L, 100L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.moduleAndAllItemsPublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(false, BulkModuleUpdateAction.PUBLISH, false))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess unpublish single module with all items displays correct snackbar`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L, 100L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.moduleAndAllItemsUnpublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(false, BulkModuleUpdateAction.UNPUBLISH, false))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateSuccess publish single module displays correct snackbar`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.onlyModulePublished)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateSuccess(true, BulkModuleUpdateAction.PUBLISH, false))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateFailed clears loading`() {
        val model = initModel.copy(
            modules = listOf(ModuleObject(1L)),
            loadingModuleItemIds = setOf(1L)
        )

        val expectedModel = model.copy(
            loadingModuleItemIds = emptySet()
        )
        val expectedSnackbarEffect = ModuleListEffect.ShowSnackbar(R.string.errorOccurred)

        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateFailed(false))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedSnackbarEffect)
                )
            )
    }

    @Test
    fun `UpdateModuleItem emits UpdateModuleItem effect`() {
        val model = initModel.copy(
            modules = listOf(ModuleObject(1L, items = listOf(ModuleItem(100L)))),
        )
        val expectedModel = model.copy(
            loadingModuleItemIds = setOf(100L)
        )
        val expectedEffect = ModuleListEffect.UpdateModuleItem(
            model.course,
            1L,
            100L,
            true
        )
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.UpdateModuleItem(100L, true))
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `ModuleItemUpdateSuccess replaces module item`() {
        val model = initModel.copy(
            modules = listOf(ModuleObject(1L, items = listOf(ModuleItem(100L, 1L, published = false)))),
            loadingModuleItemIds = setOf(100L)
        )
        val expectedModel = initModel.copy(
            modules = listOf(ModuleObject(1L, items = listOf(ModuleItem(100L, 1L, published = true)))),
            loadingModuleItemIds = emptySet()
        )
        val event = ModuleListEvent.ModuleItemUpdateSuccess(ModuleItem(100L, 1L, published = true), true)
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `ModuleItemUpdateFailed clears loading`() {
        val model = initModel.copy(
            modules = listOf(ModuleObject(1L, items = listOf(ModuleItem(100L, 1L, published = false)))),
            loadingModuleItemIds = setOf(100L)
        )
        val expectedModel = model.copy(
            loadingModuleItemIds = emptySet()
        )
        val event = ModuleListEvent.ModuleItemUpdateFailed(100L)
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `UpdateFileModuleItem emits UpdateFileModuleItem effect`() {
        val model = initModel.copy(
            modules = listOf(ModuleObject(1L, items = listOf(ModuleItem(100L, 1L, published = false)))),
        )
        val expectedEffect = ModuleListEffect.UpdateFileModuleItem(
            100L,
            ModuleContentDetails()
        )
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.UpdateFileModuleItem(100L, ModuleContentDetails()))
            .then(
                assertThatNext(
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `BulkUpdateCancelled emits LoadNextPage effect`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L)
        )
        val expectedModel = initModel.copy(
            isLoading = true,
            pageData = ModuleListPageData(forceNetwork = true),
            loadingModuleItemIds = emptySet()
        )
        val expectedEffect = ModuleListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        val snackbarEffect = ModuleListEffect.ShowSnackbar(R.string.updateCancelled)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.BulkUpdateCancelled)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect, snackbarEffect)
                )
            )
    }

    @Test
    fun `ShowSnackbar event emits ShowSnackbar effect`() {
        val model = initModel.copy(
            isLoading = false,
            pageData = ModuleListPageData(DataResult.Success(emptyList()), false, "fakeUrl"),
            modules = listOf(
                ModuleObject(1L, items = listOf(ModuleItem(100L))),
                ModuleObject(2L, items = listOf(ModuleItem(200L)))
            ),
            loadingModuleItemIds = setOf(1L)
        )
        val params = arrayOf<Any>("param")
        val snackbarEffect = ModuleListEffect.ShowSnackbar(R.string.error_unpublishable_module_item, params)
        updateSpec
            .given(model)
            .whenEvent(ModuleListEvent.ShowSnackbar(R.string.error_unpublishable_module_item, params))
            .then(
                assertThatNext(
                    matchesEffects(snackbarEffect)
                )
            )
    }


}
