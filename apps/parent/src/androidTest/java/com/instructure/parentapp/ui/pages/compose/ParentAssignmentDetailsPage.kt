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
 */
package com.instructure.parentapp.ui.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.page.withId
import com.instructure.parentapp.R

class ParentAssignmentDetailsPage(moduleItemInteractions: ModuleItemInteractions, composeTestRule: ComposeTestRule): AssignmentDetailsPage(moduleItemInteractions, composeTestRule) {

    fun assertDiscussionCheckpointDetailsOnDetailsPage(checkpointText: String, dueAt: String)
    {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNode(hasText(dueAt) and hasAnyAncestor(hasTestTag("dueDateColumn-$checkpointText") and hasAnyDescendant(hasTestTag("dueDateHeaderText-$checkpointText")))).assertIsDisplayed()
        } catch (e: AssertionError) {
            Espresso.onView(withId(R.id.dueComposeView)).perform(ViewActions.scrollTo())
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNode(hasTestTag("dueDateHeaderText-$checkpointText"), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode(hasText(dueAt) and hasAnyAncestor(hasTestTag("dueDateColumn-$checkpointText") and hasAnyDescendant(hasTestTag("dueDateHeaderText-$checkpointText")))).assertIsDisplayed()
    }

}