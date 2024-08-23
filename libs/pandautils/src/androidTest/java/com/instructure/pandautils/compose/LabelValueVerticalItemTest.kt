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
package com.instructure.pandautils.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.compose.composables.LabelValueVerticalItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LabelValueVerticalItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLabelAndValue() {
        composeTestRule.setContent {
            LabelValueVerticalItem(label = "Label", value = "Value")
        }

        composeTestRule.onNode(hasText("Label").and(hasTestTag("label"))).assertIsDisplayed()
        composeTestRule.onNode(hasText("Value").and(hasTestTag("value"))).assertIsDisplayed()
    }

    @Test
    fun testLabelOnly() {
        composeTestRule.setContent {
            LabelValueVerticalItem(label = "Label")
        }

        composeTestRule.onNode(hasText("Label").and(hasTestTag("label"))).assertIsDisplayed()
        composeTestRule.onNodeWithTag("value").assertDoesNotExist()
    }
}