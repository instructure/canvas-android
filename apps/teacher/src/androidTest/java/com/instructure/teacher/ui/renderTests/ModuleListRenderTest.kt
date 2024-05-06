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
package com.instructure.teacher.ui.renderTests

import android.graphics.Color
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.espresso.assertCompletelyDisplayed
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ui.ModuleListFragment
import com.instructure.teacher.features.modules.list.ui.ModuleListItemData
import com.instructure.teacher.features.modules.list.ui.ModuleListViewState
import com.instructure.teacher.ui.renderTests.pages.ModuleListRenderPage
import com.instructure.teacher.ui.utils.TeacherRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import java.util.Date

@HiltAndroidTest
class ModuleListRenderTest : TeacherRenderTest() {

    private val page = ModuleListRenderPage()
    private lateinit var moduleTemplate: ModuleListItemData.ModuleData
    private lateinit var moduleItemTemplate: ModuleListItemData.ModuleItemData

    @Before
    fun setUp() {
        moduleTemplate = ModuleListItemData.ModuleData(
            id = 1L,
            name = "Module 1",
            isPublished = true,
            isLoading = false,
            moduleItems = emptyList()
        )
        moduleItemTemplate = ModuleListItemData.ModuleItemData(
            id = 2L,
            title = "Assignment Module Item",
            subtitle = "Due Tomorrow",
            subtitle2 = "10 pts",
            iconResId = R.drawable.ic_assignment,
            isPublished = true,
            indent = 0,
            tintColor = Color.BLUE,
            enabled = true,
            type = ModuleItem.Type.Assignment
        )
    }

    @Test
    fun displaysFullError() {
        val state = ModuleListViewState(
            items = listOf(ModuleListItemData.FullError(Color.BLUE))
        )
        loadPageWithViewState(state)
        page.fullErrorView.assertDisplayed()
    }

    @Test
    fun setsUpToolbar() {
        val course = Course(name = "This is a Test Course!")
        loadPageWithViewState(ModuleListViewState(), course)
        page.assertDisplaysToolbarText("Modules")
        page.assertDisplaysToolbarText(course.name)
    }

    @Test
    fun displaysInlineError() {
        val state = ModuleListViewState(
            items = listOf(
                ModuleListItemData.ModuleData(1, "Module 1", true, emptyList(), false),
                ModuleListItemData.ModuleData(2, "Module 2", true, emptyList(), false),
                ModuleListItemData.ModuleData(3, "Module 3", true, emptyList(), false),
                ModuleListItemData.InlineError(Color.BLUE)
            )
        )
        loadPageWithViewState(state)
        page.inlineErrorView.assertDisplayed()
    }

    @Test
    fun displaysEmptyState() {
        val state = ModuleListViewState(
            items = listOf(ModuleListItemData.Empty)
        )
        loadPageWithViewState(state)
        page.emptyView.assertDisplayed()
    }

    @Test
    fun displaysEmptyModule() {
        val module = ModuleListItemData.ModuleData(1, "Module 1", true, emptyList(), false)
        val state = ModuleListViewState(
            items = listOf(module)
        )
        loadPageWithViewState(state)
        page.moduleName.assertHasText(module.name)
    }

    @Test
    fun displaysInitialLoadingView() {
        val state = ModuleListViewState(
            showRefreshing = true
        )
        loadPageWithViewState(state)
        page.assertRefreshing(true)
    }

    @Test
    fun displaysInlineLoadingView() {
        val state = ModuleListViewState(
            items = listOf(
                ModuleListItemData.ModuleData(1, "Module 1", true, emptyList(), false),
                ModuleListItemData.Loading
            )
        )
        loadPageWithViewState(state)
        page.inlineLoadingView.assertDisplayed()
    }

    @Test
    fun displaysModuleItem() {
        val state = ModuleListViewState(
            items = listOf(moduleItemTemplate)
        )
        loadPageWithViewState(state)
        page.moduleItemIcon.assertDisplayed()
        page.moduleItemTitle.assertDisplayed()
        page.moduleItemTitle.assertHasText(moduleItemTemplate.title!!)
    }

