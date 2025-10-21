/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.features.smartsearch

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.pandautils.features.smartsearch.SmartSearchPreferencesScreen
import com.instructure.pandautils.features.smartsearch.SmartSearchSortType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmartSearchPreferencesScreenTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun assertFilterItems() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = listOf(SmartSearchFilter.ASSIGNMENTS, SmartSearchFilter.ANNOUNCEMENTS),
                sortType = SmartSearchSortType.RELEVANCE,
                onDone = { _, _ -> },
                onCancel = {}
            )
        }

        listOf("assignmentsFilterRow", "announcementsFilterRow").forEach {
            composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
                .performScrollToNode(hasTestTag(it))

            composeTestRule.onNodeWithTag(it)
                .assertIsDisplayed()
                .assertHasClickAction()
            composeTestRule.onNode(
                hasTestTag("checkbox").and(hasParent(hasTestTag(it))),
                useUnmergedTree = true
            )
                .assertIsDisplayed()
                .assertIsOn()
        }

        listOf("pagesFilterRow", "discussion_topicsFilterRow").forEach {
            composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
                .performScrollToNode(hasTestTag(it))

            composeTestRule.onNodeWithTag(it)
                .assertIsDisplayed()
                .assertHasClickAction()
            composeTestRule.onNode(
                hasTestTag("checkbox").and(hasParent(hasTestTag(it))),
                useUnmergedTree = true
            )
                .assertIsDisplayed()
                .assertIsOff()
        }
    }

    @Test
    fun assertSelectAllButton() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = listOf(SmartSearchFilter.ASSIGNMENTS, SmartSearchFilter.ANNOUNCEMENTS),
                sortType = SmartSearchSortType.RELEVANCE,
                onDone = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithTag("toggleAllButton")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertTextEquals("Select All")
            .performClick()

        SmartSearchFilter.entries.forEach { filter ->
            composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
                .performScrollToNode(hasTestTag("${filter.name.lowercase()}FilterRow"))
            composeTestRule.onNode(
                hasTestTag("checkbox").and(hasParent(hasTestTag("${filter.name.lowercase()}FilterRow"))),
                useUnmergedTree = true
            )
                .assertIsDisplayed()
                .assertIsOn()
        }

        composeTestRule.onNodeWithTag("toggleAllButton")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertTextEquals("Unselect All")
            .performClick()

        SmartSearchFilter.entries.forEach { filter ->
            composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
                .performScrollToNode(hasTestTag("${filter.name.lowercase()}FilterRow"))
            composeTestRule.onNode(
                hasTestTag("checkbox").and(hasParent(hasTestTag("${filter.name.lowercase()}FilterRow"))),
                useUnmergedTree = true
            )
                .assertIsDisplayed()
                .assertIsOff()
        }
    }

    @Test
    fun assertFilterRowClickable() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = emptyList(),
                sortType = SmartSearchSortType.RELEVANCE,
                onDone = { _, _ -> },
                onCancel = {}
            )
        }

        val assignmentRow = composeTestRule.onNodeWithTag("assignmentsFilterRow")
        val assignmentCheckbox = composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("assignmentsFilterRow"))),
            useUnmergedTree = true
        )

        assignmentRow.assertHasClickAction()

        assignmentRow.performClick()
        assignmentCheckbox.assertIsOn()

        assignmentRow.performClick()
        assignmentCheckbox.assertIsOff()
    }

    @Test
    fun assertCheckboxClickable() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = emptyList(),
                sortType = SmartSearchSortType.RELEVANCE,
                onDone = { _, _ -> },
                onCancel = {}
            )
        }

        val assignmentCheckbox = composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("assignmentsFilterRow"))),
            useUnmergedTree = true
        )

        assignmentCheckbox.performClick()
        assignmentCheckbox.assertIsOn()

        assignmentCheckbox.performClick()
        assignmentCheckbox.assertIsOff()
    }

    @Test
    fun assertRelevanceTypeSelector() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = emptyList(),
                sortType = SmartSearchSortType.TYPE,
                onDone = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithTag("typeRadioButton")
            .assertIsSelected()

        composeTestRule.onNodeWithTag("relevanceTypeSelector")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertTextEquals("Relevance")
            .performClick()

        composeTestRule.onNodeWithTag("relevanceRadioButton")
            .assertIsSelected()
        composeTestRule.onNodeWithTag("typeRadioButton")
            .assertIsNotSelected()
    }

    @Test
    fun assertTypeTypeSelector() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = emptyList(),
                sortType = SmartSearchSortType.RELEVANCE,
                onDone = { _, _ -> },
                onCancel = {}
            )
        }

        composeTestRule.onNodeWithTag("relevanceRadioButton")
            .assertIsSelected()

        composeTestRule.onNodeWithTag("typeTypeSelector")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertTextEquals("Type")
            .performClick()

        composeTestRule.onNodeWithTag("typeRadioButton")
            .assertIsSelected()
        composeTestRule.onNodeWithTag("relevanceRadioButton")
            .assertIsNotSelected()
    }
}