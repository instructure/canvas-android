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
package com.instructure.pandautils.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.SearchBar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExpandSearchBar() {
        composeTestRule.setContent {
            SearchBar(
                icon = R.drawable.ic_smart_search,
                tintColor = Color.Black,
                placeholder = "Search",
                onExpand = { expanded -> },
                onSearch = { query -> }
            )
        }
        val searchField = composeTestRule.onNodeWithTag("searchField")
        val closeButton = composeTestRule.onNodeWithTag("closeButton")
        val clearButton = composeTestRule.onNodeWithTag("clearButton")
        val searchButton = composeTestRule.onNodeWithTag("searchButton")

        searchField
            .assertIsNotDisplayed()
        closeButton
            .assertIsNotDisplayed()
        clearButton
            .assertIsNotDisplayed()

        searchButton
            .assertIsDisplayed()
            .performClick()

        searchField
            .assertIsDisplayed()
        closeButton
            .assertIsDisplayed()
        clearButton
            .assertIsNotDisplayed()
        searchButton
            .assertIsNotDisplayed()
    }

    @Test
    fun testCollapseSearchBar() {
        composeTestRule.setContent {
            SearchBar(
                icon = R.drawable.ic_smart_search,
                tintColor = Color.Black,
                placeholder = "Search",
                onExpand = { expanded -> },
                onSearch = { query -> }
            )
        }
        val searchField = composeTestRule.onNodeWithTag("searchField")
        val closeButton = composeTestRule.onNodeWithTag("closeButton")
        val clearButton = composeTestRule.onNodeWithTag("clearButton")
        val searchButton = composeTestRule.onNodeWithTag("searchButton")

        searchButton
            .assertIsDisplayed()
            .performClick()

        searchField
            .assertIsDisplayed()
        closeButton
            .assertIsDisplayed()
        clearButton
            .assertIsNotDisplayed()
        searchButton
            .assertIsNotDisplayed()

        closeButton
            .performClick()

        searchField
            .assertIsNotDisplayed()
        closeButton
            .assertIsNotDisplayed()
        clearButton
            .assertIsNotDisplayed()
        searchButton
            .assertIsDisplayed()
    }

    @Test
    fun testClearButtonOnlyVisibleIfQueryPresent() {
        composeTestRule.setContent {
            SearchBar(
                icon = R.drawable.ic_smart_search,
                tintColor = Color.Black,
                placeholder = "Search",
                onExpand = { expanded -> },
                onSearch = { query -> }
            )
        }
        val searchField = composeTestRule.onNodeWithTag("searchField")
        val clearButton = composeTestRule.onNodeWithTag("clearButton")
        val searchButton = composeTestRule.onNodeWithTag("searchButton")

        searchButton
            .assertIsDisplayed()
            .performClick()

        clearButton
            .assertIsNotDisplayed()

        searchField
            .performTextInput("Query")

        clearButton
            .assertIsDisplayed()
    }

    @Test
    fun clearButtonClearsQuery() {
        composeTestRule.setContent {
            SearchBar(
                icon = R.drawable.ic_smart_search,
                tintColor = Color.Black,
                placeholder = "Search",
                onExpand = { expanded -> },
                onSearch = { query -> }
            )
        }
        val searchField = composeTestRule.onNodeWithTag("searchField")
        val clearButton = composeTestRule.onNodeWithTag("clearButton")
        val searchButton = composeTestRule.onNodeWithTag("searchButton")

        searchButton
            .assertIsDisplayed()
            .performClick()

        searchField
            .performTextInput("Query")

        clearButton
            .assertIsDisplayed()
            .performClick()

        searchField
            .assertIsDisplayed()
            .assert(hasText(""))
    }

    @Test
    fun testQueryRemainsAfterCollapsingSearchBar() {
        composeTestRule.setContent {
            SearchBar(
                icon = R.drawable.ic_smart_search,
                tintColor = Color.Black,
                placeholder = "Search",
                onExpand = { expanded -> },
                onSearch = { query -> }
            )
        }
        val searchField = composeTestRule.onNodeWithTag("searchField")
        val searchButton = composeTestRule.onNodeWithTag("searchButton")
        val closeButton = composeTestRule.onNodeWithTag("closeButton")

        searchButton
            .assertIsDisplayed()
            .performClick()

        searchField
            .performTextInput("Query")

        closeButton
            .assertIsDisplayed()
            .performClick()

        searchButton
            .assertIsDisplayed()
            .performClick()

        searchField.assert(hasText("Query"))
    }
}