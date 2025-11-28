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
package com.instructure.horizon.ui.features.notebook

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.addedit.AddEditNoteScreen
import com.instructure.horizon.features.notebook.addedit.AddEditNoteUiState
import com.instructure.horizon.features.notebook.common.model.NotebookType
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddEditNoteScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testAddNoteScreenDisplaysCorrectTitle() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState()
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.createNoteTitle))
            .assertIsDisplayed()
    }

    @Test
    fun testEditNoteScreenDisplaysCorrectTitle() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createEditNoteState()
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.editNoteTitle))
            .assertIsDisplayed()
    }

    @Test
    fun testSaveButtonIsDisabledWhenNoContentChange() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(hasContentChange = false)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.editNoteSaveButtonLabel))
            .assertIsNotEnabled()
    }

    @Test
    fun testSaveButtonIsEnabledWhenContentChanged() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(hasContentChange = true)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.editNoteSaveButtonLabel))
            .assertIsEnabled()
    }

    @Test
    fun testCancelButtonIsDisplayed() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState()
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.editNoteCancelButtonLabel))
            .assertIsDisplayed()
    }

    @Test
    fun testHighlightedTextIsDisplayed() {
        val highlightedText = "This is highlighted text for testing"
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(highlightedText = highlightedText)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(highlightedText)
            .assertIsDisplayed()
    }

    @Test
    fun testTextAreaIsDisplayed() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState()
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.addNoteAddANoteLabel))
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteButtonIsDisplayedForEditMode() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createEditNoteState()
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteLabel))
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteButtonIsNotDisplayedForAddMode() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState()
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNode(hasText(context.getString(R.string.deleteNoteLabel)))
            .assertDoesNotExist()
    }

    @Test
    fun testLastModifiedDateIsDisplayedForEditMode() {
        val lastModifiedDate = "Updated 2 hours ago"
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createEditNoteState(lastModifiedDate = lastModifiedDate)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(lastModifiedDate)
            .assertIsDisplayed()
    }

    @Test
    fun testLoadingStateDisplaysSpinner() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(isLoading = true)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteConfirmationDialogDisplaysWhenTriggered() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createEditNoteState(showDeleteConfirmationDialog = true)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.deleteNoteConfirmationMessage))
            .assertIsDisplayed()
    }

    @Test
    fun testExitConfirmationDialogDisplaysWhenTriggered() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(
                hasContentChange = true,
                showExitConfirmationDialog = true
            )
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.editNoteExitConfirmationTitle))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.editNoteExitConfirmationMessage))
            .assertIsDisplayed()
    }

    @Test
    fun testTypeSelectionDisplaysImportantOption() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(type = NotebookType.Important)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText("Important", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testTypeSelectionDisplaysConfusingOption() {
        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            val state = createAddNoteState(type = NotebookType.Confusing)
            AddEditNoteScreen(navController, state) { _, _ -> }
        }

        composeTestRule.onNodeWithText("Unclear", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun createAddNoteState(
        highlightedText: String = "Test highlighted text",
        userComment: String = "",
        type: NotebookType = NotebookType.Important,
        hasContentChange: Boolean = false,
        isLoading: Boolean = false,
        showDeleteConfirmationDialog: Boolean = false,
        showExitConfirmationDialog: Boolean = false
    ): AddEditNoteUiState {
        return AddEditNoteUiState(
            title = context.getString(R.string.createNoteTitle),
            type = type,
            highlightedData = NoteHighlightedData(
                selectedText = highlightedText,
                range = NoteHighlightedDataRange(0, 10, "", ""),
                textPosition = NoteHighlightedDataTextPosition(0, 10)
            ),
            userComment = TextFieldValue(userComment),
            onUserCommentChanged = {},
            onTypeChanged = {},
            onSaveNote = {},
            onSnackbarDismiss = {},
            hasContentChange = hasContentChange,
            isLoading = isLoading,
            showDeleteConfirmationDialog = showDeleteConfirmationDialog,
            showExitConfirmationDialog = showExitConfirmationDialog
        )
    }

    private fun createEditNoteState(
        highlightedText: String = "Test highlighted text",
        userComment: String = "Existing comment",
        type: NotebookType = NotebookType.Important,
        hasContentChange: Boolean = false,
        isLoading: Boolean = false,
        lastModifiedDate: String? = "Updated 2 hours ago",
        showDeleteConfirmationDialog: Boolean = false,
        showExitConfirmationDialog: Boolean = false
    ): AddEditNoteUiState {
        return AddEditNoteUiState(
            title = context.getString(R.string.editNoteTitle),
            type = type,
            highlightedData = NoteHighlightedData(
                selectedText = highlightedText,
                range = NoteHighlightedDataRange(0, 10, "", ""),
                textPosition = NoteHighlightedDataTextPosition(0, 10)
            ),
            userComment = TextFieldValue(userComment),
            onUserCommentChanged = {},
            onTypeChanged = {},
            onSaveNote = {},
            onSnackbarDismiss = {},
            onDeleteNote = {},
            hasContentChange = hasContentChange,
            isLoading = isLoading,
            lastModifiedDate = lastModifiedDate,
            showDeleteConfirmationDialog = showDeleteConfirmationDialog,
            showExitConfirmationDialog = showExitConfirmationDialog
        )
    }
}
