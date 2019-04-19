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
package com.instructure.teacher.unit.modules

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.teacher.features.modules.list.*
import com.instructure.teacher.unit.utils.matchesEffects
import com.instructure.teacher.unit.utils.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Test

class ModuleListUpdateTest : Assert() {

    private val initSpec = InitSpec(ModuleListUpdate()::init)
    private val updateSpec = UpdateSpec(ModuleListUpdate()::update)

    private val course = Course()
    private val initModel = ModulesListModel(course)

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        val expectedEffect = ModulesListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst<ModulesListModel, ModulesListEffect>(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `Initializes into a loading state with scrollToItemId`() {
        val itemId = 123L
        val expectedModel = initModel.copy(isLoading = true, scrollToItemId = itemId)
        val expectedEffect = ModulesListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        initSpec
            .whenInit(initModel.copy(scrollToItemId = itemId))
            .then(
                assertThatFirst<ModulesListModel, ModulesListEffect>(
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
        val expectedEffect = ModulesListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        updateSpec
            .given(model)
            .whenEvent(ModulesListEvent.PullToRefresh)
            .then(
                assertThatNext<ModulesListModel, ModulesListEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `ModuleClicked event emits ShowModuleDetailView effect`() {
        val item = ModuleItem(id = 123L)
        val event = ModulesListEvent.ModuleItemClicked(item)
        val expectedEffect = ModulesListEffect.ShowModuleItemDetailView(item)
        updateSpec
            .given(initModel)
            .whenEvent(event)
            .then(
                assertThatNext<ModulesListModel, ModulesListEffect>(
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `PageLoaded event with failed result correctly updates model`() {
        val event = ModulesListEvent.PageLoaded(
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
                assertThatNext<ModulesListModel, ModulesListEffect>(
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
        val event = ModulesListEvent.PageLoaded(
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
                assertThatNext<ModulesListModel, ModulesListEffect>(
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
        val event = ModulesListEvent.PageLoaded(
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
                assertThatNext<ModulesListModel, ModulesListEffect>(
                    hasModel(expectedModel),
                    matchesEffects(ModulesListEffect.ScrollToItem(itemId))
                )
            )
    }

    @Test
    fun `NextPageRequested event results in no change if already loading`() {
        val event = ModulesListEvent.NextPageRequested
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
        val event = ModulesListEvent.NextPageRequested
        val model = initModel.copy(isLoading = false)
        val expectedModel = initModel.copy(isLoading = true)
        val expectedEffect = ModulesListEffect.LoadNextPage(
            expectedModel.course,
            expectedModel.pageData,
            expectedModel.scrollToItemId
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext<ModulesListModel, ModulesListEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

}
