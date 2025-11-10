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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.instructure.canvasapi2.models.SmartSearchFilter

class SmartSearchPreferencesPage(private val composeTestRule: ComposeTestRule) {

    // Actions

    fun clickOnFilter(filter: SmartSearchFilter) {
        composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
            .performScrollToNode(hasTestTag("${filter.name.lowercase()}FilterRow"))
        composeTestRule.onNodeWithTag("${filter.name.lowercase()}FilterRow")
            .performClick()
    }

    fun applyFilters() {
        composeTestRule.onNodeWithTag("doneButton", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun cancelFilters() {
        composeTestRule.onNodeWithTag("navigationButton", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun toggleAll() {
        composeTestRule.onNodeWithTag("toggleAllButton", useUnmergedTree = true)
            .performClick()
    }

    fun selectRelevanceSortType() {
        composeTestRule.onNodeWithTag("relevanceTypeSelector")
            .performClick()
    }

    fun selectTypeSortType() {
        composeTestRule.onNodeWithTag("typeTypeSelector")
            .performClick()
    }

    // Assertions

    fun assertFilterChecked(filter: SmartSearchFilter) {
        composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
            .performScrollToNode(hasTestTag("${filter.name.lowercase()}FilterRow"))
        composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("${filter.name.lowercase()}FilterRow"))),
            useUnmergedTree = true
        )
            .assertIsOn()
    }

    fun assertFilterNotChecked(filter: SmartSearchFilter) {
        composeTestRule.onNodeWithTag("preferencesScreen", useUnmergedTree = true)
            .performScrollToNode(hasTestTag("${filter.name.lowercase()}FilterRow"))
        composeTestRule.onNode(
            hasTestTag("checkbox").and(hasParent(hasTestTag("${filter.name.lowercase()}FilterRow"))),
            useUnmergedTree = true
        )
            .assertIsOff()
    }

    fun assertSortByDetails() {
        composeTestRule.onNodeWithText("Sort By").assertIsDisplayed()
        composeTestRule.onNodeWithTag("relevanceTypeSelector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("typeTypeSelector").assertIsDisplayed()
    }

    fun assertRadioButtonSelected(sortText: String) {
        if(sortText == "Relevance") composeTestRule.onNodeWithTag("relevanceRadioButton").assertIsSelected()
        else if(sortText == "Type") composeTestRule.onNodeWithTag("typeRadioButton").assertIsSelected()
    }
}