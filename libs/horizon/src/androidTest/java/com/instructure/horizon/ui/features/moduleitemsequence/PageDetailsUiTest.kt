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
package com.instructure.horizon.ui.features.moduleitemsequence

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.NoteObjectType
import com.instructure.horizon.features.moduleitemsequence.content.page.PageDetailsContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.page.PageDetailsUiState
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class PageDetailsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPageContentDisplays() {
        val uiState = PageDetailsUiState(
            pageHtmlContent = "<p>Test page content</p>",
            loadingState = LoadingState(isLoading = false),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("PageContent")
            .assertIsDisplayed()
    }

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val uiState = PageDetailsUiState(
            loadingState = LoadingState(isLoading = true),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testErrorStateDisplaysMessage() {
        val uiState = PageDetailsUiState(
            loadingState = LoadingState(isLoading = false, isError = true),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Failed to load page")
            .assertIsDisplayed()
    }

    @Test
    fun testNotesDisplay() {
        val uiState = PageDetailsUiState(
            pageHtmlContent = "<p>Content</p>",
            notes = listOf(
                Note(
                    id = "1",
                    objectId = "1",
                    objectType = NoteObjectType.PAGE,
                    userText = "comment 1",
                    highlightedText = NoteHighlightedData(
                        selectedText = "highlighted text 1",
                        range = NoteHighlightedDataRange(1, 5, "start", "end"),
                        textPosition = NoteHighlightedDataTextPosition(1, 5)
                    ),
                    type = NotebookType.Important,
                    updatedAt = Date(),
                    courseId = 1,
                ),
                Note(
                    id = "2",
                    objectId = "1",
                    objectType = NoteObjectType.PAGE,
                    userText = "comment 2",
                    highlightedText = NoteHighlightedData(
                        selectedText = "highlighted text 2",
                        range = NoteHighlightedDataRange(10, 15, "start", "end"),
                        textPosition = NoteHighlightedDataTextPosition(10, 15)
                    ),
                    type = NotebookType.Confusing,
                    updatedAt = Date(),
                    courseId = 1,
                )
            ),
            loadingState = LoadingState(isLoading = false),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Notes")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("2 notes")
            .assertIsDisplayed()
    }

    @Test
    fun testAddNoteButtonDisplays() {
        val uiState = PageDetailsUiState(
            pageHtmlContent = "<p>Content</p>",
            loadingState = LoadingState(isLoading = false),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithContentDescription("Add note")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testEmptyPageContentDisplaysMessage() {
        val uiState = PageDetailsUiState(
            pageHtmlContent = "",
            loadingState = LoadingState(isLoading = false),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("No content available")
            .assertIsDisplayed()
    }

    @Test
    fun testPageWithNoNotes() {
        val uiState = PageDetailsUiState(
            pageHtmlContent = "<p>Content</p>",
            notes = emptyList(),
            loadingState = LoadingState(isLoading = false),
            courseId = 1L
        )

        composeTestRule.setContent {
            PageDetailsContentScreen(
                uiState,
                rememberScrollState(),
                { _, _ ->},
                rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("No notes")
            .assertIsDisplayed()
    }
}
