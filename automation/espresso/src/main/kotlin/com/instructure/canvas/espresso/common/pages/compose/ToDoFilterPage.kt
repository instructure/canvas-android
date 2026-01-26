/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.R

class ToDoFilterPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertFilterScreenTitle() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterPreferences))
            .assertIsDisplayed()
    }

    fun clickDone() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.done))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickClose() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.close))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun selectVisibleItemsOption(labelResId: Int) {
        selectOptionInSection(R.string.todoFilterVisibleItems, labelResId)
    }

    fun selectShowTasksFromOption(labelResId: Int) {
        selectOptionInSection(R.string.todoFilterShowTasksFrom, labelResId, "ShowTasksFromOptions")
    }

    fun selectShowTasksUntilOption(labelResId: Int) {
        selectOptionInSection(R.string.todoFilterShowTasksUntil, labelResId, "ShowTasksUntilOptions")
    }

    fun assertVisibleItemsSection() {
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(getStringFromResource(R.string.todoFilterVisibleItems)))
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterVisibleItems)).performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterShowPersonalToDos)).performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterShowCalendarEvents)).performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterShowCompleted)).performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterFavoriteCoursesOnly)).performScrollTo()
            .assertIsDisplayed()
    }

    fun assertShowTasksFromSection() {
        val sectionHeader = getStringFromResource(R.string.todoFilterShowTasksFrom)
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(sectionHeader))

        val items = listOf(
            R.string.todoFilterShowTasksFrom,
            R.string.todoFilterFourWeeks,
            R.string.todoFilterThreeWeeks,
            R.string.todoFilterTwoWeeks,
            R.string.todoFilterLastWeek,
            R.string.todoFilterThisWeek,
            R.string.todoFilterToday
        )

        items.forEachIndexed { index, labelResId ->
            val labelText = getStringFromResource(labelResId)
            if (index == 0) {
                composeTestRule.onNodeWithTag("ToDoFilterContent")
                    .performScrollToNode(hasText(labelText))
                composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
            } else {
                composeTestRule.onNodeWithTag("ToDoFilterContent")
                    .performScrollToNode(
                        hasAnyAncestor(hasTestTag("ShowTasksFromOptions")) and hasText(labelText)
                    )
                composeTestRule.onNode(
                    hasAnyAncestor(hasTestTag("ShowTasksFromOptions")) and hasText(labelText)
                ).assertIsDisplayed()
            }
        }
    }

    fun assertShowTasksUntilSection() {
        val sectionHeader = getStringFromResource(R.string.todoFilterShowTasksUntil)
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(sectionHeader))

        val items = listOf(
            R.string.todoFilterShowTasksUntil,
            R.string.todoFilterToday,
            R.string.todoFilterThisWeek,
            R.string.todoFilterNextWeek,
            R.string.todoFilterInTwoWeeks,
            R.string.todoFilterInThreeWeeks,
            R.string.todoFilterInFourWeeks
        )

        items.forEachIndexed { index, labelResId ->
            val labelText = getStringFromResource(labelResId)
            if (index == 0) {
                composeTestRule.onNodeWithTag("ToDoFilterContent")
                    .performScrollToNode(hasText(labelText))
                composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
            } else {
                composeTestRule.onNodeWithTag("ToDoFilterContent")
                    .performScrollToNode(
                        hasAnyAncestor(hasTestTag("ShowTasksUntilOptions")) and hasText(labelText)
                    )
                composeTestRule.onNode(
                    hasAnyAncestor(hasTestTag("ShowTasksUntilOptions")) and hasText(labelText)
                ).assertIsDisplayed()
            }
        }
    }

    fun assertToDoFilterScreenDetails() {
        assertVisibleItemsSection()
        assertShowTasksFromSection()
        assertShowTasksUntilSection()
    }

    fun assertVisibleItemOptionCheckedState(labelResId: Int, isChecked: Boolean) {
        val sectionHeader = getStringFromResource(R.string.todoFilterVisibleItems)
        val labelText = getStringFromResource(labelResId)
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(sectionHeader))
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(labelText))
        val node = composeTestRule.onNode(
            hasTestTag("checkboxItem") and hasAnyAncestor(hasTestTag("checkboxItemRow") and hasContentDescription(labelText)),
            useUnmergedTree = true
        )
        if (isChecked) node.assertIsOn()
        else node.assertIsOff()
    }

    fun assertShowTasksFromOptionSelectedState(labelResId: Int, isSelected: Boolean) {
        val sectionHeader = getStringFromResource(R.string.todoFilterShowTasksFrom)
        val labelText = getStringFromResource(labelResId)
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(sectionHeader))
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(
                hasAnyAncestor(hasTestTag("ShowTasksFromOptions")) and hasText(labelText)
            )
        val node = composeTestRule.onNode(
            hasTestTag("radioButtonItem") and hasAnyAncestor(
                hasTestTag("radioButtonRow") and hasAnyDescendant(hasText(labelText))
            ) and hasAnyAncestor(hasTestTag("ShowTasksFromOptions")),
            useUnmergedTree = true
        )
        if (isSelected) node.assertIsSelected()
        else node.assertIsNotSelected()
    }

    fun assertShowTasksUntilOptionSelectedState(labelResId: Int, isSelected: Boolean) {
        val sectionHeader = getStringFromResource(R.string.todoFilterShowTasksUntil)
        val labelText = getStringFromResource(labelResId)
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(sectionHeader))
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(
                hasAnyAncestor(hasTestTag("ShowTasksUntilOptions")) and hasText(labelText)
            )
        val node = composeTestRule.onNode(
            hasTestTag("radioButtonItem") and hasAnyAncestor(
                hasTestTag("radioButtonRow") and hasAnyDescendant(hasText(labelText))
            ) and hasAnyAncestor(hasTestTag("ShowTasksUntilOptions")),
            useUnmergedTree = true
        )
        if (isSelected) node.assertIsSelected()
        else node.assertIsNotSelected()
    }

    private fun selectOptionInSection(sectionHeaderResId: Int, labelResId: Int, sectionTestTag: String? = null) {
        val sectionHeader = getStringFromResource(sectionHeaderResId)
        val labelText = getStringFromResource(labelResId)
        composeTestRule.onNodeWithTag("ToDoFilterContent")
            .performScrollToNode(hasText(sectionHeader))

        if (sectionTestTag != null) {
            composeTestRule.onNodeWithTag("ToDoFilterContent")
                .performScrollToNode(
                    hasAnyAncestor(hasTestTag(sectionTestTag)) and hasText(labelText)
                )
            composeTestRule.onNode(
                hasAnyAncestor(hasTestTag(sectionTestTag)) and hasText(labelText)
            ).performClick()
        } else {
            composeTestRule.onNodeWithTag("ToDoFilterContent")
                .performScrollToNode(hasText(labelText))
            composeTestRule.onNodeWithText(labelText).performClick()
        }
        composeTestRule.waitForIdle()
    }
}
