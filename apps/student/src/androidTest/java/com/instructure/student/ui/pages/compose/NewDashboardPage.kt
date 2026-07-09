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
package com.instructure.student.ui.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.espresso.page.BasePage

class NewDashboardPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertDashboardDisplayed() {
        waitForTag(DASHBOARD_PULL_REFRESH_INDICATOR_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_PULL_REFRESH_INDICATOR_TAG).assertExists()
    }

    fun refreshDashboard() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val x = device.displayWidth / 2
        val startY = (device.displayHeight * 0.3).toInt()
        val endY = (device.displayHeight * 0.8).toInt()
        device.swipe(x, startY, x, endY, 20)
        composeTestRule.waitForIdle()
    }

    fun assertWelcomeGreetingDisplayed(expectedGreeting: String, timeoutMillis: Long = 20000) {
        val matcher = hasTestTag(WELCOME_GREETING_TAG).and(hasText(expectedGreeting))
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(matcher).assertExists()
    }

    fun assertWelcomeMessageDisplayed() {
        waitForTag(WELCOME_MESSAGE_TAG)
        composeTestRule.onNodeWithTag(WELCOME_MESSAGE_TAG).assertIsDisplayed()
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 20000) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    companion object {
        private const val DASHBOARD_PULL_REFRESH_INDICATOR_TAG = "dashboardPullRefreshIndicator"
        private const val WELCOME_GREETING_TAG = "welcomeGreetingText"
        private const val WELCOME_MESSAGE_TAG = "welcomeMessageText"
    }
}
