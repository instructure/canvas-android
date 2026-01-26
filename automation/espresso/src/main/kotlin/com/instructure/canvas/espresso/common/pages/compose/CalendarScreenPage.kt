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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.R
import org.junit.Assert.assertTrue

class CalendarScreenPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertCalendarPageTitle() {
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    fun clickOnAddButton() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.calendarAddNewCalendarItemContentDescription))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickAddEvent() {
        composeTestRule.onNodeWithText("Add Event").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickAddTodo() {
        composeTestRule.onNodeWithText("Add To Do").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickOnItem(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertItemDetails(eventTitle: String, contextName: String, eventDate: String? = null, eventStatus: String? = null, index: Int = -1) {
        if (index != -1) {
            // If we have more items in the list and a small device we need to scroll to the item.
            // We can't use a node matcher here because LazyColumn doesn't render all items at once.
            composeTestRule.onNodeWithTag("calendarEventsList").performScrollToIndex(index)
        }
        composeTestRule.onNode(hasAnyChild(hasText(contextName)).and(hasAnyChild(hasText(eventTitle)))).assertIsDisplayed()
        if (eventDate != null) composeTestRule.onNodeWithText(eventDate).assertIsDisplayed()
        if (eventStatus != null) composeTestRule.onNodeWithText(eventStatus).assertIsDisplayed()
    }

    fun assertEmptyView() {
        assertTrue(composeTestRule.onAllNodesWithTag("calendarEventsEmpty").fetchSemanticsNodes().isNotEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertItemDisplayed(itemTitle: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(
                hasTestTag("eventTitle") and hasText(itemTitle),
                useUnmergedTree = true
            ).fetchSemanticsNodes().size == 1
        }
    }

    fun assertItemNotExist(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).assertDoesNotExist()
    }

    fun assertItemNotDisplayed(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).assertIsNotDisplayed()
    }

    fun swipeCalendarLeft() {
        composeTestRule.onNodeWithTag("calendarPager").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()
    }

    fun swipeEventsLeft(times: Int = 1) {
        repeat(times) {
            composeTestRule.onNodeWithTag("calendarEventsPage0").performTouchInput {
                swipeLeft()
            }
            composeTestRule.waitForIdle()
        }
    }

    fun swipeEventsRight(times: Int = 1) {
        repeat(times) {
            composeTestRule.onNodeWithTag("calendarEventsPage0").performTouchInput {
                swipeRight()
            }
            composeTestRule.waitForIdle()
        }
    }

    fun clickCalendarHeader() {
        composeTestRule.onNodeWithTag("yearMonthTitle").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickCalendarFilters() {
        composeTestRule.onNodeWithText("Calendars").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickTodayButton() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.a11y_contentDescriptionCalendarJumpToToday), true).performClick()
        composeTestRule.waitForIdle()
    }

    fun checkCalendarCollapsed(): Boolean {
        return try {
            composeTestRule.onNode(hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
            composeTestRule.onNode(hasTestTag("calendarRow1").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsNotDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }
    }

    fun assertCalendarCollapsed() {
        composeTestRule.onNode(hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow1").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsNotDisplayed()
    }

    fun assertCalendarExpanded() {
        composeTestRule.onNode(hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow1").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow2").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow3").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
    }

    fun checkCalendarExpanded(): Boolean {
        return try {
            composeTestRule.onNode(hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
            composeTestRule.onNode(hasTestTag("calendarRow1").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
            composeTestRule.onNode(hasTestTag("calendarRow2").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
            composeTestRule.onNode(hasTestTag("calendarRow3").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }
    }
}