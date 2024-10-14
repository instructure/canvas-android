/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp


class GradesPage(private val composeTestRule: ComposeTestRule) {

    fun clickGroupHeader(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .performClick()
    }

    fun assertAssignmentIsDisplayed(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .assertIsDisplayed()
    }

    fun assertAssignmentIsNotDisplayed(name: String) {
        composeTestRule.onNodeWithText(name)
            .assertIsNotDisplayed()
    }

    fun assertGradeText(grade: String) {
        composeTestRule.onNodeWithText(grade)
            .assertIsDisplayed()
    }

    fun clickBasedOnGradedAssignments() {
        composeTestRule.onNodeWithText("Based on graded assignments")
            .performClick()
    }

    fun assertGroupHeaderIsDisplayed(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .assertIsDisplayed()
    }

    fun assertGroupHeaderIsNotDisplayed(name: String) {
        composeTestRule.onNodeWithText(name)
            .assertIsNotDisplayed()
    }

    fun clickFilterButton() {
        composeTestRule.onNodeWithContentDescription("Filter")
            .performClick()
    }

    fun clickFilterOption(option: String) {
        composeTestRule.onNodeWithText(option)
            .performClick()
    }

    fun clickSaveButton() {
        composeTestRule.onNodeWithText("Save")
            .performClick()
    }

    fun clickAssignment(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .performClick()
    }

    fun assertEmptyStateIsDisplayed() {
        composeTestRule.onNodeWithText("No Assignments")
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun scrollScreen() {
        composeTestRule.onNodeWithTag("gradesList")
            .performTouchInput { swipeUp() }
    }

    fun assertCardText(text: String) {
        composeTestRule.onNodeWithTag("gradesCardText", true)
            .assertTextEquals(text)
            .assertIsDisplayed()
    }
}