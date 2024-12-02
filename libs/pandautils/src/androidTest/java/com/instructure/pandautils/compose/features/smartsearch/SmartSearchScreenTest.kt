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
 */
package com.instructure.pandautils.compose.features.smartsearch

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.composeTest.hasSiblingWithText
import com.instructure.pandautils.features.smartsearch.SmartSearchResultUiState
import com.instructure.pandautils.features.smartsearch.SmartSearchScreen
import com.instructure.pandautils.features.smartsearch.SmartSearchUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmartSearchScreenTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun testSmartSearchQuery() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = emptyList(),
                    actionHandler = {},
                    loading = false,
                    error = false
                )
            ) { }
        }

        composeTestRule.onNode(
            hasParent(hasTestTag("searchBar")).and(hasTestTag("searchField")),
            useUnmergedTree = true
        )
            .assertExists()
            .assertTextEquals("Test")
    }

    @Test
    fun testCourseName() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = emptyList(),
                    actionHandler = {},
                    loading = false,
                    error = false
                )
            ) { }
        }

        composeTestRule.onNodeWithTag("courseTitle")
            .assertExists()
            .assertTextEquals("Test course")
    }

    @Test
    fun testLoading() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = emptyList(),
                    actionHandler = {},
                    loading = true,
                    error = false
                )
            ) { }
        }

        composeTestRule.onNodeWithTag("loading")
            .assertIsDisplayed()
    }

    @Test
    fun testError() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = emptyList(),
                    actionHandler = {},
                    loading = false,
                    error = true
                )
            ) { }
        }

        composeTestRule.onNodeWithTag("error")
            .assertIsDisplayed()
    }

    @Test
    fun testEmpty() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = emptyList(),
                    actionHandler = {},
                    loading = false,
                    error = false
                )
            ) { }
        }

        composeTestRule.onNodeWithTag("empty")
            .assertIsDisplayed()
    }

    @Test
    fun testEmptyResultBodyIsHidden() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = listOf(
                        SmartSearchResultUiState(
                            title = "Test title",
                            body = "",
                            relevance = 50,
                            type = SmartSearchContentType.ASSIGNMENT,
                            url = "https://example.com"
                        )
                    ),
                    actionHandler = {},
                    loading = false,
                    error = false
                )
            ) { }
        }

        composeTestRule.onNode(
            hasTestTag("resultBody").and(
                hasParent(
                    hasTestTag("resultItem").and(
                        hasSiblingWithText("Test title")
                    )
                )
            ), useUnmergedTree = true
        )
            .assertDoesNotExist()
    }

    @Test
    fun testResultItem() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = listOf(
                        SmartSearchResultUiState(
                            title = "Test title",
                            body = "Test body",
                            relevance = 50,
                            type = SmartSearchContentType.ASSIGNMENT,
                            url = "https://example.com"
                        )
                    ),
                    actionHandler = {},
                    loading = false,
                    error = false
                )
            ) { }
        }

        composeTestRule.onNode(
            hasTestTag("resultItem")
                .and(
                    hasAnyChild(hasTestTag("resultBody").and(hasText("Test body")))
                )
                .and(
                    hasAnyChild(hasTestTag("resultTitle").and(hasText("Test title")))
                )
                .and(
                    hasAnyChild(hasTestTag("resultType").and(hasText("Assignment")))
                ),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testRelevanceDotCount() {
        composeTestRule.setContent {
            SmartSearchScreen(
                uiState = SmartSearchUiState(
                    query = "Test",
                    canvasContext = Course(name = "Test course"),
                    results = listOf(
                        SmartSearchResultUiState(
                            title = "Test title",
                            body = "Test body",
                            relevance = 75,
                            type = SmartSearchContentType.ASSIGNMENT,
                            url = "https://example.com"
                        ),
                        SmartSearchResultUiState(
                            title = "Test title 2",
                            body = "Test body 2",
                            relevance = 50,
                            type = SmartSearchContentType.ASSIGNMENT,
                            url = "https://example.com"
                        )
                    ),
                    actionHandler = {},
                    loading = false,
                    error = false
                )
            ) { }
        }

        composeTestRule.onAllNodes(
            hasTestTag("relevanceDot filled").and(hasSiblingWithText("Test title")),
            useUnmergedTree = true
        ).assertCountEquals(4)

        composeTestRule.onAllNodes(
            hasTestTag("relevanceDot empty").and(hasSiblingWithText("Test title")),
            useUnmergedTree = true
        ).assertCountEquals(0)

        composeTestRule.onAllNodes(
            hasTestTag("relevanceDot filled").and(hasSiblingWithText("Test title 2")),
            useUnmergedTree = true
        ).assertCountEquals(3)

        composeTestRule.onAllNodes(
            hasTestTag("relevanceDot empty").and(hasSiblingWithText("Test title 2")),
            useUnmergedTree = true
        ).assertCountEquals(1)

    }
}