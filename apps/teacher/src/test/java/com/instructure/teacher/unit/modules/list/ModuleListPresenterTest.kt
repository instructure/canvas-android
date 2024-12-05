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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ModuleListModel
import com.instructure.teacher.features.modules.list.ModuleListPageData
import com.instructure.teacher.features.modules.list.ModuleListPresenter
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData
import com.instructure.teacher.features.modules.list.ui.ModuleListViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleListPresenterTest : Assert() {

    private val course = Course()
    private lateinit var context: Context

    private lateinit var modelTemplate: ModuleListModel
    private lateinit var moduleTemplate: ModuleObject
    private lateinit var moduleItemTemplate: ModuleItem

    private lateinit var moduleDataTemplate: ModuleListItemData.ModuleData
    private lateinit var moduleItemDataTemplate: ModuleListItemData.ModuleItemData

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        moduleTemplate = ModuleObject(
            id = 1L,
            name = "Module 1",
            published = true
        )
        moduleDataTemplate = ModuleListItemData.ModuleData(
            id = 1L,
            name = "Module 1",
            isPublished = true,
            isLoading = false,
            moduleItems = listOf(ModuleListItemData.EmptyItem(1L))
        )
        moduleItemTemplate = ModuleItem(
            id = 1000L,
            title = "Module Item 1",
            type = "Assignment",
            published = true,
            moduleDetails = ModuleContentDetails(
                dueAt = DateHelper.makeDate(2050, 1, 12, 15, 7, 0).toApiString()
            )
        )

        moduleItemDataTemplate = ModuleListItemData.ModuleItemData(
            id = 1000L,
            title = "Module Item 1",
            subtitle = "February 12, 2050 at 3:07 PM",
            subtitle2 = null,
            iconResId = R.drawable.ic_assignment,
            isPublished = true,
            indent = 0,
            tintColor = course.color,
            enabled = true,
            type = ModuleItem.Type.Assignment,
            contentDetails = ModuleContentDetails(
                dueAt = DateHelper.makeDate(2050, 1, 12, 15, 7, 0).toApiString()
            ),
            contentId = 0
        )
        modelTemplate = ModuleListModel(
            course = course,
            modules = listOf(moduleTemplate),
            pageData = ModuleListPageData(
                lastPageResult = DataResult.Success(listOf(moduleTemplate))
            )
        )
    }

    @Test
    fun `Returns empty state for no data`() {
        val model = ModuleListModel(
            course = course,
            pageData = ModuleListPageData(
                lastPageResult = DataResult.Success(emptyList())
            )
        )
        val expectedState = ModuleListViewState(
            items = listOf(ModuleListItemData.Empty)
        )
        val actualState = ModuleListPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns full error state for error on first page`() {
        val model = ModuleListModel(
            course = course,
            pageData = ModuleListPageData(
                lastPageResult = DataResult.Fail()
            )
        )
        val expectedState = ModuleListViewState(
            items = listOf(ModuleListItemData.FullError(ThemePrefs.buttonColor))
        )
        val actualState = ModuleListPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns inline error state for error after first page`() {
        val model = modelTemplate.copy(
            pageData = ModuleListPageData(
                lastPageResult = DataResult.Fail()
            ),
            modules = listOf(moduleTemplate)
        )
        val expectedState = ModuleListViewState(
            items = listOf(
                moduleDataTemplate,
                ModuleListItemData.InlineError(ThemePrefs.buttonColor)
            )
        )
        val actualState = ModuleListPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns inline loading state if loading after first page`() {
        val model = modelTemplate.copy(
            pageData = ModuleListPageData(
                lastPageResult = DataResult.Success(emptyList()),
                nextPageUrl = "fake_url"
            ),
            modules = listOf(moduleTemplate)
        )
        val expectedState = ModuleListViewState(
            items = listOf(
                moduleDataTemplate,
                ModuleListItemData.Loading
            )
        )
        val actualState = ModuleListPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns loaded state`() {
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(moduleItemTemplate))
            )
        )
        val expectedState = ModuleListViewState(
            items = listOf(
                moduleDataTemplate.copy(moduleItems = listOf(moduleItemDataTemplate))
            )
        )
        val actualState = ModuleListPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct state for Assignment module item`() {
        val item = moduleItemTemplate.copy(
            title = "Assignment item",
            type = "Assignment"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_assignment
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for Discussion module item`() {
        val item = moduleItemTemplate.copy(
            title = "Discussion item",
            type = "Discussion"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_discussion,
            type = ModuleItem.Type.Discussion
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for File module item`() {
        val item = moduleItemTemplate.copy(
            title = "File item",
            type = "File"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_attachment,
            type = ModuleItem.Type.File
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for Page module item`() {
        val item = moduleItemTemplate.copy(
            title = "Page item",
            type = "Page"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_pages,
            type = ModuleItem.Type.Page
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for Quiz module item`() {
        val item = moduleItemTemplate.copy(
            title = "Quiz item",
            type = "Quiz"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_quiz,
            type = ModuleItem.Type.Quiz
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for ExternalUrl module item`() {
        val item = moduleItemTemplate.copy(
            title = "ExternalUrl item",
            type = "ExternalUrl"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_link,
            type = ModuleItem.Type.ExternalUrl
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for ExternalTool module item`() {
        val item = moduleItemTemplate.copy(
            title = "Assignment ExternalTool ",
            type = "ExternalTool"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_lti,
            type = ModuleItem.Type.ExternalTool
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for New Quiz module item`() {
        val item = moduleItemTemplate.copy(
            title = "New Quiz",
            type = "Assignment",
            quizLti = true
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            iconResId = R.drawable.ic_quiz,
            type = ModuleItem.Type.Assignment
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for SubHeader`() {
        val item = moduleItemTemplate.copy(
            title = "This is a header",
            type = "SubHeader"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            )
        )
        val expectedState = ModuleListItemData.SubHeader(1000L, "This is a header", 0, false, true, false)
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

    @Test
    fun `Returns correct state for loading item`() {
        val item = moduleItemTemplate.copy(
            title = "File item",
            type = "File"
        )
        val model = modelTemplate.copy(
            modules = listOf(
                moduleTemplate.copy(items = listOf(item))
            ),
            loadingModuleItemIds = setOf(item.id)
        )
        val expectedState = moduleItemDataTemplate.copy(
            title = item.title,
            enabled = false,
            isLoading = true,
            iconResId = R.drawable.ic_attachment,
            type = ModuleItem.Type.File
        )
        val viewState = ModuleListPresenter.present(model, context)
        val itemState = (viewState.items[0] as ModuleListItemData.ModuleData).moduleItems.first()
        assertEquals(expectedState, itemState)
    }

}
