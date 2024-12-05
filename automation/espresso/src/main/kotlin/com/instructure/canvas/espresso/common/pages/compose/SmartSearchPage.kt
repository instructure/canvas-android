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
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
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
        composeTestRule.onNode(
            hasTestTag("resultItem")
                .and(
                    hasAnyChild(hasTestTag("resultTitle").and(hasText(title)))
                )
                .and(
                    hasAnyChild(hasTestTag("resultType").and(hasText(type)))
                ),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assertHasClickAction()
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
    }
}