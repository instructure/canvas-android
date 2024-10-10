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

package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.canvasapi2.models.User


class ManageStudentsPage(private val composeTestRule: ComposeTestRule) {

    fun assertStudentItemDisplayed(user: User) {
        composeTestRule.onNodeWithText(user.shortName.orEmpty())
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("studentListItem") and hasAnyChild(hasText(user.shortName.orEmpty())), true)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    fun assertStudentItemNotDisplayed(user: User) {
        composeTestRule.onNodeWithText(user.shortName.orEmpty())
            .assertDoesNotExist()
        composeTestRule.onNode(hasTestTag("studentListItem") and hasAnyChild(hasText(user.shortName.orEmpty())), true)
            .assertDoesNotExist()
    }

    fun tapStudent(name: String) {
        composeTestRule.onNodeWithText(name)
            .assertIsDisplayed()
            .performClick()
    }

    fun tapStudentColor(name: String) {
        composeTestRule.onNode(hasTestTag("studentColor") and hasAnySibling(hasText(name)), true)
            .assertIsDisplayed()
            .performClick()
    }

    fun assertColorPickerDialogDisplayed() {
        composeTestRule.onNodeWithText("Select Student Color")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("OK")
            .assertIsDisplayed()
    }

    fun tapAddStudent() {
        composeTestRule.onNodeWithTag("addStudentButton").performClick()
    }
}
