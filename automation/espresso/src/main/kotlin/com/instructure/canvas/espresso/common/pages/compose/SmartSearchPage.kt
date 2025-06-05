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

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.espresso.page.BasePage

class SmartSearchPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertQuery(query: String) {
        composeTestRule.onNode(
            hasParent(hasTestTag("searchBar")).and(hasTestTag("searchField")),
            useUnmergedTree = true
        )
            .assertExists()
            .assertTextEquals(query)
    }

    fun assertCourse(courseName: String) {
        composeTestRule.onNode(
            hasTestTag("courseTitle"),
            useUnmergedTree = true
        )
            .assertExists()
            .assertTextEquals(courseName)
    }

    fun assertItemDisplayed(title: String, type: String) {
        val resultMatcher = hasTestTag("resultItem")
            .and(
                hasAnyChild(hasTestTag("resultTitle").and(hasText(title)))
            )
            .and(
                hasAnyChild(hasTestTag("resultType").and(hasText(type)))
            )
        composeTestRule.onNodeWithTag("results", useUnmergedTree = true)
            .performScrollToNode(resultMatcher)

        composeTestRule.onNode(resultMatcher, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    fun assertItemNotDisplayed(title: String, type: String) {
        composeTestRule.onNode(
            hasTestTag("resultItem")
                .and(
                    hasAnyChild(hasTestTag("resultTitle").and(hasText(title)))
                )
                .and(
                    hasAnyChild(hasTestTag("resultType").and(hasText(type)))
                ), useUnmergedTree = true
        )
            .assertDoesNotExist()
    }

    fun clickOnItem(title: String) {
        composeTestRule.onNode(
            hasTestTag("resultItem")
                .and(
                    hasAnyChild(hasTestTag("resultTitle").and(hasText(title)))
                ),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickOnFilters() {
        composeTestRule.onNode(
            hasTestTag("filterButton"),
            useUnmergedTree = true
        )
            .performClick()
    }

    fun assertGroupHeaderDisplayed(type: SmartSearchContentType) {
        composeTestRule.onNodeWithTag("results", useUnmergedTree = true)
            .performScrollToNode(hasTestTag("${type.name.lowercase()}GroupHeader"))

        composeTestRule.onNodeWithTag("${type.name.lowercase()}GroupHeader", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    fun assertGroupItemCount(expectedCount: String, type: SmartSearchContentType) {
        composeTestRule.onNode(hasTestTag("groupHeaderTitle") and hasAnyAncestor(hasTestTag("${type.name.lowercase()}GroupHeader")) and hasText("($expectedCount)", substring = true), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertGroupHeaderNotDisplayed(type: SmartSearchContentType) {
        composeTestRule.onNodeWithTag("${type.name.lowercase()}GroupHeader", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    fun toggleGroup(type: SmartSearchContentType) {
        composeTestRule.onNodeWithTag("results", useUnmergedTree = true)
            .performScrollToNode(hasTestTag("${type.name.lowercase()}GroupHeader"))
        composeTestRule.onNodeWithTag("${type.name.lowercase()}GroupHeader", useUnmergedTree = true)
            .performClick()
    }
}