package com.instructure.horizon.ui.features.dashboard.widget.skilloverview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.skilloverview.DashboardSkillOverviewSection
import com.instructure.horizon.features.dashboard.widget.skilloverview.DashboardSkillOverviewUiState
import com.instructure.horizon.features.dashboard.widget.skilloverview.card.DashboardSkillOverviewCardState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardSkillOverviewWidgetUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testLoadingStateDisplaysCorrectly() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.LOADING
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun testErrorStateDisplaysCorrectly() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        val errorMessage = context.getString(R.string.dashboardSkillOverviewErrorMessage)
        val retryLabel = context.getString(R.string.dashboardSkillOverviewRetry)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(retryLabel).assertIsDisplayed()
    }

    @Test
    fun testErrorStateRefreshButtonWorks() {
        var refreshCalled = false
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { refreshCalled = true; it() }
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val retryLabel = context.getString(R.string.dashboardSkillOverviewRetry)
        composeTestRule.onNodeWithText(retryLabel).performClick()

        assert(refreshCalled)
    }

    @Test
    fun testNoDataStateDisplaysCorrectly() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillOverviewCardState(completedSkillCount = 0)
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        val noDataMessage = context.getString(R.string.dashboardSkillOverviewNoDataMessage)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(noDataMessage).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithSkillCountDisplaysCorrectly() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillOverviewCardState(completedSkillCount = 5)
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        val earnedLabel = context.getString(R.string.dashboardSkillOverviewEarnedLabel)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText(earnedLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithSingleSkill() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillOverviewCardState(completedSkillCount = 1)
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        val earnedLabel = context.getString(R.string.dashboardSkillOverviewEarnedLabel)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText(earnedLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithLargeSkillCount() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillOverviewCardState(completedSkillCount = 99)
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        val earnedLabel = context.getString(R.string.dashboardSkillOverviewEarnedLabel)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText("99").assertIsDisplayed()
        composeTestRule.onNodeWithText(earnedLabel).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithVeryLargeSkillCount() {
        val uiState = DashboardSkillOverviewUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillOverviewCardState(completedSkillCount = 999)
        )

        composeTestRule.setContent {
            DashboardSkillOverviewSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillOverviewTitle)
        val earnedLabel = context.getString(R.string.dashboardSkillOverviewEarnedLabel)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText("999").assertIsDisplayed()
        composeTestRule.onNodeWithText(earnedLabel).assertIsDisplayed()
    }

    @Test
    fun testMultipleSkillCounts() {
        val skillCounts = listOf(0, 1, 3, 10, 24, 50, 100)

        skillCounts.forEach { count ->
            val uiState = DashboardSkillOverviewUiState(
                state = DashboardItemState.SUCCESS,
                cardState = DashboardSkillOverviewCardState(completedSkillCount = count)
            )

            composeTestRule.setContent {
                DashboardSkillOverviewSection(uiState, rememberNavController())
            }

            val title = context.getString(R.string.dashboardSkillOverviewTitle)
            composeTestRule.onNodeWithText(title).assertIsDisplayed()

            if (count == 0) {
                val noDataMessage = context.getString(R.string.dashboardSkillOverviewNoDataMessage)
                composeTestRule.onNodeWithText(noDataMessage).assertIsDisplayed()
            } else {
                val earnedLabel = context.getString(R.string.dashboardSkillOverviewEarnedLabel)
                composeTestRule.onNodeWithText(count.toString()).assertIsDisplayed()
                composeTestRule.onNodeWithText(earnedLabel).assertIsDisplayed()
            }
        }
    }
}
