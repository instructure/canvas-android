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
 *
 */

package com.instructure.parentapp.ui.pages.compose

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.instructure.parentapp.utils.ParentComposeWaitMatchers


class NotAParentPage(private val composeTestRule: ComposeTestRule) {

    fun expandAppOptions() {
        composeTestRule.onNodeWithText("Are you a student or teacher?", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    fun clickReturnToLogin() {
        composeTestRule.onNodeWithText("Return to login").performClick()
    }

    fun clickApp(appContentDescription: String) {
        composeTestRule.onNodeWithContentDescription(appContentDescription, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    fun assertStudentAppDisplayed() {
        composeTestRule.onNodeWithContentDescription("Canvas").assertIsDisplayed().assertHasClickAction()
    }

    fun assertTeacherAppDisplayed() {
        composeTestRule.onNodeWithContentDescription("Canvas Teacher").assertIsDisplayed().assertHasClickAction()
    }

    fun assertOtherAppSubtitleDisplayed() {
        ParentComposeWaitMatchers.waitForNodeWithText(composeTestRule, "We couldn't find any students associated with your account")
    }

    fun assertNotAParentPageDetails() {
        ParentComposeWaitMatchers.waitForNodeWithText(composeTestRule, "Not a parent?")
        composeTestRule.onNodeWithText("We couldn't find any students associated with your account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Return to login")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText("Are you a student or teacher?")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
