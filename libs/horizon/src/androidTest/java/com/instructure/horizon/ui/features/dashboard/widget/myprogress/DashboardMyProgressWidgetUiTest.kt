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
package com.instructure.horizon.ui.features.dashboard.widget.myprogress

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.myprogress.DashboardMyProgressSection
import com.instructure.horizon.features.dashboard.widget.myprogress.DashboardMyProgressUiState
import com.instructure.horizon.features.dashboard.widget.myprogress.card.DashboardMyProgressCardState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardMyProgressWidgetUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingStateDisplaysShimmerEffect() {
        val state = DashboardMyProgressUiState(
            state = DashboardItemState.LOADING
        )

        composeTestRule.setContent {
            DashboardMyProgressSection(state)
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testErrorStateDisplaysErrorMessageAndRetryButton() {
        var refreshCalled = false
        val state = DashboardMyProgressUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { onComplete ->
                refreshCalled = true
                onComplete()
            }
        )

        composeTestRule.setContent {
            DashboardMyProgressSection(state)
        }

        composeTestRule.onNodeWithText("Activities").assertIsDisplayed()

        composeTestRule.onNodeWithText("We weren't able to load this content.\nPlease try again.", substring = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Refresh")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        assert(refreshCalled) { "Refresh callback should be called when retry button is clicked" }
    }

    @Test
    fun testSuccessStateDisplaysModuleCountAndTitle() {
        val state = DashboardMyProgressUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardMyProgressCardState(
                moduleCountCompleted = 5
            )
        )

        composeTestRule.setContent {
            DashboardMyProgressSection(state)
        }

        composeTestRule.onNodeWithText("Activities").assertIsDisplayed()

        composeTestRule.onNodeWithText("5").assertIsDisplayed()

        composeTestRule.onNodeWithText("completed").assertIsDisplayed()
    }

    @Test
    fun testEmptyState() {
        val state = DashboardMyProgressUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardMyProgressCardState(
                moduleCountCompleted = 0
            )
        )

        composeTestRule.setContent {
            DashboardMyProgressSection(state)
        }

        composeTestRule.onNodeWithText("This widget will update once data becomes available.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }
}
