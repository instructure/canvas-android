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
package com.instructure.pandautils.compose.features.settings

import android.content.Context
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.pandautils.features.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.instructure.pandautils.R
import com.instructure.pandautils.features.settings.SettingsItem
import com.instructure.pandautils.features.settings.SettingsUiState

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun testSettingsItems() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val items = mapOf(
            R.string.preferences to listOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS,
            ),
            R.string.offlineContent to listOf(
                SettingsItem.OFFLINE_SYNCHRONIZATION
            ),
            R.string.legal to listOf(
                SettingsItem.ABOUT,
            )
        )

        val uiState = SettingsUiState(
            items = items,
            homeroomView = true,
            offlineState = R.string.daily,
            appTheme = R.string.appThemeLight,
            onClick = {},
            actionHandler = {}
        )
        composeTestRule.setContent {
            SettingsScreen(uiState = uiState) {}
        }

        items.forEach { (title, items) ->
            composeTestRule.onNodeWithText(context.getString(title)).assertExists()
            items.forEach { item ->
                composeTestRule.onNodeWithText(context.getString(item.res)).assertExists()
                composeTestRule.onNode(
                    hasTestTag("settingsItem").and(
                        hasAnyDescendant(
                            hasText(
                                context.getString(item.res)
                            )
                        )
                    ), useUnmergedTree = true
                )
                    .assertHasClickAction()
            }
        }
    }
}