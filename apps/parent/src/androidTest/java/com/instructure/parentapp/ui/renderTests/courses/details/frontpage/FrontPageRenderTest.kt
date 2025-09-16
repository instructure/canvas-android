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

package com.instructure.parentapp.ui.renderTests.courses.details.frontpage

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.parentapp.features.courses.details.frontpage.FrontPageContent
import com.instructure.parentapp.features.courses.details.frontpage.FrontPageUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FrontPageRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertLoadingContent() {
        composeTestRule.setContent {
            FrontPageContent(
                uiState = FrontPageUiState(
                    isLoading = true
                ),
                actionHandler = {},
                applyOnWebView = {},
                onLtiButtonPressed = {}
            )
        }

        composeTestRule.onNodeWithTag("loading")
            .assertIsDisplayed()
    }

    @Test
    fun assertErrorContent() {
        composeTestRule.setContent {
            FrontPageContent(
                uiState = FrontPageUiState(
                    isLoading = false,
                    isError = true
                ),
                actionHandler = {},
                applyOnWebView = {},
                onLtiButtonPressed = {}
            )
        }

        composeTestRule.onNodeWithText("An unexpected error occurred.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertCourseDetailsContent() {
        composeTestRule.setContent {
            FrontPageContent(
                uiState = FrontPageUiState(
                    isLoading = false,
                    isError = false,
                    htmlContent = "Front page content"
                ),
                actionHandler = {},
                applyOnWebView = {},
                onLtiButtonPressed = {}
            )
        }

        composeTestRule.onNodeWithTag("CourseDetailsWebViewScreen")
            .assertIsDisplayed()
    }
}
