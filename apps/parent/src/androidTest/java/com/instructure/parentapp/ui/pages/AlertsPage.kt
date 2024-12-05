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
 */    package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText

class AlertsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertAlertItemDisplayed(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertAlertItemNotDisplayed(title: String) {
        composeTestRule.onNodeWithText(title).assertIsNotDisplayed()
    }

    fun assertEmptyState() {
        composeTestRule.onNodeWithTag("emptyAlerts").assertIsDisplayed()
    }

    fun assertAlertRead(title: String) {
        composeTestRule.onNode(
            hasTestTag("unreadIndicator")
                .and(hasAnyAncestor(hasTestTag("alertItem").and(hasAnyDescendant(hasText(title))))),
            useUnmergedTree = true
        ).assertIsNotDisplayed()
    }

    fun assertAlertUnread(title: String) {
        composeTestRule.onNode(
            hasTestTag("unreadIndicator")
                .and(hasAnyAncestor(hasTestTag("alertItem").and(hasAnyDescendant(hasText(title))))),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    fun dismissAlert(title: String) {
        composeTestRule.onNode(
            hasAnyAncestor(hasAnyDescendant(hasText(title)).and(hasTestTag("alertItem"))).and(
                hasTestTag(
                    "dismissButton"
                )
            ),
            useUnmergedTree = true
        ).performClick()
    }

    fun clickOnAlert(title: String) {
        composeTestRule.onNode(
            hasTestTag("alertItem").and(hasAnyDescendant(hasText(title))),
            useUnmergedTree = true
        ).performClick()
    }

    fun refresh() {
        composeTestRule.onRoot().performTouchInput { swipeDown() }
    }

    fun assertSnackbar(message: String) {
        onViewWithText(message).assertDisplayed()
    }

    fun clickUndo() {
        onViewWithText("UNDO").click()
    }
}