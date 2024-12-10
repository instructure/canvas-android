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
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.pandautils.features.smartsearch.SmartSearchPreferencesScreen
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
                navigationClick = {}
            )
        }

        composeTestRule.onNodeWithTag("assignmentsFilterRow")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("assignmentsFilterRow"))),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assertIsOn()

        composeTestRule.onNodeWithTag("announcementsFilterRow")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("announcementsFilterRow"))),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assertIsOn()

        composeTestRule.onNodeWithTag("pagesFilterRow")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("pagesFilterRow"))),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assertIsOff()

        composeTestRule.onNodeWithTag("discussion_topicsFilterRow")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("discussion_topicsFilterRow"))),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assertIsOff()
    }

    @Test
    fun assertSelectAllButton() {
        composeTestRule.setContent {
            SmartSearchPreferencesScreen(
                color = Color.Magenta,
                filters = listOf(SmartSearchFilter.ASSIGNMENTS, SmartSearchFilter.ANNOUNCEMENTS),
                navigationClick = {}
            )
        }

        composeTestRule.onNodeWithTag("toggleAllButton")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertTextEquals("Select All")
            .performClick()

        SmartSearchFilter.entries.forEach { filter ->
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
                navigationClick = {}
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
                navigationClick = {}
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
}