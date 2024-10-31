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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.instructure.espresso.R
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.retry
import com.instructure.pandautils.utils.AppTheme

class SettingsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertSettingsItemDisplayed(title: String) {
        composeTestRule.onNode(
            hasTestTag("settingsItem").and(hasAnyDescendant(hasText(title))),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
    }

    fun clickOnSettingsItem(title: String) {
        val nodeMatcher = hasTestTag("settingsItem").and(hasAnyDescendant(hasText(title)))
        retry(catchBlock = {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).swipe(
                500,
                1500,
                500,
                500,
                10
            )
        }) {
            composeTestRule.onNode(nodeMatcher, useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()
        }

    }

    fun assertAboutDialogOpened() {
        onViewWithText("About").assertDisplayed()
    }

    fun assertLegalDialogOpened() {
        onViewWithText("Legal").assertDisplayed()
    }

    fun assertFiveStarRatingDisplayed() {
        for (i in 1 until 6) {
            Espresso.onView(ViewMatchers.withId(R.id.star + i))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    fun selectAppTheme(appTheme: AppTheme) {
        val testTag = when (appTheme) {
            AppTheme.LIGHT -> {
                "lightThemeButton"
            }

            AppTheme.DARK -> {
                "darkThemeButton"
            }

            AppTheme.SYSTEM -> {
                "systemThemeButton"
            }
        }

        composeTestRule
            .onNodeWithTag(testTag)
            .performScrollTo()
            .performClick()
    }

    fun clickOnSyncSettingsItem() {
        retry(catchBlock = {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).swipe(
                500,
                1500,
                500,
                500,
                10
            )
        }) {
            composeTestRule.onNode(
                hasTestTag("syncSettingsItem"),
                useUnmergedTree = true
            ).assertIsDisplayed()
                .performClick()
        }
    }

    fun clickOnSubscribeButton() {
        onViewWithText("Subscribe").click()
    }

    fun assertOfflineContentDisplayed() {
        retry(catchBlock = {
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).swipe(
                500,
                1500,
                500,
                500,
                10
            )
        }) {
            composeTestRule.onNode(
                hasTestTag("syncSettingsItem"),
                useUnmergedTree = true
            ).assertIsDisplayed()
        }
    }

    fun assertOfflineSyncSettingsStatus(status: String) {
        composeTestRule.onNodeWithTag("settingsList", useUnmergedTree = true)
            .performScrollToNode(hasTestTag("syncSettingsItem"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(status, useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertOfflineContentNotDisplayed() {
        composeTestRule.onNode(
            hasTestTag("syncSettingsItem"),
            useUnmergedTree = true
        ).assertIsNotDisplayed()
    }
}