/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.features.pages.details

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.features.pages.details.PageDetailsActions
import com.instructure.pandautils.features.pages.details.PageDetailsScreenState
import com.instructure.pandautils.features.pages.details.PageDetailsUiState
import com.instructure.pandautils.features.pages.details.composables.PageDetailsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PageDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun inToolbar(text: String) = hasText(text) and hasAnyAncestor(hasTestTag("toolbar"))

    @Test
    fun showsToolbarTitleAndInvokesNavigationOnBackClick() {
        var backClicked = false
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(toolbarTitle = "My Page"),
                toolbarColor = 0,
                navigationActionClick = { backClicked = true },
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks()
            )
        }

        composeTestRule.onNode(inToolbar("My Page")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("navigationButton").assertHasClickAction().performClick()
        assertTrue(backClicked)
    }

    @Test
    fun showsLoadingIndicatorWhenScreenStateIsLoading() {
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(screenState = PageDetailsScreenState.Loading, toolbarTitle = "Page"),
                toolbarColor = 0,
                navigationActionClick = {},
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks()
            )
        }

        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pageDetailsWebView").assertDoesNotExist()
    }

    @Test
    fun showsWebViewWhenScreenStateIsContent() {
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(
                    screenState = PageDetailsScreenState.Content,
                    toolbarTitle = "Page",
                    htmlContent = "<p>Hello</p>"
                ),
                toolbarColor = 0,
                navigationActionClick = {},
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks()
            )
        }

        composeTestRule.onNodeWithTag("pageDetailsWebView").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading").assertDoesNotExist()
    }

    @Test
    fun editButtonIsShownAndInvokesOnEditClickWhenEditingIsAllowed() {
        var editClicked = false
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(
                    toolbarTitle = "Page",
                    editMenuVisible = true,
                    actions = PageDetailsActions(onEditClick = { editClicked = true })
                ),
                toolbarColor = 0,
                navigationActionClick = {},
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks()
            )
        }

        composeTestRule.onNodeWithTag("editPageButton").assertHasClickAction().performClick()
        assertTrue(editClicked)
    }

    @Test
    fun editButtonIsHiddenWhenEditingIsNotAllowed() {
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(toolbarTitle = "Page", editMenuVisible = false),
                toolbarColor = 0,
                navigationActionClick = {},
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks()
            )
        }

        composeTestRule.onNodeWithTag("editPageButton").assertDoesNotExist()
    }

    @Test
    fun bookmarkOverflowMenuIsHiddenWhenOnAddBookmarkIsNotProvided() {
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(toolbarTitle = "Page"),
                toolbarColor = 0,
                navigationActionClick = {},
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(),
                onAddBookmark = null
            )
        }

        composeTestRule.onNodeWithTag("overflowMenu").assertDoesNotExist()
    }

    @Test
    fun bookmarkOverflowMenuInvokesOnAddBookmarkWhenItemIsClicked() {
        var bookmarkAdded = false
        composeTestRule.setContent {
            PageDetailsScreen(
                uiState = PageDetailsUiState(toolbarTitle = "Page"),
                toolbarColor = 0,
                navigationActionClick = {},
                webViewCallbacks = ComposeWebViewCallbacks(),
                embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(),
                onAddBookmark = { bookmarkAdded = true }
            )
        }

        composeTestRule.onNodeWithTag("overflowMenu").assertHasClickAction().performClick()
        composeTestRule.onNodeWithText("Add Bookmark").performClick()

        assertTrue(bookmarkAdded)
    }
}
