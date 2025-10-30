package com.instructure.horizon.ui.features.dashboard.widget.skillhighlights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.skillhighlights.DashboardSkillHighlightsSection
import com.instructure.horizon.features.dashboard.widget.skillhighlights.DashboardSkillHighlightsUiState
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.DashboardSkillHighlightsCardState
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.SkillHighlight
import com.instructure.horizon.features.dashboard.widget.skillhighlights.card.SkillHighlightProficiencyLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardSkillHighlightsWidgetUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testLoadingStateDisplaysCorrectly() {
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.LOADING
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillHighlightsTitle)
        composeTestRule.onNodeWithText(title, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testErrorStateDisplaysCorrectly() {
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { it() }
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillHighlightsTitle)
        val errorMessage = context.getString(R.string.dashboardWidgetCardErrorMessage)
        val retryLabel = context.getString(R.string.dashboardSkillHighlightsRetry)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(retryLabel, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testErrorStateRefreshButtonWorks() {
        var refreshCalled = false
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { refreshCalled = true; it() }
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        val retryLabel = context.getString(R.string.dashboardSkillHighlightsRetry)
        composeTestRule.onNodeWithText(retryLabel, useUnmergedTree = true).performClick()

        assert(refreshCalled)
    }

    @Test
    fun testNoDataStateDisplaysCorrectly() {
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillHighlightsCardState(skills = emptyList())
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillHighlightsTitle)
        val noDataTitle = context.getString(R.string.dashboardSkillHighlightsNoDataTitle)
        val noDataMessage = context.getString(R.string.dashboardSkillHighlightsNoDataMessage)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(noDataTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(noDataMessage).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithSkillsDisplaysCorrectly() {
        val skills = listOf(
            SkillHighlight("Advanced JavaScript", SkillHighlightProficiencyLevel.ADVANCED),
            SkillHighlight("Python Programming", SkillHighlightProficiencyLevel.PROFICIENT),
            SkillHighlight("Data Analysis", SkillHighlightProficiencyLevel.BEGINNER)
        )
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillHighlightsCardState(skills = skills)
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        val title = context.getString(R.string.dashboardSkillHighlightsTitle)
        val advancedLabel = context.getString(R.string.dashboardSkillProficienyLevelAdvanced)
        val proficientLabel = context.getString(R.string.dashboardSkillProficienyLevelProficient)
        val beginnerLabel = context.getString(R.string.dashboardSkillProficienyLevelBeginner)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced JavaScript").assertIsDisplayed()
        composeTestRule.onNodeWithText("Python Programming").assertIsDisplayed()
        composeTestRule.onNodeWithText("Data Analysis").assertIsDisplayed()
        composeTestRule.onNodeWithText(advancedLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(proficientLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(beginnerLabel).assertIsDisplayed()
    }

    @Test
    fun testAllProficiencyLevelsDisplay() {
        val skills = listOf(
            SkillHighlight("Expert Skill", SkillHighlightProficiencyLevel.EXPERT),
            SkillHighlight("Advanced Skill", SkillHighlightProficiencyLevel.ADVANCED),
            SkillHighlight("Proficient Skill", SkillHighlightProficiencyLevel.PROFICIENT)
        )
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillHighlightsCardState(skills = skills)
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        val expertLabel = context.getString(R.string.dashboardSkillProficienyLevelExpert)
        val advancedLabel = context.getString(R.string.dashboardSkillProficienyLevelAdvanced)
        val proficientLabel = context.getString(R.string.dashboardSkillProficienyLevelProficient)

        composeTestRule.onNodeWithText(expertLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(advancedLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(proficientLabel).assertIsDisplayed()
    }

    @Test
    fun testLongSkillNameIsDisplayed() {
        val skills = listOf(
            SkillHighlight(
                "This is a very long skill name that should be displayed correctly in the UI",
                SkillHighlightProficiencyLevel.ADVANCED
            ),
            SkillHighlight("Short Skill", SkillHighlightProficiencyLevel.PROFICIENT),
            SkillHighlight("Medium Length Skill Name", SkillHighlightProficiencyLevel.BEGINNER)
        )
        val uiState = DashboardSkillHighlightsUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardSkillHighlightsCardState(skills = skills)
        )

        composeTestRule.setContent {
            DashboardSkillHighlightsSection(uiState, rememberNavController())
        }

        // Text will be truncated but should still be findable by partial match
        composeTestRule.onNodeWithText(
            "This is a very long skill name that should be displayed correctly in the UI",
            substring = true
        ).assertIsDisplayed()
    }
}