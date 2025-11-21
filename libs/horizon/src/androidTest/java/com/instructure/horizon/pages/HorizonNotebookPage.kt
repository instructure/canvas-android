/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.horizon.R

class HorizonNotebookPage(private val composeTestRule: ComposeTestRule) {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    fun assertAddNoteScreenDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.createNoteTitle))
            .assertIsDisplayed()
    }

    fun assertEditNoteScreenDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteTitle))
            .assertIsDisplayed()
    }

    fun assertHighlightedTextDisplayed(text: String) {
        composeTestRule.onNodeWithText(text)
            .assertIsDisplayed()
    }

    fun assertSaveButtonEnabled() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteSaveButtonLabel))
            .assertIsEnabled()
    }

    fun assertSaveButtonDisabled() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteSaveButtonLabel))
            .assertIsNotEnabled()
    }

    fun assertCancelButtonDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteCancelButtonLabel))
            .assertIsDisplayed()
    }

    fun assertDeleteButtonDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteLabel))
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun assertDeleteButtonNotDisplayed() {
        composeTestRule.onNode(hasText(context.getString(R.string.deleteNoteLabel)))
            .assertDoesNotExist()
    }

    fun assertLastModifiedDateDisplayed(date: String) {
        composeTestRule.onNodeWithText(date)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun assertLoadingDisplayed() {
        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    fun assertDeleteConfirmationDialogDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteConfirmationTitle))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteConfirmationMessage))
            .assertIsDisplayed()
    }

    fun assertExitConfirmationDialogDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteExitConfirmationTitle))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteExitConfirmationMessage))
            .assertIsDisplayed()
    }

    fun assertTypeSelected(type: String) {
        composeTestRule.onNodeWithText(type)
            .assertIsDisplayed()
    }

    fun assertTextAreaPlaceholderDisplayed() {
        composeTestRule.onNodeWithText(context.getString(R.string.addNoteAddANoteLabel))
            .assertIsDisplayed()
    }

    fun assertUserCommentDisplayed(comment: String) {
        composeTestRule.onNode(hasText(comment))
            .assertExists()
    }

    fun clickSaveButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteSaveButtonLabel))
            .performClick()
    }

    fun clickCancelButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteCancelButtonLabel))
            .performClick()
    }

    fun clickDeleteButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteLabel))
            .performScrollTo()
            .performClick()
    }

    fun clickDeleteConfirmationButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteConfirmationDeleteLabel))
            .performClick()
    }

    fun clickDeleteCancelButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteConfirmationCancelLabel))
            .performClick()
    }

    fun clickExitConfirmationButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteExitConfirmationExitButtonLabel))
            .performClick()
    }

    fun clickExitCancelButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteExitConfirmationCancelButtonLabel))
            .performClick()
    }

    fun enterUserComment(comment: String) {
        composeTestRule.onNodeWithText(context.getString(R.string.addNoteAddANoteLabel))
            .performClick()
            .performTextInput(comment)
    }

    fun clearUserComment() {
        composeTestRule.onNode(hasText(context.getString(R.string.addNoteAddANoteLabel)).not())
            .performClick()
            .performTextClearance()
    }

    fun selectType(type: String) {
        composeTestRule.onNodeWithText(type)
            .performClick()
    }

    fun selectNoteType(type: String) {
        selectType(type)
    }

    fun clearAndEnterUserComment(comment: String) {
        clearUserComment()
        enterUserComment(comment)
    }

    fun clickDeleteConfirmButton() {
        clickDeleteConfirmationButton()
    }

    fun waitForIdle() {
        composeTestRule.waitForIdle()
    }

    fun clickAddNoteButton() {
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteSaveButtonLabel))
            .performClick()
    }

    fun clickNote(noteText: String) {
        composeTestRule.onNodeWithText(noteText, substring = true)
            .performClick()
    }

    fun assertNoteDisplayed(highlightText: String, noteText: String? = null) {
        composeTestRule.onNodeWithText(highlightText, substring = true)
            .assertIsDisplayed()

        noteText?.let {
            composeTestRule.onNodeWithText(noteText, substring = true)
                .assertIsDisplayed()
        }
    }

    fun assertNoteNotDisplayed(noteText: String) {
        composeTestRule.onNode(hasText(noteText, substring = true))
            .assertDoesNotExist()
    }

    fun clearNoteText() {
        composeTestRule.onNode(
            hasText(context.getString(R.string.addNoteAddANoteLabel)).not()
        )
            .performClick()
            .performTextClearance()
    }
}
