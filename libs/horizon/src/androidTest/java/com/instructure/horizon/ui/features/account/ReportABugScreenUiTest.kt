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
package com.instructure.horizon.ui.features.account

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.account.reportabug.ReportABugScreen
import com.instructure.horizon.features.account.reportabug.ReportABugUiState
import com.instructure.horizon.ui.features.account.pages.ReportABugPage
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReportABugScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val page by lazy { ReportABugPage(composeTestRule) }

    @Test
    fun testScreenDisplaysWithTitle() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertScreenDisplayed()
    }

    @Test
    fun testScreenDisplaysDescriptionMessage() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertDescriptionMessageDisplayed()
    }

    @Test
    fun testScreenDisplaysCloseButton() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertCloseButtonDisplayed()
    }

    @Test
    fun testScreenDisplaysTopicField() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertTopicFieldDisplayed()
    }

    @Test
    fun testScreenDisplaysSubjectField() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertSubjectFieldDisplayed()
    }

    @Test
    fun testScreenDisplaysDescriptionField() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertDescriptionFieldDisplayed()
    }

    @Test
    fun testScreenDisplaysCancelButton() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertCancelButtonDisplayed()
    }

    @Test
    fun testScreenDisplaysSubmitButton() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertSubmitButtonDisplayed()
    }

    @Test
    fun testSubjectFieldShowsRequiredIndicator() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertSubjectRequiredIndicator()
    }

    @Test
    fun testDescriptionFieldShowsRequiredIndicator() {
        val uiState = ReportABugUiState()

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertDescriptionRequiredIndicator()
    }

    @Test
    fun testSelectedTopicDisplays() {
        val uiState = ReportABugUiState(
            selectedTopic = "Minor issue"
        )

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertSelectedTopicDisplayed("Minor issue")
    }

    @Test
    fun testTopicErrorDisplays() {
        val uiState = ReportABugUiState(
            topicError = "Topic is required"
        )

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertTopicError("Topic is required")
    }

    @Test
    fun testSubjectErrorDisplays() {
        val uiState = ReportABugUiState(
            subjectError = "Subject is required"
        )

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertSubjectError("Subject is required")
    }

    @Test
    fun testDescriptionErrorDisplays() {
        val uiState = ReportABugUiState(
            descriptionError = "Description is required"
        )

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertDescriptionError("Description is required")
    }

    @Test
    fun testNotLoadingStateEnablesSubmitButton() {
        val uiState = ReportABugUiState(
            isLoading = false
        )

        composeTestRule.setContent {
            ReportABugScreen(uiState, rememberNavController())
        }

        page.assertNotLoading()
    }

}
