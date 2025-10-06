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
package com.instructure.horizon.pages

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.performClick

class HorizonNotificationPage(private val composeTestRule: ComposeTestRule) {
    fun assertNotificationItem(title: String, label: String) {
        composeTestRule.onNode(
            hasAnyChild(hasText(label)).and(
                hasAnyChild(hasText(title))
            )
        ).assertIsDisplayed().onChild().assertHasClickAction()
    }

    fun clickNotificationItem(title: String, label: String) {
        composeTestRule.onNode(
            hasAnyChild(hasText(label)).and(
                hasAnyChild(hasText(title))
            )
        ).onChild().performClick()
    }
}