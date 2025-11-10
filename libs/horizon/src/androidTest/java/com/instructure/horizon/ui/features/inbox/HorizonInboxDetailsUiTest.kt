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
package com.instructure.horizon.ui.features.inbox

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.inbox.details.HorizonInboxDetailsItem
import com.instructure.horizon.features.inbox.details.HorizonInboxDetailsScreen
import com.instructure.horizon.features.inbox.details.HorizonInboxDetailsUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class HorizonInboxDetailsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDetailsScreenDisplaysTitle() {
        val uiState = HorizonInboxDetailsUiState(
            title = "Test Conversation",
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            HorizonInboxDetailsScreen(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Test Conversation")
            .assertIsDisplayed()
    }

    @Test
    fun testDetailsScreenDisplaysMessages() {
        val uiState = HorizonInboxDetailsUiState(
            title = "Conversation",
            items = listOf(
                HorizonInboxDetailsItem(
                    author = "John Doe",
                    date = Date(),
                    isHtmlContent = false,
                    content = "Test message content",
                    attachments = emptyList()
                )
            ),
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            HorizonInboxDetailsScreen(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("John Doe")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Test message content")
            .assertIsDisplayed()
    }

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val uiState = HorizonInboxDetailsUiState(
            title = "Loading...",
            loadingState = LoadingState(isLoading = true)
        )

        composeTestRule.setContent {
            HorizonInboxDetailsScreen(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testMultipleMessagesDisplay() {
        val uiState = HorizonInboxDetailsUiState(
            title = "Conversation",
            items = listOf(
                HorizonInboxDetailsItem(
                    author = "Student 1",
                    date = Date(),
                    isHtmlContent = false,
                    content = "First message",
                    attachments = emptyList()
                ),
                HorizonInboxDetailsItem(
                    author = "Teacher",
                    date = Date(),
                    isHtmlContent = false,
                    content = "Second message",
                    attachments = emptyList()
                ),
                HorizonInboxDetailsItem(
                    author = "Student 1",
                    date = Date(),
                    isHtmlContent = false,
                    content = "Third message",
                    attachments = emptyList()
                )
            ),
            loadingState = LoadingState(isLoading = false)
        )

        composeTestRule.setContent {
            HorizonInboxDetailsScreen(uiState, rememberNavController())
        }

        composeTestRule.onNodeWithText("First message")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Second message")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Third message")
            .assertIsDisplayed()
    }
}
