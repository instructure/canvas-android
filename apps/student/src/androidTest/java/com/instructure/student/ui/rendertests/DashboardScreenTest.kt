package com.instructure.student.ui.rendertests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.student.features.dashboard.compose.DashboardScreenContent
import com.instructure.student.features.dashboard.compose.DashboardUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
            DashboardScreenContent(uiState = mockUiState)
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
            DashboardScreenContent(uiState = mockUiState)
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
            DashboardScreenContent(uiState = mockUiState)
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
            DashboardScreenContent(uiState = mockUiState)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboardPullRefreshIndicator").assertIsDisplayed()
    }
}