    @Test
    fun displaysModuleItemPublishIcon() {
        val moduleItem = moduleItemTemplate.copy(
            isPublished = true
        )
        val state = ModuleListViewState(
            items = listOf(moduleItem)
        )
        loadPageWithViewState(state)
        page.assertStatusIconContentDescription(R.string.a11y_published)
    }

    @Test
    fun displaysModuleItemUnpublishedIcon() {
        val moduleItem = moduleItemTemplate.copy(
            isPublished = false
        )
        val state = ModuleListViewState(
            items = listOf(moduleItem)
        )
        loadPageWithViewState(state)
        page.assertStatusIconContentDescription(R.string.a11y_unpublished)
    }

    @Test
    fun displaysSubHeaderModuleItem() {
        val moduleItem = moduleItemTemplate.copy(
            title = null,
            iconResId = null,
            subtitle = "This is a SubHeader"
        )
        val state = ModuleListViewState(
            items = listOf(moduleItem)
        )
        loadPageWithViewState(state)
        page.moduleItemIcon.assertNotDisplayed()
        page.moduleItemTitle.assertNotDisplayed()
        page.moduleItemSubtitle.assertDisplayed()
        page.moduleItemSubtitle.assertHasText(moduleItem.subtitle!!)
    }

    @Test
    fun expandsAndCollapsesModule() {
        val itemCount = 5
        val state = ModuleListViewState(
            items = listOf(
                ModuleListItemData.ModuleData(
                    1, "Module 1", true,
                    List(itemCount - 1) { idx ->
                        ModuleListItemData.ModuleItemData(
                            id = idx + 2L,
                            title = "Module Item ${idx + 1}",
                            subtitle = null,
                            subtitle2 = null,
                            iconResId = R.drawable.ic_assignment,
                            isPublished = false,
                            isLoading = false,
                            indent = 0,
                            tintColor = Color.BLUE,
                            enabled = true,
                            type = ModuleItem.Type.Assignment
                        )
                    }, false
                )
            )
        )
        loadPageWithViewState(state)

        // Assert expected item count as a precondition
        page.assertListItemCount(itemCount)

        // Click the first item (the module header) and assert it has collapsed to a single item
        page.clickItemAtPosition(0)
        page.assertListItemCount(1)

        // Click the module again and assert it has expanded to the original count
        page.clickItemAtPosition(0)
        page.assertListItemCount(itemCount)
    }

    @Test
    fun displaysModulePublishIcon() {
        val state = ModuleListViewState(
            items = listOf(
                moduleTemplate.copy(isPublished = true)
            )
        )
        loadPageWithViewState(state)
        page.modulePublishedIcon.assertDisplayed()
        page.moduleUnpublishedIcon.assertNotDisplayed()
    }

    @Test
    fun displaysModuleUnpublishedIcon() {
        val state = ModuleListViewState(
            items = listOf(
                moduleTemplate.copy(isPublished = false)
            )
        )
        loadPageWithViewState(state)
        page.moduleUnpublishedIcon.assertDisplayed()
        page.modulePublishedIcon.assertNotDisplayed()
    }

    @Test
    fun doesNotDisplayModulePublishStatusIcon() {
        val state = ModuleListViewState(
            items = listOf(
                moduleTemplate.copy(isPublished = null)
            )
        )
        loadPageWithViewState(state)
        page.moduleUnpublishedIcon.assertNotDisplayed()
        page.modulePublishedIcon.assertNotDisplayed()
    }

    @Test
    fun displaysModuleItemDueDate() {
        val item = moduleItemTemplate.copy(subtitle = "Due Tomorrow")
        val state = ModuleListViewState(
            items = listOf(item)
        )
        loadPageWithViewState(state)
        page.moduleItemSubtitle.assertDisplayed()
        page.moduleItemSubtitle.assertHasText(item.subtitle!!)
    }

    @Test
    fun hidesModuleItemDueDate() {
        val item = moduleItemTemplate.copy(subtitle = null)
        val state = ModuleListViewState(
            items = listOf(item)
        )
        loadPageWithViewState(state)
        page.moduleItemSubtitle.assertNotDisplayed()
    }

