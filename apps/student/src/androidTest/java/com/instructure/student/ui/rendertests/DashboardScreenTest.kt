package com.instructure.student.ui.rendertests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.student.features.dashboard.compose.DashboardScreenContent
import com.instructure.student.features.dashboard.compose.DashboardUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRouter = object : DashboardRouter {
        override fun routeToGlobalAnnouncement(subject: String, message: String) {}
        override fun routeToSubmissionDetails(canvasContext: CanvasContext, assignmentId: Long, attemptId: Long) {}
        override fun routeToMyFiles(canvasContext: CanvasContext, folderId: Long) {}
        override fun routeToSyncProgress() {}
        override fun routeToManageOfflineContent() {}
        override fun routeToCustomizeDashboard() {}
        override fun restartApp() {}
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
                router = mockRouter
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
                router = mockRouter
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
                router = mockRouter
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
                router = mockRouter
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
                router = mockRouter
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Customize Dashboard").assertIsDisplayed()
    }

    @Test
    fun testCustomizeDashboardButtonCallsRouter() {
        var routerCalled = false
        val testRouter = object : DashboardRouter {
            override fun routeToGlobalAnnouncement(subject: String, message: String) {}
            override fun routeToSubmissionDetails(canvasContext: CanvasContext, assignmentId: Long, attemptId: Long) {}
            override fun routeToMyFiles(canvasContext: CanvasContext, folderId: Long) {}
            override fun routeToSyncProgress() {}
            override fun routeToManageOfflineContent() {}
            override fun routeToCustomizeDashboard() {
                routerCalled = true
            }
            override fun restartApp() {}
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
                router = testRouter
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Customize Dashboard").performClick()

        assertTrue("Router's routeToCustomizeDashboard should be called", routerCalled)
    }
}