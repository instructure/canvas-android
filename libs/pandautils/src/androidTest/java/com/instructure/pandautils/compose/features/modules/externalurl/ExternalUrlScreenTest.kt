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

package com.instructure.pandautils.compose.features.modules.externalurl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.modules.externalurl.ExternalUrlScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExternalUrlScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertTitleAndMessageDisplayed() {
        composeTestRule.setContent {
            ExternalUrlScreen(
                title = "Course Intro Video",
                onNavigationClick = {},
                onOpenClick = {}
            )
        }

        composeTestRule.onNodeWithText("Course Intro Video").assertIsDisplayed()
        // The message scrolls: in landscape the illustration above it is taller than the viewport.
        composeTestRule.onNodeWithText("This page can only be viewed from a web browser.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun assertOpenButtonDisplayedAndClickable() {
        var clicked = false
        composeTestRule.setContent {
            ExternalUrlScreen(
                title = "Course Intro Video",
                onNavigationClick = {},
                onOpenClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Open In Browser").assertIsDisplayed()
        composeTestRule.onNodeWithTag("openInBrowserButton").performClick()

        assertTrue(clicked)
    }

    @Test
    fun assertOpenButtonHiddenWhenThereIsNoUrlToOpen() {
        composeTestRule.setContent {
            ExternalUrlScreen(
                title = "Course Intro Video",
                onNavigationClick = {},
                onOpenClick = null
            )
        }

        composeTestRule.onNodeWithText("This page can only be viewed from a web browser.")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("openInBrowserButton").assertDoesNotExist()
    }
}