    @Test
    fun loadsWithModuleCollapsed() {
        val itemCount = 5
        val state = ModuleListViewState(
            items = listOf(
                ModuleListItemData.ModuleData(
                    1L, "Module 1", true,
                    List(itemCount - 1) { idx ->
                        ModuleListItemData.ModuleItemData(
                            id = idx + 2L,
                            title = "Module Item ${idx + 1}",
                            subtitle = null,
                            subtitle2 = null,
                            iconResId = R.drawable.ic_assignment,
                            isPublished = false,
                            isLoading = false,
                            indent = 0,
                            tintColor = Color.BLUE,
                            enabled = true,
                            type = ModuleItem.Type.Assignment
                        )
                    }, false
                )
            ),
            collapsedModuleIds = setOf(1L)
        )
        loadPageWithViewState(state)
        page.assertListItemCount(1)
    }

    @Test
    fun scrollsToTargetItem() {
        val itemCount = 50
        val targetItem = ModuleListItemData.ModuleItemData(
            1234L,
            "This is the target item",
            null,
            null,
            R.drawable.ic_attachment,
            false,
            0,
            Color.BLUE,
            true,
            type = ModuleItem.Type.Assignment
        )
        val state = ModuleListViewState(
            items = listOf(
                ModuleListItemData.ModuleData(
                    1, "Module 1", true,
                    List(itemCount - 1) { idx ->
                        if (idx == 35) {
                            targetItem
                        } else {
                            moduleItemTemplate.copy(
                                id = idx + 2L,
                                title = "Module Item ${idx + 1}",
                                isLoading = false
                            )
                        }
                    }, false
                )
            )
        )
        val fragment = loadPageWithViewState(state)
        page.waitForViewWithId(R.id.moduleName)
        activityRule.runOnUiThread {
            fragment.view.scrollToItem(targetItem.id)
        }
        page.onViewWithText(targetItem.title!!).assertCompletelyDisplayed()
    }

    @Test
    fun displaysModuleItemWithIndent() {
        val item = moduleItemTemplate.copy(indent = 100)
        val state = ModuleListViewState(
            items = listOf(item)
        )
        loadPageWithViewState(state)
        page.assertHasItemIndent(item.indent)
    }

    @Test
    fun displaysModuleItemLoadingIndicator() {
        val item = moduleItemTemplate.copy(
            iconResId = null,
            enabled = false,
            isLoading = true
        )
        val state = ModuleListViewState(
            items = listOf(item)
        )
        loadPageWithViewState(state)
        page.moduleItemIcon.assertNotDisplayed()
        page.moduleItemLoadingView.assertDisplayed()
        page.moduleItemRoot.check(matches(not(isEnabled())))
    }

    @Test
    fun displaysFileModuleItemHiddenIcon() {
        val item = moduleItemTemplate.copy(
            iconResId = R.drawable.ic_attachment,
            type = ModuleItem.Type.File,
            contentDetails = ModuleContentDetails(
                hidden = true
            )
        )
        val state = ModuleListViewState(
            items = listOf(item)
        )
        loadPageWithViewState(state)
        page.moduleItemIcon.assertDisplayed()
        page.assertStatusIconContentDescription(R.string.a11y_hidden)
    }

    @Test
    fun displaysFileModuleItemScheduledIcon() {
        val item = moduleItemTemplate.copy(
            iconResId = R.drawable.ic_attachment,
            type = ModuleItem.Type.File,
            contentDetails = ModuleContentDetails(
                hidden = false,
                locked = true,
                unlockAt = Date().toApiString()
            )
        )
        val state = ModuleListViewState(
            items = listOf(item)
        )
        loadPageWithViewState(state)
        page.moduleItemIcon.assertDisplayed()
        page.assertStatusIconContentDescription(R.string.a11y_scheduled)
    }

    private fun loadPageWithViewState(
        state: ModuleListViewState,
        course: Course = Course(name = "Test Course")
    ): ModuleListFragment {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = ModuleListFragment.makeBundle(course)
        val fragment = ModuleListFragment.newInstance(route).apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }

}
