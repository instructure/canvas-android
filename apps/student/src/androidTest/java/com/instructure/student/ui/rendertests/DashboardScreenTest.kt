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
 *
 */

package com.instructure.student.ui.rendertests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.dashboard.DashboardNavigationHandler
import com.instructure.pandautils.features.dashboard.compose.DashboardUiState
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.student.features.dashboard.compose.DashboardScreenContent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavigationHandler = object : DashboardNavigationHandler {
        override fun handleCoursesNavigation(event: DashboardNavigationEvent.Courses) {}
        override fun handleTodoNavigation(event: DashboardNavigationEvent.Todo) {}
        override fun handleForecastNavigation(event: DashboardNavigationEvent.Forecast) {}
        override fun handleProgressNavigation(event: DashboardNavigationEvent.Progress) {}
        override fun handleConferencesNavigation(event: DashboardNavigationEvent.Conferences) {}
        override fun handleDashboardNavigation(event: DashboardNavigationEvent.Dashboard) {}
    }

    @Test
    fun testDashboardScreenShowsLoadingState() {
        val mockUiState = DashboardUiState(
            loading = true,
            error = null,
            refreshing = false,
            onRefresh = {},
            onRetry = {}
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState = mockUiState,
                refreshSignal = MutableSharedFlow(),
                snackbarMessageFlow = MutableSharedFlow(),
                onShowSnackbar = { _, _, _ -> },
                navigationHandler = mockNavigationHandler
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
    }

    @Test
    fun testDashboardScreenShowsErrorState() {
        val mockUiState = DashboardUiState(
            loading = false,
            error = "An error occurred",
            refreshing = false,
            onRefresh = {},
            onRetry = {}
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState = mockUiState,
                refreshSignal = MutableSharedFlow(),
                snackbarMessageFlow = MutableSharedFlow(),
                onShowSnackbar = { _, _, _ -> },
                navigationHandler = mockNavigationHandler
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorContent").assertIsDisplayed()
    }

    @Test
    fun testDashboardScreenShowsEmptyState() {
        val mockUiState = DashboardUiState(
            loading = false,
            error = null,
            refreshing = false,
            onRefresh = {},
            onRetry = {}
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState = mockUiState,
                refreshSignal = MutableSharedFlow(),
                snackbarMessageFlow = MutableSharedFlow(),
                onShowSnackbar = { _, _, _ -> },
                navigationHandler = mockNavigationHandler
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("emptyContent").assertIsDisplayed()
    }

    @Test
    fun testDashboardScreenShowsRefreshIndicator() {
        val mockUiState = DashboardUiState(
            loading = false,
            error = null,
            refreshing = true,
            onRefresh = {},
            onRetry = {}
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState = mockUiState,
                refreshSignal = MutableSharedFlow(),
                snackbarMessageFlow = MutableSharedFlow(),
                onShowSnackbar = { _, _, _ -> },
                navigationHandler = mockNavigationHandler
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboardPullRefreshIndicator").assertIsDisplayed()
    }

    @Test
    fun testCustomizeDashboardButtonIsDisplayed() {
        val mockWidgets = listOf(
            WidgetMetadata("widget1", 0, true),
            WidgetMetadata("widget2", 1, true)
        )
        val mockUiState = DashboardUiState(
            loading = false,
            error = null,
            refreshing = false,
            widgets = mockWidgets,
            onRefresh = {},
            onRetry = {}
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState = mockUiState,
                refreshSignal = MutableSharedFlow(),
                snackbarMessageFlow = MutableSharedFlow(),
                onShowSnackbar = { _, _, _ -> },
                navigationHandler = mockNavigationHandler
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Customize Dashboard").assertIsDisplayed()
    }

    @Test
    fun testCustomizeDashboardButtonCallsNavigationHandler() {
        var navigationCalled = false
        val testNavigationHandler = object : DashboardNavigationHandler {
            override fun handleCoursesNavigation(event: DashboardNavigationEvent.Courses) {}
            override fun handleTodoNavigation(event: DashboardNavigationEvent.Todo) {}
            override fun handleForecastNavigation(event: DashboardNavigationEvent.Forecast) {}
            override fun handleProgressNavigation(event: DashboardNavigationEvent.Progress) {}
            override fun handleConferencesNavigation(event: DashboardNavigationEvent.Conferences) {}
            override fun handleDashboardNavigation(event: DashboardNavigationEvent.Dashboard) {
                if (event is DashboardNavigationEvent.Dashboard.NavigateToCustomizeDashboard) {
                    navigationCalled = true
                }
            }
        }

        val mockWidgets = listOf(
            WidgetMetadata("widget1", 0, true)
        )
        val mockUiState = DashboardUiState(
            loading = false,
            error = null,
            refreshing = false,
            widgets = mockWidgets,
            onRefresh = {},
            onRetry = {}
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                uiState = mockUiState,
                refreshSignal = MutableSharedFlow(),
                snackbarMessageFlow = MutableSharedFlow(),
                onShowSnackbar = { _, _, _ -> },
                navigationHandler = testNavigationHandler
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Customize Dashboard").performClick()

        assertTrue("NavigationHandler's handleDashboardNavigation should be called", navigationCalled)
    }
}
