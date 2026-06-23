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
import androidx.compose.ui.test.performScrollTo
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.espresso.ModuleItemInteractions

class ParentAssignmentDetailsPage(moduleItemInteractions: ModuleItemInteractions, composeTestRule: ComposeTestRule): AssignmentDetailsPage(moduleItemInteractions, composeTestRule) {

    fun assertDiscussionCheckpointDetailsOnDetailsPage(checkpointText: String, dueAt: String)
    {
        composeTestRule.waitForIdle()
        // Scroll the checkpoint's due-date value into view (it sits below the header and can be
        // off-screen on landscape / small screens), then assert both the header and the due date.
        val dueDateNode = hasText(dueAt) and hasAnyAncestor(
            hasTestTag("dueDateColumn-$checkpointText") and hasAnyDescendant(hasTestTag("dueDateHeaderText-$checkpointText"))
        )
        composeTestRule.onNode(dueDateNode).performScrollTo().assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("dueDateHeaderText-$checkpointText"), useUnmergedTree = true).assertIsDisplayed()
    }
}
